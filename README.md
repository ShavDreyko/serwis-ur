# Serwis UR - Asystent Serwisu

Aplikacja Android do zarządzania zadaniami serwisowymi / utrzymania ruchu (UR).

## Funkcje
- Zadania serwisowe z priorytetami, statusami i kategoriami (w tym **Nadgodziny**)
- Notatki ze zdjęciami (zdjęcia są trwale zapisywane w aplikacji)
- Kalendarz i podsumowanie miesięczne
- Generowanie raportu PDF z opcjonalnymi zdjęciami z notatek
- Prace dodatkowe

## Budowanie

### Lokalnie (Android Studio)
1. Otwórz projekt w Android Studio
2. Zsynchronizuj Gradle
3. `Build → Build Bundle(s) / APK(s) → Build APK(s)`

### CI (GitHub Actions)
Przy każdym pushu na `main` uruchamia się workflow **Android CI**, który buduje debug APK.

Po zakończeniu builda:
1. Wejdź w zakładkę **Actions**
2. Wybierz ostatni workflow run
3. Pobierz artefakt **app-debug-apk**

## Repo
https://github.com/ShavDreyko/serwis-ur
