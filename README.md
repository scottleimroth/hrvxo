# HrvXo

[![Download APK](https://img.shields.io/badge/DOWNLOAD-APK-green?style=for-the-badge)](https://github.com/scottleimroth/hrvxo/releases/latest)

Discover which music puts your heart in sync. HrvXo measures your heart rate variability (HRV) through a Bluetooth chest strap while you listen to music on YouTube Music, scoring each song by how it affects your autonomic nervous system.

---

## Available Platforms

| Platform | How to Get It |
|----------|---------------|
| Android | [Download APK](https://github.com/scottleimroth/hrvxo/releases/latest) — requires BLE heart rate chest strap + YouTube Music |

## Features

### Core
- **BLE Heart Rate Integration** — Polar H10 (via SDK) or any standard BLE HR chest strap
- **HRV Processing** — Artifact correction (cubic spline), RMSSD, FFT spectral analysis
- **Coherence Scoring** — HeartMath algorithm with Welch's method PSD estimation
- **Real-time Display** — Live BPM, coherence %, RMSSD, and RR intervals
- **YouTube Music Integration** — Search songs, auto-detect track changes, generate coherence playlists
- **Movement Detection** — Accelerometer + HR anomaly flagging for reliable readings

### Session & Data
- **Session History** — Browse past sessions, expand to see per-song results, delete individual sessions or all data
- **CSV Export** — Share session data via ShareSheet for external analysis
- **Leaderboard** — All songs ranked by coherence score with listen counts and rank badges
- **Insights Dashboard** — Stats grid, coherence trend chart, best song/artist, streak tracking, total listen time
- **Coherence Playlists** — Auto-generate YouTube Music playlists from your top-performing songs

### UX
- **Bottom Navigation** — Home, Insights, Leaderboard, History tabs for easy navigation
- **Dark Mode** — Teal/cyan themed dark mode with persistent toggle
- **Onboarding** — 3-page swipeable intro for first-time users
- **Cold-Start Suggestions** — Curated search queries for users with zero data
- **Re-Listen Mode** — Quick replay of top coherence songs during active sessions

## Getting Started

### Prerequisites

- Android 8.0+ (API 26)
- BLE heart rate chest strap (Polar H10 recommended)
- Bluetooth enabled on device
- [YouTube Music](https://play.google.com/store/apps/details?id=com.google.android.apps.youtube.music) app installed

### Installation

Download the latest APK from the badge above, or from the [Releases page](https://github.com/scottleimroth/hrvxo/releases).

1. Open the APK on your Android device
2. Allow "Install from unknown sources" when prompted
3. Open the app and complete the onboarding
4. Wet your chest strap, put it on, and connect via Bluetooth
5. Start a music session and tag songs as you listen

### Building from Source

```bash
git clone https://github.com/scottleimroth/hrvxo.git
cd hrvxo
./gradlew composeApp:assembleDebug
```

APK output: `composeApp/build/outputs/apk/debug/composeApp-debug.apk`

## Project Structure

```
hrvxo/
├── composeApp/src/
│   ├── commonMain/         # Cross-platform code
│   │   └── kotlin/com/heartsyncradio/
│   │       ├── hrv/        # HRV processing (artifact detection, FFT, coherence)
│   │       ├── model/      # Data classes (HeartRateData, ConnectionState)
│   │       ├── music/      # Session management, song repository
│   │       ├── ui/         # Compose UI screens and components
│   │       └── db/         # SQLDelight schema and migrations
│   ├── androidMain/        # Android-specific code
│   │   └── kotlin/com/heartsyncradio/
│   │       ├── polar/      # Polar BLE SDK integration
│   │       ├── ble/        # Generic BLE HR manager
│   │       ├── viewmodel/  # Android ViewModels (Home, Session, History, Insights)
│   │       ├── di/         # Manual dependency injection
│   │       └── permission/ # BLE permission handling
│   └── commonTest/         # Cross-platform tests (40 tests)
├── gradle/
│   └── libs.versions.toml  # Dependency version catalog
└── docs/                   # Design documentation
```

## Testing

```bash
./gradlew composeApp:testDebugUnitTest
```

40 unit tests covering HRV processing, FFT accuracy (Parseval's theorem), coherence calculation, and PhysioNet real-world data validation.

## Tech Stack

### Mobile App
- **Kotlin Multiplatform** with Compose Multiplatform
- **Polar BLE SDK** 6.14.0 for heart rate sensor communication
- **SQLDelight** for local song-coherence database
- **Material3** with custom dark/light themes
- **AndroidX Lifecycle** + ViewModels with StateFlow

### Backend
- **Python FastAPI** hosted on Fly.io
- **ytmusicapi** for YouTube Music search and playlist creation

## Roadmap

- [x] BLE heart rate integration (Polar H10 + generic BLE)
- [x] HRV processing with HeartMath coherence scoring
- [x] YouTube Music integration and playlist creation
- [x] Session history, CSV export, leaderboard
- [x] Insights dashboard with streaks and trend charts
- [x] Dark mode, onboarding, bottom navigation
- [ ] Genre/tempo analysis: correlate audio features with coherence
- [ ] iOS support (KMP structure in place)
