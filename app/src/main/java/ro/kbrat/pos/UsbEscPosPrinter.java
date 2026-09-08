package ro.kbrat.pos;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import java.util.HashMap;

/**
 * Trimite date ESC/POS catre o imprimanta termica USB folosind Android USB Host API.
 * Metodele adnotate cu @JavascriptInterface sunt apelabile din JavaScript
 * prin obiectul global window.AndroidPrinter.
 */
public class UsbEscPosPrinter {

    private static final String ACTION_USB_PERMISSION = "ro.kbrat.pos.USB_PERMISSION";

    private final Context ctx;
    private final UsbManager usbManager;
    private final Handler ui = new Handler(Looper.getMainLooper());

    private UsbDevice device;
    private UsbDeviceConnection connection;
    private UsbInterface iface;
    private UsbEndpoint endpointOut;

    private final BroadcastReceiver permReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context c, Intent intent) {
            if (!ACTION_USB_PERMISSION.equals(intent.getAction())) return;
            synchronized (UsbEscPosPrinter.this) {
                UsbDevice d = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                boolean granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
                if (granted && d != null) {
                    openDevice(d);
                    toast("Imprimanta USB conectata");
                } else {
                    toast("Permisiune USB refuzata");
                }
            }
        }
    };

    public UsbEscPosPrinter(Context context) {
        this.ctx = context.getApplicationContext();
        this.usbManager = (UsbManager) ctx.getSystemService(Context.USB_SERVICE);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        if (Build.VERSION.SDK_INT >= 33) {
            ctx.registerReceiver(permReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            ctx.registerReceiver(permReceiver, filter);
        }
    }

    /* ===================== API expus in JavaScript ===================== */

    /** Returneaza true daca exista o conexiune activa pentru scriere. */
    @JavascriptInterface
    public synchronized boolean isReady() {
        return connection != null && endpointOut != null;
    }

    /**
     * Cauta imprimanta USB si cere permisiunea. Conectarea propriu-zisa se
     * finalizeaza asincron, dupa ce utilizatorul accepta dialogul Android.
     */
    @JavascriptInterface
    public synchronized String connect() {
        UsbDevice found = findPrinter();
        if (found == null) {
            toast("Nicio imprimanta USB gasita. Verifica cablul OTG.");
            return "ERR_NO_DEVICE";
        }
        this.device = found;
        if (usbManager.hasPermission(found)) {
            return openDevice(found) ? "OK_CONNECTED" : "ERR_OPEN";
        }
        int flags = (Build.VERSION.SDK_INT >= 31)
                ? (PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE)
                : PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pi = PendingIntent.getBroadcast(
                ctx, 0, new Intent(ACTION_USB_PERMISSION).setPackage(ctx.getPackageName()), flags);
        usbManager.requestPermission(found, pi);
        return "OK_REQUESTED";
    }

    /** Primeste ESC/POS ca base64 si il trimite la imprimanta. */
    @JavascriptInterface
    public synchronized String printBase64(String b64) {
        try {
            if (!isReady()) {
                // incearca reconectare rapida daca aveam deja un device permis
                if (device != null && usbManager.hasPermission(device)) openDevice(device);
                if (!isReady()) return "ERR_NOT_CONNECTED";
            }
            byte[] data = Base64.decode(b64, Base64.DEFAULT);
            final int CH = 16384;
            int offset = 0;
            while (offset < data.length) {
                int len = Math.min(CH, data.length - offset);
                byte[] chunk = new byte[len];
                System.arraycopy(data, offset, chunk, 0, len);
                int sent = connection.bulkTransfer(endpointOut, chunk, len, 5000);
                if (sent < 0) return "ERR_TRANSFER";
                offset += len;
            }
            return "OK";
        } catch (Exception e) {
            return "ERR_" + e.getMessage();
        }
    }

    /* ===================== Intern ===================== */

    private UsbDevice findPrinter() {
        HashMap<String, UsbDevice> list = usbManager.getDeviceList();
        // 1) preferam un device cu interfata din clasa Printer (7)
        for (UsbDevice d : list.values()) {
            for (int i = 0; i < d.getInterfaceCount(); i++) {
                if (d.getInterface(i).getInterfaceClass() == UsbConstants.USB_CLASS_PRINTER) {
                    return d;
                }
            }
        }
        // 2) altfel, orice device care are un endpoint BULK OUT
        for (UsbDevice d : list.values()) {
            for (int i = 0; i < d.getInterfaceCount(); i++) {
                UsbInterface itf = d.getInterface(i);
                for (int e = 0; e < itf.getEndpointCount(); e++) {
                    UsbEndpoint ep = itf.getEndpoint(e);
                    if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK
                            && ep.getDirection() == UsbConstants.USB_DIR_OUT) {
                        return d;
                    }
                }
            }
        }
        return null;
    }

    private boolean openDevice(UsbDevice d) {
        closeConnectionOnly();
        UsbInterface chosen = null;
        UsbEndpoint out = null;
        for (int i = 0; i < d.getInterfaceCount() && chosen == null; i++) {
            UsbInterface itf = d.getInterface(i);
            for (int e = 0; e < itf.getEndpointCount(); e++) {
                UsbEndpoint ep = itf.getEndpoint(e);
                if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK
                        && ep.getDirection() == UsbConstants.USB_DIR_OUT) {
                    chosen = itf; out = ep; break;
                }
            }
        }
        if (chosen == null || out == null) { toast("Endpoint de scriere negasit"); return false; }

        UsbDeviceConnection conn = usbManager.openDevice(d);
        if (conn == null) { toast("Nu pot deschide imprimanta USB"); return false; }
        if (!conn.claimInterface(chosen, true)) {
            conn.close();
            toast("Nu pot prelua interfata USB");
            return false;
        }
        this.device = d;
        this.connection = conn;
        this.iface = chosen;
        this.endpointOut = out;
        return true;
    }

    private void closeConnectionOnly() {
        try {
            if (connection != null && iface != null) connection.releaseInterface(iface);
            if (connection != null) connection.close();
        } catch (Exception ignored) {}
        connection = null; iface = null; endpointOut = null;
    }

    public synchronized void close() {
        closeConnectionOnly();
        try { ctx.unregisterReceiver(permReceiver); } catch (Exception ignored) {}
    }

    private void toast(final String msg) {
        ui.post(() -> Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show());
    }
}
