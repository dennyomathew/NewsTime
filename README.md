# NewsTime

Top headlines from [News API](https://newsapi.org/), cached offline.

![alt tag](https://i.imgur.com/xmtks1h.png)

## Tech stack

- **UI**: Jetpack Compose with Material 3 (pull to refresh, edge-to-edge, light and dark themes)
- **Architecture**: single-activity MVVM; Room is the single source of truth, exposed as a `Flow`
- **DI**: Hilt
- **Networking**: Retrofit 3, OkHttp 5 and kotlinx.serialization
- **Images**: Coil 3
- **Build**: Gradle 9.8 (Kotlin DSL, version catalog), Android Gradle Plugin 9.4, Kotlin 2.4, KSP
- **Tests**: JUnit, Robolectric and kotlinx-coroutines-test

## Setup

1. Clone this project.
2. Get an API key from [News API](https://newsapi.org/).
3. Add it to `local.properties` in the project root (this file is gitignored):
   ```properties
   API_KEY=your-key-here
   ```
   A Gradle property works too, for example `API_KEY=...` in `~/.gradle/gradle.properties`.
4. Open the project in Android Studio and run the `app` configuration.

Without a key the app still builds and launches, then shows an "API key is missing or invalid" message.

## Commands

```bash
./gradlew assembleDebug        # build the debug APK
./gradlew testDebugUnitTest    # run unit tests
./gradlew lintDebug            # run Android lint
```
