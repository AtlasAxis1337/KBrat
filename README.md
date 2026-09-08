# KBrat Family POS — aplicație Android (WebView + imprimantă USB ESC/POS)

Aceasta este aplicația ta HTML `index.html` împachetată ca aplicație Android nativă.
Printarea bonurilor (client + bucătărie) se face **direct pe imprimanta termică USB**
prin Android USB Host API — nu mai depinde de limitările Chrome/WebUSB.

Imprimanta țintă: **Soninhigh SC053, 80mm, ESC/POS, USB + LAN**, conectată la tabletă
prin adaptor / cablu **USB-C OTG**.

---

## Ce conține proiectul

```
KBratPOS/
├─ settings.gradle
├─ build.gradle                (top-level)
├─ gradle.properties
└─ app/
   ├─ build.gradle
   ├─ proguard-rules.pro
   └─ src/main/
      ├─ AndroidManifest.xml
      ├─ assets/index.html      ← aplicația ta (cu punte nativă adăugată)
      ├─ java/ro/kbrat/pos/
      │   ├─ MainActivity.java          (WebView + expune AndroidPrinter)
      │   └─ UsbEscPosPrinter.java      (trimite ESC/POS pe USB)
      └─ res/
          ├─ drawable/ic_launcher.xml
          └─ values/strings.xml
```

---

## Cum obții APK-ul (Android Studio)

1. Instalează **Android Studio** (gratuit) pe Mac.
2. `File → Open…` și selectează folderul `KBratPOS`.
3. Lasă Android Studio să facă **Gradle Sync** (descarcă automat SDK-ul și
   Gradle Wrapper). Dacă îți cere să instaleze componente SDK, acceptă.
4. Conectează tableta prin USB (cu **Depanare USB / USB debugging** activat) SAU
   folosește: `Build → Build Bundle(s)/APK(s) → Build APK(s)`.
5. APK-ul rezultat îl găsești în:
   `app/build/outputs/apk/debug/app-debug.apk`
6. Copiază APK-ul pe tabletă și instalează-l (permite „surse necunoscute").

> Notă: proiectul nu include binarul `gradle-wrapper.jar` (fișier binar).
> Când deschizi proiectul în Android Studio, wrapper-ul e generat/descărcat
> automat. Dacă vrei build din linie de comandă, rulează întâi în folder:
> `gradle wrapper` (necesită Gradle instalat), apoi `./gradlew assembleDebug`.

---

## Cum funcționează la casă (food truck)

1. Conectezi imprimanta SC053 la tabletă prin **USB-C OTG**.
2. Deschizi aplicația **KBrat POS**.
3. Apeși **🔌 Conectează imprimanta** → Android arată dialogul de permisiune
   USB → apeși **OK** (bifează „folosește implicit pentru acest dispozitiv").
4. Butonul devine **🟢 Imprimantă conectată**.
5. Fiecare **Print** trimite automat bonul de client + bonul de bucătărie,
   fără dialog, cu tăiere automată (auto-cut).

Datele (produse, prețuri, istoric bonuri, numerotare) se salvează local pe
tabletă în `localStorage` — funcționează 100% offline.

---

## Dacă imprimanta nu e găsită

- Verifică întâi cu un stick USB că tableta vede USB prin OTG (adică suportă
  USB Host). Samsung Galaxy Tab suportă.
- Încearcă un cablu/adaptor **USB-C OTG** dedicat (nu doar convertor de conector).
- Unele adaptoare/hub-uri alimentate (ex. HP Dock G2) sunt gândite pentru laptop;
  un OTG simplu USB-C→USB-A funcționează mai sigur pe tabletă.

---

## Alternativă rapidă fără compilare (plan B)

Dacă nu vrei să compilezi acum, poți testa imediat cu aplicația **RawBT**
(print service ESC/POS pentru Android, gratuit) + versiunea web pe HTTPS.
Aplicația nativă de aici rămâne însă soluția cea mai curată și stabilă.
