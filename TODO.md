# HrvXo - Development Log & TODO

## Last Session

- **Date:** 2026-02-16
- **Summary:** v1.7.0 — animated coherence ring, recording progress, celebration messages, share results, color coding, home stats
- **Key changes:**
  - Animated circular coherence ring on session screen (Canvas + animateFloatAsState)
  - Color-coded coherence scores across all screens (green/amber/red)
  - Recording progress bar during 60s minimum with coherence-colored fill
  - Session celebration messages based on avg coherence performance
  - Quick stats dashboard on HomeScreen (streak, avg coherence, total songs)
  - Share button on session summary via Android ShareSheet
  - Insights auto-refresh when returning from session to home
  - BPM display alongside RMSSD in session coherence header
  - Version bump to v1.7.0 (versionCode 10)
- **Stopped at:** All features built and pushed. Continuing to build.
- **Blockers:** None

---

## Current Status

### Working Features
- Compose Multiplatform project builds successfully (Android target)
- BLE permission request flow (Android 12+ and older)
- Device chooser UI: Polar H10 or Generic BLE HR Monitor selection
- Polar H10 device scanning (finds nearby Polar devices)
- Generic BLE HR sensor scanning and connection
- Device connection with auto HR streaming
- Live heart rate (BPM), RR intervals, coherence score, and RMSSD display
- Connection status + battery level display
- Error handling and disconnect
- HRV processing pipeline: artifact detection (cubic spline), RMSSD, coherence score, LF/HF band powers
- 40 unit tests all passing (PhysioNet spectral validation within 15% of scipy reference)
- App icon (generated from logo, adaptive icon support)
- HRV_METHODOLOGY.md scientific methodology document
- YouTube Music backend deployed at `https://hrvxo-music.fly.dev` (FastAPI + ytmusicapi)
- Ktor HTTP client wired to backend (search songs, create playlists)
- SQLDelight local database for song-coherence persistence
- Session screen UI with 6 phase states and song search bottom sheet
- Coherence-based playlist generation from user's actual leaderboard data

- Auto music detection via NotificationListenerService (track changes + play/pause)
- Session pause YTM on end, auto-return to app after YTM launch
- Early-end handling with user-friendly message
- Movement detection via accelerometer + HR anomaly flagging
- v1.1.0 released on GitHub with signed APK

- Session history screen with expand/collapse per session
- CSV export via ShareSheet
- Cold-start suggestion chips for new users (< 3 songs)
- Dark mode with persistent theme toggle (teal/cyan brand colors)
- Insights screen with stats, coherence trend chart, best song/artist, total listen time
- Streak tracking (current + longest consecutive session days)
- 3-page onboarding flow for first-time users (HorizontalPager)
- Re-listen mode: top coherence songs shown during active sessions for quick replay
- Leaderboard screen with rank badges and listen counts
- About screen with app info, version, how-it-works, privacy
- Delete session and delete all data in History (with confirmation dialogs)

### In Progress
- None

### Known Bugs
- YTM ads play before songs — cannot be controlled externally (YTM Premium only)

---

## TODO - Priority

1. [x] **HRV & Coherence — Validation Complete:**
    - [x] Implement artifact removal with cubic spline interpolation (Quigley spec)
    - [x] Calculate coherence score based on PSD (HeartMath algorithm)
    - [x] Add LF and HF band powers (no ratio)
    - [x] Fix PSD normalization to match scipy (ms²/Hz units)
    - [x] Re-run all 40 tests after PSD normalization fix — all pass
    - [x] PhysioNet spectral powers within 15% of scipy reference (LF: 5.8% error, HF: 8.8% error)
    - [x] Tightened spectral power tolerances from 50% to 15%
    - [x] HRV metrics displayed in UI (coherence score + RMSSD)
2. [x] **YouTube Music Integration (replaced Spotify):**
    - [x] Build FastAPI backend with ytmusicapi (POST /search, POST /create-playlist, GET /health)
    - [x] Deploy to Fly.io Sydney region
    - [x] Wire Ktor HTTP client in Android app
    - [x] Song search and tagging via backend proxy
3. [x] **Music Session Mode:**
    - [x] Session screen with 5 phase states (NOT_STARTED, ACTIVE_NO_SONG, ACTIVE_SETTLING, ACTIVE_RECORDING, ENDED)
    - [x] 15-second settle-in exclusion at start of each new song
    - [x] Minimum 60 seconds per song for valid coherence reading
    - [x] Per-song coherence result cards with validity indicator
    - [x] Session summary on End: songs analysed, average coherence, best song
    - [x] Manual song tagging (YouTube Music has no "now playing" API)
    - [x] Auto music detection via NotificationListenerService (replaces manual-only tagging)
    - [x] ACTIVE_WAITING_PLAYBACK phase for pending song detection
4. [x] **Song-Coherence Database:**
    - [x] SQLDelight local database (song_coherence table)
    - [x] Store per-song coherence results after each valid reading
    - [x] Top coherence songs leaderboard (AVG grouped by video_id)
    - [x] Song count tracking for playlist generation threshold
5. [x] **Playlist Generation:**
    - [x] Progress indicator: "3/3 songs — ready for playlist" (3 song minimum for MVP)
    - [x] "Create Coherence Playlist" button: takes top songs from leaderboard, creates YouTube Music playlist
    - [x] Primary generation always from user's actual coherence leaderboard
6. [x] **Movement Detection:**
    - [x] Phone accelerometer monitoring via Android SensorManager (TYPE_LINEAR_ACCELERATION)
    - [x] Rolling movement score: flag when acceleration exceeds resting threshold
    - [x] HR anomaly detection: flag sudden HR spikes (>30 BPM above rolling average) not explained by music change
    - [x] Per-song movement badge: "Movement detected — score may be less accurate"
    - [x] Live red warning on recording screen when movement detected

---

## TODO - Nice to Have

- [x] Cold-start suggestions for new users with zero session data (search "ambient meditation calm", etc.)
- [x] Onboarding flow: Polar H10 pairing guide → YouTube Music setup → first session walkthrough
- [ ] Genre/tempo analysis: correlate audio features with coherence scores
- [x] Re-listen mode: replay top coherence songs and see if scores hold across sessions
- [ ] iOS support (KMP structure ready, targets commented out)
- [x] Dark mode / theme customization
- [x] Export coherence session data (CSV/JSON)
- [x] Historical session tracking and trends chart
- [x] App icon and branding assets (generated from logo)
- [x] Session reminders / streak tracking ("Listen for 10 minutes daily")
- [x] Insights: "Your coherence is X% higher with slower tempo songs" (after 20+ songs)

---

## Completed

- [x] Set up Compose Multiplatform project for Android and iOS (2026-02-03)
- [x] Rename the project to "HeartSyncRadio" (2026-02-03)
- [x] Integrate the Polar SDK for Android (2026-02-03)
- [x] Implement logic to search for, connect to, and receive data from H10 sensor (2026-02-03)
- [x] Design Home Screen as central hub for Polar H10 connection (2026-02-03)
- [x] Set up standard repo files (.gitignore, CREDENTIALS.md, SECURITY_AUDIT.md) (2026-02-03)
- [x] Implement HRV processing pipeline: ArtifactDetector, Rmssd, CoherenceCalculator, HrvProcessor, Fft (2026-02-03)
- [x] Rewrite ArtifactDetector with cubic spline interpolation per Quigley spec (2026-02-03)
- [x] Add LF/HF band powers to CoherenceCalculator and HrvMetrics (2026-02-03)
- [x] Fix frequency resolution bug in CoherenceCalculator (segment size vs total data) (2026-02-03)
- [x] Add PSD normalization for correct ms²/Hz units (2026-02-03)
- [x] Write 40 unit tests including PhysioNet real-data validation (2026-02-03)
- [x] Download PhysioNet Subject 000 RR data and compute scipy reference values (2026-02-03)
- [x] Device chooser UI: Polar H10 / Generic BLE HR Monitor selection (2026-02-04)
- [x] HrDeviceManager interface + GenericBleManager for multi-device support (2026-02-04)
- [x] HomeViewModel flatMapLatest for dynamic device manager switching (2026-02-04)
- [x] App icon from logo — all mipmap densities + adaptive icon (2026-02-04)
- [x] HRV_METHODOLOGY.md scientific methodology document (2026-02-04)
- [x] Switch spectral integration to trapezoidal rule (LF error 23%→5.8%, HF 16.5%→8.8%) (2026-02-04)
- [x] Extract CubicSpline.kt shared utility from ArtifactDetector (2026-02-04)
- [x] Tighten PhysioNet spectral test tolerances from 50% to 15% (2026-02-04)
- [x] Build YouTube Music backend (FastAPI + ytmusicapi) with search and playlist creation (2026-02-10)
- [x] Deploy backend to Fly.io Sydney region (2026-02-10)
- [x] Wire Ktor HTTP client to backend (2026-02-10)
- [x] Set up SQLDelight database for song-coherence tracking (2026-02-10)
- [x] Build SessionManager with 15s settle-in and 60s minimum recording (2026-02-10)
- [x] Build SessionViewModel with search, tag, end session, create playlist (2026-02-10)
- [x] Create SessionScreen UI with 5 session phases (2026-02-10)
- [x] Add enum-based navigation (HOME ↔ SESSION) (2026-02-10)
- [x] Wire MainActivity with SessionViewModel integration (2026-02-10)
- [x] Add auto music detection via NotificationListenerService (2026-02-11)
- [x] Fix release APK signing — add debug signingConfig to release build type (2026-02-11)
- [x] Bump version to v1.1.0 and create GitHub Release (2026-02-11)
- [x] Fix session auto-detecting already-playing songs — baseline snapshot + hasUserSelectedSong flag (2026-02-11)
- [x] Replace "Collecting data ~30s" card with subtle "Analysing heart rate..." text (2026-02-11)
- [x] Add 60s milestone notice and movement instructions to session screen (2026-02-11)
- [x] Add auto-return to HrvXo after opening YouTube Music (2026-02-11)
- [x] Add YTM pause on session end via MediaController (2026-02-11)
- [x] Add early-end message card for sessions ended before 60s (2026-02-11)
- [x] Session history screen with expand/collapse per session (2026-02-15)
- [x] CSV export via ShareSheet with FileProvider (2026-02-15)
- [x] Cold-start suggestion chips for new users (2026-02-15)
- [x] Add movement_detected column to song_coherence table with migration (2026-02-15)
- [x] Dark mode with persistent toggle and custom HrvXoTheme (teal/cyan brand) (2026-02-15)
- [x] Insights screen: stats grid, coherence trend chart, best song/artist, listen time (2026-02-15)
- [x] Streak tracking: current + longest consecutive session days (2026-02-15)
- [x] InsightsViewModel with all insight data loading and streak calculation (2026-02-15)
- [x] Material Icons Extended for BarChart/DarkMode/LightMode icons (2026-02-15)
- [x] 3-page onboarding flow with HorizontalPager (Welcome, Sensor, Music) (2026-02-15)
- [x] Re-listen mode: top coherence songs on session screen for quick replay (2026-02-15)
- [x] Version bump to v1.6.0 (versionCode 9) (2026-02-15)
- [x] Leaderboard screen with rank badges and listen counts (2026-02-15)
- [x] About screen with version, how-it-works, privacy info (2026-02-15)
- [x] Delete session + delete all data in History with confirmation dialogs (2026-02-15)
- [x] SQL queries: deleteSession, deleteAllData (2026-02-15)
- [x] Bottom navigation bar: Home, Insights, Leaderboard, History tabs (2026-02-15)
- [x] README overhaul with all v1.6.0 features (2026-02-15)
- [x] Color-coded coherence scores across all screens (green/amber/red) (2026-02-16)
- [x] Quick stats dashboard on HomeScreen (streak, avg, songs) (2026-02-16)
- [x] Share session results button via Android ShareSheet (2026-02-16)
- [x] Animated coherence ring on session screen with Canvas drawing (2026-02-16)
- [x] Recording progress bar with 60s countdown (2026-02-16)
- [x] Session celebration messages based on performance (2026-02-16)
- [x] Insights auto-refresh when returning from session (2026-02-16)
- [x] BPM display in session coherence header (2026-02-16)
- [x] Version bump to v1.7.0 (versionCode 10) (2026-02-16)

---

## Architecture & Decisions

| Decision | Reason | Date |
|----------|--------|------|
| Compose Multiplatform (KMP) | Cross-platform with shared UI, Android-first | 2026-02-03 |
| Single `composeApp` module | Simpler for greenfield; extract shared module later | 2026-02-03 |
| PolarManager with StateFlows | Clean bridge from RxJava (Polar SDK) to Compose | 2026-02-03 |
| minSdk 26 | Required by Polar BLE SDK 6.14.0 | 2026-02-03 |
| No expect/actual for ViewModel | Android-first; pass state as params to commonMain UI | 2026-02-03 |
| Manual DI (AppModule singleton) | No DI framework needed at this scale | 2026-02-03 |
| Cubic spline for artifact correction | Quigley spec: don't delete beats, interpolate to preserve time-series integrity | 2026-02-03 |
| No LF/HF ratio | Scientifically unsound; individual LF and HF band powers are valid | 2026-02-03 |
| Pure Kotlin FFT (Cooley-Tukey) | KMP commonMain can't use platform FFT libs; keeps code cross-platform | 2026-02-03 |
| HeartMath coherence algorithm | Peak power ratio in 0.04-0.26 Hz band; 64s sliding window | 2026-02-03 |
| Trapezoidal integration for band power | More accurate than rectangular; reduced spectral error by ~60% | 2026-02-04 |
| Linear resampling over cubic spline | Spline overshoots on irregular RR data; HF error 8.8% vs 78.9% with spline | 2026-02-04 |
| HrDeviceManager interface | Abstracts Polar SDK vs standard BLE; enables device chooser | 2026-02-04 |
| 60s minimum per song | Task Force HRV guidelines: 60s minimum for reliable short-term metrics; aligns with 64s coherence window | 2026-02-04 |
| 15s settle-in exclusion | Cardiac response to new auditory stimulus stabilises in ~10-15s; 30s too aggressive for session flow | 2026-02-04 |
| YouTube Music via backend proxy | Spotify registration frozen; ytmusicapi provides search + playlist creation via FastAPI backend | 2026-02-10 |
| Manual song tagging | YouTube Music has no "now playing" API (unlike Spotify); user searches and taps to tag | 2026-02-10 |
| Ktor 3.0.3 HTTP client | KMP-compatible; ContentNegotiation with kotlinx.serialization.json | 2026-02-10 |
| SQLDelight for local DB | KMP-compatible; type-safe SQL with coroutines Flow support | 2026-02-10 |
| Enum-based navigation | Simple state-driven screen switching; no nav library needed at this scale | 2026-02-10 |
| 3 song minimum for playlist | MVP threshold; lower than original 10 to let users experience playlist creation faster | 2026-02-10 |
| Leaderboard-based playlists | Primary generation always from user's actual coherence data, not genre assumptions | 2026-02-10 |
| NotificationListenerService for auto-detection | YTM has no "now playing" API; MediaSessionManager provides track metadata and playback state | 2026-02-11 |
| Baseline track snapshot on session start | Prevents auto-detecting already-playing song; only reacts to NEW tracks after user selects via search | 2026-02-11 |
| Debug signingConfig for release builds | Proper signing with release keystore deferred; debug keystore allows APK installation for testing | 2026-02-11 |
| Version bump per release | User requires GitHub Releases to be versioned — bump versionCode/versionName for each release | 2026-02-11 |
| HistoryViewModel separate from SessionViewModel | History is an independent concern; keeps SessionViewModel focused on active sessions | 2026-02-15 |
| UI models (SessionSummaryUi, HistorySongUi) in commonMain | Keeps HistoryScreen in commonMain decoupled from SQLDelight generated types | 2026-02-15 |
| SQLDelight migration (1.sqm) for movement_detected | Preserves existing user data; column defaults to 0 for historical records | 2026-02-15 |
| FileProvider + ACTION_SEND for CSV export | ShareSheet pattern lets users share via email/Drive/files; no SAF picker complexity | 2026-02-15 |
| Cold-start chips gated on totalSongCount < 3 | Matches existing playlist threshold (3 songs); disappears naturally once user has data | 2026-02-15 |
| HrvXoTheme with light/dark color schemes | Custom teal/cyan brand; SharedPreferences for persistence across restarts | 2026-02-15 |
| InsightsViewModel separate from HistoryViewModel | Insights is analytics (aggregates + streaks); History is raw session browsing | 2026-02-15 |
| Canvas-based coherence trend chart | No charting library dependency; simple line chart sufficient for trend visualization | 2026-02-15 |
| Streak calculation from distinctSessionDates | Efficient: single SQL query returns distinct dates; Kotlin LocalDate math for streak logic | 2026-02-15 |
| Material Icons Extended | Required for BarChart, DarkMode, LightMode icons; added to commonMain dependencies | 2026-02-15 |
| Onboarding with HorizontalPager | 3-page swipeable intro; SharedPreferences flag prevents re-showing; Skip + Get Started buttons | 2026-02-15 |
| Re-listen as quick-play cards on session screen | Reuses existing onTagSong flow; converts TopCoherenceSongs to SearchResult for YTM launch | 2026-02-15 |
| Top songs show when totalSongCount >= 3 | Complements cold-start chips (< 3 songs); natural progression from suggestions to personal data | 2026-02-15 |
| Leaderboard as dedicated screen | Separate from session re-listen cards; shows full ranked list with rank badges (top 3 highlighted) | 2026-02-15 |
| AlertDialog for destructive actions | Delete session/all requires user confirmation; prevents accidental data loss | 2026-02-15 |
| About screen with hardcoded version | Simple approach; version could be read from BuildConfig in future | 2026-02-15 |
| Bottom NavigationBar with 4 tabs | Replaced 5 cramped top bar icons; bottomBar passed to each screen's Scaffold | 2026-02-15 |
| About kept in top bar, not bottom nav | About is not a primary destination; info icon + theme toggle in Home top bar | 2026-02-15 |
| Coherence color utility (CoherenceColor.kt) | Green (60%+), amber (30-60%), red (<30%); Material colors work on light/dark | 2026-02-16 |
| Quick stats on HomeScreen from InsightsViewModel | Insights loads in init{} so data is ready immediately; no extra fetch needed | 2026-02-16 |
| Share text summary via ACTION_SEND | Simple text share avoids image generation complexity; includes best song + avg | 2026-02-16 |
| Animated coherence ring (Canvas) | animateFloatAsState + animateColorAsState for smooth 600ms transitions | 2026-02-16 |
| Recording progress bar colored by coherence | LinearProgressIndicator fills over 60s; color matches current coherence level | 2026-02-16 |
| Session celebration messages | Dynamic text based on avg coherence thresholds; colored card with coherenceColor | 2026-02-16 |
| onRefreshInsights on session end | Ensures home dashboard stats update immediately after returning from a session | 2026-02-16 |

---

## Notes

- Polar SDK uses RxJava3; we bridge to Kotlin StateFlows in PolarManager
- Polar BLE SDK's `PolarBleApiCallback` requires implementing `disInformationReceived(String, DisInfo)` and `htsNotificationReceived` in v6.14.0
- `searchForDevice()` Flowable runs indefinitely until disposed -- always show Stop button
- PolarManager is a singleton tied to Activity lifecycle (shutDown in onDestroy when isFinishing)
- JetBrains lifecycle libraries use different versioning than AndroidX (2.8.4 not 2.8.7)
- PhysioNet Subject 000 (53M, healthy) scipy reference values for 5-min segment: RMSSD=52.62ms, MeanHR=61.23bpm, LF=273.53ms², HF=283.39ms²
- Welch PSD params matching scipy: nperseg=256, noverlap=128, window='hann', fs=4.0Hz
- JAVA_HOME needs to be set to Android Studio JBR for Gradle: `C:\Program Files\Android\Android Studio\jbr`
- **IMPORTANT: Always commit and push to the private GitHub repo before finishing a session**
- Remote configured: private GitHub repo HrvXo
- YouTube Music backend at `https://hrvxo-music.fly.dev` — uses base64-encoded OAuth (YTMUSIC_OAUTH_B64 env var)
- oauth.json is gitignored — never commit it
- SessionViewModel uses lambda provider `() -> HrDeviceManager` to get current device manager (avoids coupling to AppModule nullable state)
