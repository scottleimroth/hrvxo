package com.heartsyncradio

import androidx.compose.runtime.Composable
import com.heartsyncradio.ui.theme.HrvXoTheme
import com.heartsyncradio.hrv.HrvMetrics
import com.heartsyncradio.model.ConnectionState
import com.heartsyncradio.model.HeartRateData
import com.heartsyncradio.model.PolarDeviceInfo
import com.heartsyncradio.music.SearchResult
import com.heartsyncradio.music.SessionPhase
import com.heartsyncradio.music.SongSessionResult
import com.heartsyncradio.music.TaggedSong
import com.heartsyncradio.ui.AboutScreen
import com.heartsyncradio.ui.HistoryScreen
import com.heartsyncradio.ui.HistorySongUi
import com.heartsyncradio.ui.HomeScreen
import com.heartsyncradio.ui.InsightsScreen
import com.heartsyncradio.ui.InsightsUi
import com.heartsyncradio.ui.LeaderboardScreen
import com.heartsyncradio.ui.OnboardingScreen
import com.heartsyncradio.ui.SessionScreen
import com.heartsyncradio.ui.SessionSummaryUi
import com.heartsyncradio.ui.TopSongUi

enum class AppScreen { ONBOARDING, HOME, SESSION, HISTORY, INSIGHTS, LEADERBOARD, ABOUT }

@Composable
fun App(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    // Home parameters
    connectionState: ConnectionState,
    heartRateData: HeartRateData?,
    scannedDevices: List<PolarDeviceInfo>,
    isScanning: Boolean,
    batteryLevel: Int?,
    error: String?,
    permissionsGranted: Boolean,
    hrvMetrics: HrvMetrics?,
    selectedDeviceMode: String?,
    onSelectDeviceMode: (String) -> Unit,
    onChangeDeviceMode: () -> Unit,
    bluetoothEnabled: Boolean = true,
    locationEnabled: Boolean = true,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onConnectDevice: (String) -> Unit,
    onDisconnect: () -> Unit,
    onClearError: () -> Unit,
    onRequestPermissions: () -> Unit,
    onRequestBluetooth: () -> Unit = {},
    onRequestLocation: () -> Unit = {},
    // Session parameters
    sessionPhase: SessionPhase = SessionPhase.NOT_STARTED,
    sessionCurrentSong: TaggedSong? = null,
    pendingSong: SearchResult? = null,
    sessionResults: List<SongSessionResult> = emptyList(),
    settleCountdownSec: Int = 0,
    recordingDurationSec: Int = 0,
    searchResults: List<SearchResult> = emptyList(),
    isSearching: Boolean = false,
    searchError: String? = null,
    totalSongCount: Long = 0,
    playlistCreated: String? = null,
    isCreatingPlaylist: Boolean = false,
    isMoving: Boolean = false,
    notificationListenerEnabled: Boolean = false,
    overlayPermissionGranted: Boolean = false,
    onStartSession: () -> Unit = {},
    onEndSession: () -> Unit = {},
    onSearchSongs: (String) -> Unit = {},
    onTagSong: (SearchResult) -> Unit = {},
    onCreatePlaylist: () -> Unit = {},
    onResetSession: () -> Unit = {},
    onClearSearchError: () -> Unit = {},
    onRequestNotificationListener: () -> Unit = {},
    onRequestOverlayPermission: () -> Unit = {},
    topSongs: List<TopSongUi> = emptyList(),
    // History parameters
    historySessions: List<SessionSummaryUi> = emptyList(),
    expandedSessionDates: Set<Long> = emptySet(),
    expandedSessionSongs: Map<Long, List<HistorySongUi>> = emptyMap(),
    isExporting: Boolean = false,
    onToggleSession: (Long) -> Unit = {},
    onExportCsv: () -> Unit = {},
    onDeleteSession: (Long) -> Unit = {},
    onDeleteAllData: () -> Unit = {},
    onViewHistory: () -> Unit = {},
    // Theme
    isDarkTheme: Boolean = false,
    onToggleDarkTheme: () -> Unit = {},
    // Insights
    insights: InsightsUi = InsightsUi(),
    onViewInsights: () -> Unit = {},
    // Leaderboard
    onViewLeaderboard: () -> Unit = {},
    // About
    versionName: String = "",
    onViewAbout: () -> Unit = {},
    // Onboarding
    onOnboardingComplete: () -> Unit = {}
) {
    HrvXoTheme(darkTheme = isDarkTheme) {
        when (currentScreen) {
            AppScreen.ONBOARDING -> OnboardingScreen(
                onComplete = onOnboardingComplete
            )
            AppScreen.HOME -> HomeScreen(
                connectionState = connectionState,
                heartRateData = heartRateData,
                scannedDevices = scannedDevices,
                isScanning = isScanning,
                batteryLevel = batteryLevel,
                error = error,
                permissionsGranted = permissionsGranted,
                hrvMetrics = hrvMetrics,
                selectedDeviceMode = selectedDeviceMode,
                bluetoothEnabled = bluetoothEnabled,
                locationEnabled = locationEnabled,
                onSelectDeviceMode = onSelectDeviceMode,
                onChangeDeviceMode = onChangeDeviceMode,
                onStartScan = onStartScan,
                onStopScan = onStopScan,
                onConnectDevice = onConnectDevice,
                onDisconnect = onDisconnect,
                onClearError = onClearError,
                onRequestPermissions = onRequestPermissions,
                onRequestBluetooth = onRequestBluetooth,
                onRequestLocation = onRequestLocation,
                onStartSession = { onNavigate(AppScreen.SESSION) },
                onViewHistory = onViewHistory,
                isDarkTheme = isDarkTheme,
                onToggleDarkTheme = onToggleDarkTheme,
                onViewInsights = onViewInsights,
                onViewLeaderboard = onViewLeaderboard,
                onViewAbout = onViewAbout
            )
            AppScreen.INSIGHTS -> InsightsScreen(
                insights = insights,
                onBack = { onNavigate(AppScreen.HOME) }
            )
            AppScreen.HISTORY -> HistoryScreen(
                sessions = historySessions,
                expandedSessionDates = expandedSessionDates,
                expandedSessionSongs = expandedSessionSongs,
                isExporting = isExporting,
                onToggleSession = onToggleSession,
                onExportCsv = onExportCsv,
                onDeleteSession = onDeleteSession,
                onDeleteAllData = onDeleteAllData,
                onBack = { onNavigate(AppScreen.HOME) }
            )
            AppScreen.LEADERBOARD -> LeaderboardScreen(
                songs = topSongs,
                onBack = { onNavigate(AppScreen.HOME) }
            )
            AppScreen.ABOUT -> AboutScreen(
                versionName = versionName,
                onBack = { onNavigate(AppScreen.HOME) }
            )
            AppScreen.SESSION -> SessionScreen(
                sessionPhase = sessionPhase,
                currentSong = sessionCurrentSong,
                pendingSong = pendingSong,
                sessionResults = sessionResults,
                settleCountdownSec = settleCountdownSec,
                recordingDurationSec = recordingDurationSec,
                hrvMetrics = hrvMetrics,
                searchResults = searchResults,
                isSearching = isSearching,
                searchError = searchError,
                totalSongCount = totalSongCount,
                playlistCreated = playlistCreated,
                isCreatingPlaylist = isCreatingPlaylist,
                isMoving = isMoving,
                notificationListenerEnabled = notificationListenerEnabled,
                overlayPermissionGranted = overlayPermissionGranted,
                onStartSession = onStartSession,
                onEndSession = onEndSession,
                onSearchSongs = onSearchSongs,
                onTagSong = onTagSong,
                onCreatePlaylist = onCreatePlaylist,
                onResetSession = onResetSession,
                onClearSearchError = onClearSearchError,
                onRequestNotificationListener = onRequestNotificationListener,
                onRequestOverlayPermission = onRequestOverlayPermission,
                topSongs = topSongs,
                onBack = {
                    onResetSession()
                    onNavigate(AppScreen.HOME)
                }
            )
        }
    }
}
