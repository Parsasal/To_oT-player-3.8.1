# TO_OT Player v3.8.0

A high-performance, multi-lane audio workstation and smart player for Android, built with Jetpack Compose and Material 3.

## What's New in v3.8.0 (Build 46)

### 🎵 Device Audio & Downloads Explorer Rebuild
- **Complete Redesign:** Ground-up overhaul with clean Material 3 dark studio glass aesthetics.
- **Batch Management:** Added multi-track selection with floating bottom bar, fast audio preview playback, and instant target lane assignment chips.

### ⌨️ Seamless Keyboard Dismissal
- **Scroll-to-Dismiss (`onDrag`):** Integrated bidirectional `nestedScroll` connection to immediately dismiss the soft keyboard upon list scrolling or dragging.
- **Tap-Outside Unfocus:** Instant focus clearance and keyboard suppression whenever tapping anywhere outside the search field without blocking item clicks.
- **Action Buttons:** Added an explicit hide-keyboard icon button and wired IME `Search`/`Done` actions.

### ⚡ Performance & Architecture Overhaul
- **Lifecycle-Aware State Collection:** Upgraded all `StateFlow` collections to `collectAsStateWithLifecycle()` to eliminate background CPU/battery drain.
- **Lazy Initialization:** Converted heavy secondary services (`MasterRecorder`, `VideoAudioExtractor`, `DeviceAudioScanner`, `AiMusicSearchService`) to lazy initialization for lightning-fast cold app launches.
- **Memory & Scanner Safety:** Added direct `SIZE > 0` database query filtering and memory bounds safeguards (`maxResults`) to eliminate `OutOfMemoryError` risks on large audio libraries.
