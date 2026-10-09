# Android skills

Agent skills from [android/skills](https://github.com/android/skills) (Google, Apache License 2.0;
each skill folder includes `LICENSE.txt`). Claude Code loads them automatically in this repo.

Copied unmodified from commit `42dc2270e96032bd860bb94511e440aa00a43125` (2026-09-25):

| Skill | Source path | Use it for |
|---|---|---|
| `edge-to-edge` | `system/edge-to-edge` | System bar and keyboard insets, list padding |
| `android-intent-security` | `security/android-intent-security` | Intents, exported components, PendingIntents |
| `testing-setup` | `testing/testing-setup` | Unit, Compose UI, screenshot and database tests |
| `agp-9-upgrade` | `build-system/agp/agp-9-upgrade` | AGP 9 DSL, built-in Kotlin, KSP, BuildConfig |
| `adaptive` | `jetpack-compose/adaptive` | Tablet and foldable layouts, adaptive grids |

To update, copy the same folders from a newer commit of android/skills and change the commit above.
