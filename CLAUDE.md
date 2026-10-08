# NewsTime

Offline-first Android news reader: top headlines from News API, built with Kotlin,
Jetpack Compose (Material 3), Hilt, Room, Retrofit 3 / kotlinx.serialization and Coil 3.

## Commands

```bash
./gradlew assembleDebug        # build
./gradlew testDebugUnitTest    # unit + Compose UI tests (JVM, Robolectric; no device needed)
./gradlew lintDebug            # Android lint
```

CI (`.github/workflows/android.yml`) runs all three on pushes to `main` and on pull requests.

## Setup

- News API key goes in `local.properties` (gitignored) as `API_KEY=...`; quotes optional.
  Never put it in `gradle.properties`, which is committed. Builds and tests work without a key.
- JDK 17+ runs the build (CI uses 21). Compile/target SDK 37, min SDK 24.
- Dependencies live in `gradle/libs.versions.toml`; add new ones there, not inline.

## Architecture

- `data/NewsCategory`: the feeds behind the category chips. `Top` is the Associated Press
  source; the rest are News API US categories (`sources` can't be combined with `category`).
- `data/NewsRepository`: Room is the single source of truth (`articles(category): Flow`).
  `refresh(category)` skips the network if that category's last refresh (persisted in
  DataStore via `RefreshTimeStore`) is under an hour old and its cache isn't empty.
- `data/local`: `ArticleEntity` keyed by (category, URL), ordered by `position`. The database uses
  destructive migration because it only caches headlines; turn on schema export and add real
  migrations before storing anything users create (e.g. saved articles).
- `ui/headlines`: `HeadlinesViewModel` exposes `HeadlinesUiState`; `HeadlinesScreen` is
  stateless (the route wires the ViewModel and opens articles in Custom Tabs).

## Tests

- `app/src/test`: Robolectric, in-memory Room (`TestDatabase.kt`), `FakeNewsApi`,
  `FakeRefreshTimeStore` and an injectable clock (`now`) for cache-age tests.
- Compose tests use `androidx.compose.ui.test.junit4.v2.createComposeRule`.
- `app/build.gradle.kts` passes `--add-exports`/`--add-opens` to unit tests so Robolectric
  works on JDK 25+.

## Gotchas

- Maven Central sometimes rate-limits fresh dependency downloads (HTTP 429). Retry with
  `--max-workers=1`; once cached it doesn't recur.
- README screenshots: use a new file name when replacing one, or GitHub's image cache keeps
  showing the old image for a while.
- Dependabot opens weekly update PRs (`.github/dependabot.yml`); merge them when CI is green.

## Git workflow

- One branch per pull request, named after the work (e.g. `claude/category-chips`), created
  fresh from the default branch. Never reuse a merged branch or force-push to restart one;
  follow-up fixes after a merge get their own new branch and pull request too.
- Merge with squash. Delete the head branch after merging: the repo has GitHub's
  "Automatically delete head branches" setting on for this (cloud sessions can't delete
  remote branches themselves).
