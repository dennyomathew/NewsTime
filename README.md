# NewsTime

Top headlines from [News API](https://newsapi.org/), cached offline.

<img src="docs/screenshot.webp" alt="NewsTime headlines screen" width="320">

## Tech stack

- **UI**: Jetpack Compose with Material 3 (pull to refresh, edge-to-edge, light and dark themes)
- **Architecture**: single-activity MVVM; Room is the single source of truth, exposed as a `Flow`
- **DI**: Hilt
- **Networking**: Retrofit 3, OkHttp 5 and kotlinx.serialization
- **Images**: Coil 3
- **Build**: Gradle 9.8 (Kotlin DSL, version catalog), Android Gradle Plugin 9.4, Kotlin 2.4, KSP
- **Tests**: JUnit, Robolectric, Compose UI tests and kotlinx-coroutines-test, run on every push and pull request by GitHub Actions

## Setup

1. Clone this project.
2. Get an API key from [News API](https://newsapi.org/).
3. Add it to `local.properties` in the project root, creating the file if it doesn't exist:
   ```properties
   API_KEY=your-key-here
   ```
   `local.properties` is gitignored, so your key stays on your machine. Don't put the key in
   `gradle.properties`: that file is committed.
4. Open the project in Android Studio and run the `app` configuration.

Without a key the app still builds and launches, then shows an "API key is missing or invalid" message.

## Commands

```bash
./gradlew assembleDebug        # build the debug APK
./gradlew testDebugUnitTest    # run unit tests
./gradlew lintDebug            # run Android lint
```
