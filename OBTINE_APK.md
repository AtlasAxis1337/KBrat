# Cum obții APK-ul fără Android Studio (build în cloud pe GitHub)

Nu ai nevoie de nimic instalat pe Mac. GitHub compilează APK-ul pentru tine,
gratuit, iar tu doar descarci fișierul.

## Pași

1. **Creează un repository nou** pe GitHub (poate fi privat).
2. **Urcă tot conținutul folderului `KBratPOS`** în acel repo.
   - Cel mai simplu prin site: butonul „Add file → Upload files" și tragi
     toate fișierele/folderele (păstrează structura de foldere).
   - Sau prin git:
     ```
     cd KBratPOS
     git init
     git add .
     git commit -m "KBrat POS Android"
     git branch -M main
     git remote add origin https://github.com/UTILIZATOR/REPO.git
     git push -u origin main
     ```
3. Mergi în repo la tab-ul **Actions**. Vei vedea workflow-ul
   **„Build KBrat POS APK"**. Dacă îți cere, apasă „I understand… enable".
4. Build-ul pornește automat după push. Dacă nu, apasă **Run workflow**.
5. După ~3–5 minute, când apare bifa verde ✅, deschide rularea și, jos la
   secțiunea **Artifacts**, descarcă **`KBratPOS-apk`**.
6. Dezarhivezi → obții **`KBratPOS.apk`**.

## Instalarea pe tabletă

1. Copiază `KBratPOS.apk` pe tabletă (cablu, e-mail, Drive etc.).
2. Deschide-l → permite instalarea din „surse necunoscute" dacă ți se cere.
3. Deschide aplicația **KBrat POS**.
4. Conectează imprimanta SC053 prin **USB-C OTG** → apasă
   **🔌 Conectează imprimanta** → **OK** la permisiunea USB.
5. Printezi bonurile direct, offline.

> Este un APK de tip *debug* (semnat cu cheia de test) — perfect pentru uz
> intern pe tableta ta. Nu poate fi publicat în Google Play așa, dar pentru
> food truck se instalează și funcționează fără probleme.
