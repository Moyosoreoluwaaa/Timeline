# Timeline

Timeline is an app-usage measuring app for Android. It runs as a floating overlay
on top of other apps (dockable to the side of the screen), captures a screenshot
of whichever app is currently open, and tracks usage sessions — entirely offline,
with no accounts, network calls, AI, or payments involved.

### What it does

- **Floating overlay** — displays over other apps and can be docked to a screen edge
- **Foreground service** — keeps tracking running in the background with a
  glance-style persistent notification (play/pause-style controls, similar to a
  media player notification)
- **Screenshot capture** — snapshots the app currently open; screenshots are cached
  in-app (not saved to device storage) and automatically purged after 14 days to
  keep storage usage low
- **App thumbnails** — shows each tracked app's icon in the usage list/history
- **Local prefs** — dock side, tracking on/off, purge-interval overrides, etc. are
  stored locally

### Permissions

- Display over other apps (overlay)
- App usage access (usage stats)
- Notifications (foreground service)

### Project structure

This is a Kotlin Multiplatform project. The current feature set — usage stats,
overlay windows, foreground-service capture, and cross-app screenshotting — relies
entirely on Android-only OS capabilities, so active development is Android-first
for now. The KMP/iOS scaffolding is left in place but unused until there's an
actual iOS-compatible feature to build.

* [/androidApp](./androidApp) — the Android application module: overlay window,
  foreground service, notification, screenshot capture/viewer, permissions UI.

* [/shared](./shared/src) — code shared across Compose Multiplatform targets.
  - [commonMain](./shared/src/commonMain/kotlin) — code common to all targets.
  - [androidMain](./shared/src/androidMain/kotlin) — Android-specific implementations
    (currently where most feature logic lives): local persistence (Room, DataStore),
    the periodic cache-purge worker, and logging.
  - Other folders (e.g. [iosMain](./shared/src/iosMain/kotlin)) are reserved for
    platform-specific code once/if an iOS target is picked back up.

### Running the app

Use the run configuration in your IDE's toolbar, or:

- Android app: `./gradlew :androidApp:assembleDebug`

### Running tests

- Android tests: `./gradlew :shared:testAndroidHostTest`

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…