package ro.kbrat.pos;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebChromeClient;

import androidx.appcompat.app.AppCompatActivity;

/**
 * KBrat Family POS - container Android (WebView).
 * Incarca aplicatia HTML din assets si expune puntea "AndroidPrinter"
 * catre JavaScript, pentru printare ESC/POS direct pe imprimanta USB.
 */
public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private UsbEscPosPrinter printer;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ecran mereu aprins - util la casa de marcat / food truck
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);            // localStorage (istoric bonuri, produse)
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setMediaPlaybackRequiresUserGesture(false);

        webView.setWebChromeClient(new WebChromeClient());

        // Puntea nativa expusa in JS ca window.AndroidPrinter
        printer = new UsbEscPosPrinter(this);
        webView.addJavascriptInterface(printer, "AndroidPrinter");

        webView.loadUrl("file:///android_asset/index.html");
    }

    @Override
    protected void onDestroy() {
        if (printer != null) printer.close();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
}
