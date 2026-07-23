# jimvro for Android

Native, local-first gym diary built with Kotlin and Jetpack Compose.

## Features

- Today dashboard for training, nutrition, and latest weigh-in
- Workout logging with exercises, sets, reps, weight, RPE, and volume
- Body measurements with history
- Manual food and macro logging
- On-device barcode scanning with Open Food Facts product lookup
- Room/SQLite storage with no account, server, or AI dependency
- Light and dark themes following device settings

All diary data remains in app database on device. Barcode recognition happens
on device; fetching a product that is not already cached requires internet.
Scanned products are reviewed before they are saved.

## Requirements

- Android Studio with JDK 17 or newer
- Android SDK 36
- Android device or emulator running Android 6.0 (API 23) or newer

## Build

Open this directory in Android Studio, allow Gradle sync to finish, then run the
`app` configuration. From terminal:

```sh
./gradlew testDebugUnitTest assembleDebug
```

Debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Architecture

- `ui/` — Compose navigation, screens, dialogs, and design system
- `data/` — Room entities/DAOs, repository, and Open Food Facts client
- `domain/` — reusable business rules such as macro scaling
- `AppViewModel` — lifecycle-aware UI state and write operations

Room schema exports live in `app/schemas/` and should be committed with future
database migrations.

## Privacy

Android cloud backup is disabled. App requests internet access only for product
lookup and Google Play services barcode scanner delivery. No API key is stored
in the app.

Product data is provided by [Open Food Facts](https://world.openfoodfacts.org/).
