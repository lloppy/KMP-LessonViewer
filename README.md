# Akta

Кроссплатформенное приложение на **Kotlin Multiplatform + Compose Multiplatform**.

**Платформы:** Android · iOS · Desktop (macOS / Windows / Linux)

## Структура проекта

```
.
├── androidApp/   ← Android-приложение (точка входа, манифест, ресурсы)
├── desktopApp/   ← Desktop-приложение (JVM)
├── iosApp/       ← iOS-приложение (Xcode-проект, обёртка над Compose)
└── shared/       ← Общий код (commonMain) + платформенные реализации
    └── src/
        ├── commonMain/   ← App, тема, DI
        ├── androidMain/
        ├── desktopMain/
        └── iosMain/
```

Базовый пакет: `com.lloppy.akta`.

## Запуск

- **Android:** `./gradlew :androidApp:installDebug` или запуск из Android Studio.
- **Desktop:** `./gradlew :desktopApp:run`.
- **iOS:** открыть `iosApp/iosApp.xcodeproj` в Xcode и запустить.
