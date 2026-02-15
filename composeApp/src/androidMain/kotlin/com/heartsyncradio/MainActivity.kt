package com.heartsyncradio

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.widget.Toast
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import androidx.core.location.LocationManagerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import java.io.File
import com.heartsyncradio.di.AppModule
import com.heartsyncradio.di.DeviceMode
import com.heartsyncradio.music.MusicDetectionService
import com.heartsyncradio.permission.BlePermissionHandler
import com.heartsyncradio.ui.HistorySongUi
import com.heartsyncradio.ui.InsightsUi
import com.heartsyncradio.ui.SessionSummaryUi
import com.heartsyncradio.ui.TopSongUi
import com.heartsyncradio.viewmodel.HistoryViewModel
import com.heartsyncradio.viewmodel.HomeViewModel
import com.heartsyncradio.viewmodel.InsightsViewModel
import com.heartsyncradio.viewmodel.SessionViewModel

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var sessionViewModel: SessionViewModel
    private lateinit var historyViewModel: HistoryViewModel
    private lateinit var insightsViewModel: InsightsViewModel
    private var showOnboarding by mutableStateOf(false)
    private var currentScreen by mutableStateOf(AppScreen.HOME)
    private var notificationListenerEnabled by mutableStateOf(false)
    private var overlayPermissionGranted by mutableStateOf(false)
    private var bluetoothEnabled by mutableStateOf(false)
    private var locationEnabled by mutableStateOf(false)
    private var isDarkTheme by mutableStateOf(false)

    companion object {
        private const val RETURN_CHANNEL_ID = "hrvxo_return"
        private const val RETURN_NOTIFICATION_ID = 42
        private const val AUTO_RETURN_DELAY_MS = 1500L
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        viewModel.onPermissionsResult(allGranted)
        checkReadiness()
    }

    private val bluetoothEnableLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        checkReadiness()
    }

    private val locationSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { _ ->
        checkReadiness()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        createReturnNotificationChannel()

        val prefs = getSharedPreferences("hrvxo_prefs", MODE_PRIVATE)
        isDarkTheme = prefs.getBoolean("dark_theme", false)
        showOnboarding = !prefs.getBoolean("onboarding_complete", false)
        if (showOnboarding) currentScreen = AppScreen.ONBOARDING

        viewModel = ViewModelProvider(
            this,
            AppModule.provideHomeViewModelFactory(this)
        )[HomeViewModel::class.java]

        sessionViewModel = ViewModelProvider(
            this,
            AppModule.provideSessionViewModelFactory(this)
        )[SessionViewModel::class.java]

        historyViewModel = ViewModelProvider(
            this,
            AppModule.provideHistoryViewModelFactory(this)
        )[HistoryViewModel::class.java]

        insightsViewModel = ViewModelProvider(
            this,
            AppModule.provideInsightsViewModelFactory(this)
        )[InsightsViewModel::class.java]

        checkReadiness()
        notificationListenerEnabled = MusicDetectionService.isEnabled(this)
        overlayPermissionGranted = Settings.canDrawOverlays(this)

        setContent {
            // Home state
            val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
            val heartRateData by viewModel.heartRateData.collectAsStateWithLifecycle()
            val scannedDevices by viewModel.scannedDevices.collectAsStateWithLifecycle()
            val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
            val batteryLevel by viewModel.batteryLevel.collectAsStateWithLifecycle()
            val error by viewModel.error.collectAsStateWithLifecycle()
            val permissionsGranted by viewModel.permissionsGranted.collectAsStateWithLifecycle()
            val hrvMetrics by viewModel.hrvMetrics.collectAsStateWithLifecycle()
            val selectedDeviceMode by viewModel.selectedDeviceMode.collectAsStateWithLifecycle()

            // Session state
            val sessionPhase by sessionViewModel.sessionPhase.collectAsStateWithLifecycle()
            val sessionCurrentSong by sessionViewModel.currentSong.collectAsStateWithLifecycle()
            val pendingSong by sessionViewModel.pendingSong.collectAsStateWithLifecycle()
            val sessionResults by sessionViewModel.sessionResults.collectAsStateWithLifecycle()
            val settleCountdownSec by sessionViewModel.settleCountdownSec.collectAsStateWithLifecycle()
            val recordingDurationSec by sessionViewModel.recordingDurationSec.collectAsStateWithLifecycle()
            val searchResults by sessionViewModel.searchResults.collectAsStateWithLifecycle()
            val isSearching by sessionViewModel.isSearching.collectAsStateWithLifecycle()
            val searchError by sessionViewModel.searchError.collectAsStateWithLifecycle()
            val totalSongCount by sessionViewModel.totalSongCount.collectAsStateWithLifecycle()
            val playlistCreated by sessionViewModel.playlistCreated.collectAsStateWithLifecycle()
            val isCreatingPlaylist by sessionViewModel.isCreatingPlaylist.collectAsStateWithLifecycle()
            val isMoving by sessionViewModel.isMoving.collectAsStateWithLifecycle()
            val topSongsDb by sessionViewModel.topSongs.collectAsStateWithLifecycle()
            val topSongsUi = topSongsDb.map { s ->
                TopSongUi(
                    videoId = s.video_id,
                    title = s.title,
                    artist = s.artist,
                    score = s.score ?: 0.0,
                    listenCount = s.listen_count
                )
            }

            // Insights state
            val insightsState by insightsViewModel.state.collectAsStateWithLifecycle()
            val insightsUi = InsightsUi(
                totalListens = insightsState.totalListens,
                uniqueSongs = insightsState.uniqueSongs,
                totalSessions = insightsState.totalSessions,
                overallAvgCoherence = insightsState.overallAvgCoherence,
                allTimeBestCoherence = insightsState.allTimeBestCoherence,
                totalListenMinutes = insightsState.totalListenTimeSec / 60,
                bestSongTitle = insightsState.bestSongTitle,
                bestSongArtist = insightsState.bestSongArtist,
                topArtist = insightsState.topArtist,
                topArtistCoherence = insightsState.topArtistCoherence,
                topArtistListenCount = insightsState.topArtistListenCount,
                trendCoherences = insightsState.coherenceTrend.map { it.avgCoherence },
                currentStreak = insightsState.currentStreak,
                longestStreak = insightsState.longestStreak,
                isLoaded = insightsState.isLoaded
            )

            // History state
            val historySessions by historyViewModel.sessions.collectAsStateWithLifecycle()
            val historyExpandedSongs by historyViewModel.expandedSessionSongs.collectAsStateWithLifecycle()
            val isExporting by historyViewModel.isExporting.collectAsStateWithLifecycle()

            // Map DB types to UI models
            val historySessionsUi = historySessions.map { s ->
                SessionSummaryUi(
                    sessionDate = s.session_date,
                    formattedDate = HistoryViewModel.formatDate(s.session_date),
                    songCount = s.song_count,
                    avgCoherence = s.avg_coh ?: 0.0,
                    bestCoherence = s.best_coh ?: 0.0,
                    bestTitle = s.best_title,
                    bestArtist = s.best_artist
                )
            }
            val historyExpandedSongsUi = historyExpandedSongs.mapValues { (_, songs) ->
                songs.map { song ->
                    HistorySongUi(
                        title = song.title,
                        artist = song.artist,
                        avgCoherence = song.avg_coherence,
                        avgRmssd = song.avg_rmssd,
                        meanHr = song.mean_hr,
                        durationSec = song.duration_listened_sec,
                        movementDetected = song.movement_detected != 0L
                    )
                }
            }

            App(
                currentScreen = currentScreen,
                onNavigate = { currentScreen = it },
                // Home parameters
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
                onSelectDeviceMode = { mode ->
                    val deviceMode = when (mode) {
                        "polar" -> DeviceMode.POLAR
                        else -> DeviceMode.GENERIC_BLE
                    }
                    val manager = AppModule.switchMode(this, deviceMode)
                    viewModel.switchManager(manager, mode)
                },
                onChangeDeviceMode = {
                    viewModel.clearDeviceSelection()
                },
                onStartScan = viewModel::startScan,
                onStopScan = viewModel::stopScan,
                onConnectDevice = viewModel::connectToDevice,
                onDisconnect = viewModel::disconnectFromDevice,
                onClearError = viewModel::clearError,
                onRequestPermissions = {
                    permissionLauncher.launch(
                        BlePermissionHandler.requiredPermissions().toTypedArray()
                    )
                },
                onRequestBluetooth = ::requestBluetoothEnable,
                onRequestLocation = ::requestLocationEnable,
                // Session parameters
                sessionPhase = sessionPhase,
                sessionCurrentSong = sessionCurrentSong,
                pendingSong = pendingSong,
                sessionResults = sessionResults,
                settleCountdownSec = settleCountdownSec,
                recordingDurationSec = recordingDurationSec,
                searchResults = searchResults,
                isSearching = isSearching,
                searchError = searchError,
                totalSongCount = totalSongCount,
                playlistCreated = playlistCreated,
                isCreatingPlaylist = isCreatingPlaylist,
                isMoving = isMoving,
                notificationListenerEnabled = notificationListenerEnabled,
                overlayPermissionGranted = overlayPermissionGranted,
                onStartSession = sessionViewModel::startSession,
                onEndSession = sessionViewModel::endSession,
                onSearchSongs = sessionViewModel::searchSongs,
                onTagSong = { result ->
                    sessionViewModel.selectSong(result)
                    // Target YTM directly to avoid Chrome resolver on first launch
                    val ytmIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://music.youtube.com/watch?v=${result.videoId}")).apply {
                        setPackage("com.google.android.apps.youtube.music")
                    }
                    try {
                        startActivity(ytmIntent)
                    } catch (_: Exception) {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://music.youtube.com/watch?v=${result.videoId}")))
                    }
                    showReturnNotification()
                    if (Settings.canDrawOverlays(this)) {
                        // SYSTEM_ALERT_WINDOW exempts us from background activity restrictions
                        Handler(Looper.getMainLooper()).postDelayed({
                            val returnIntent = Intent(this, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or
                                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                            }
                            startActivity(returnIntent)
                        }, AUTO_RETURN_DELAY_MS)
                    }
                },
                onCreatePlaylist = sessionViewModel::createPlaylist,
                onResetSession = sessionViewModel::resetSession,
                onClearSearchError = sessionViewModel::clearSearchError,
                onRequestNotificationListener = {
                    startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                },
                onRequestOverlayPermission = {
                    startActivity(
                        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                    )
                },
                topSongs = topSongsUi,
                onShareResults = { text ->
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, text)
                    }
                    startActivity(Intent.createChooser(intent, "Share Session Results"))
                },
                // History parameters
                historySessions = historySessionsUi,
                expandedSessionDates = historyExpandedSongsUi.keys,
                expandedSessionSongs = historyExpandedSongsUi,
                isExporting = isExporting,
                onToggleSession = historyViewModel::toggleSession,
                onExportCsv = ::exportCsv,
                onDeleteSession = historyViewModel::deleteSession,
                onDeleteAllData = historyViewModel::deleteAllData,
                onViewHistory = { currentScreen = AppScreen.HISTORY },
                // Theme
                isDarkTheme = isDarkTheme,
                onToggleDarkTheme = {
                    isDarkTheme = !isDarkTheme
                    prefs.edit().putBoolean("dark_theme", isDarkTheme).apply()
                },
                // Insights
                insights = insightsUi,
                onViewInsights = {
                    insightsViewModel.refresh()
                    currentScreen = AppScreen.INSIGHTS
                },
                onRefreshInsights = { insightsViewModel.refresh() },
                // Quick stats for home
                quickStatStreak = insightsUi.currentStreak,
                quickStatAvgCoherence = insightsUi.overallAvgCoherence,
                quickStatTotalSongs = insightsUi.uniqueSongs,
                // Leaderboard
                onViewLeaderboard = { currentScreen = AppScreen.LEADERBOARD },
                // About
                versionName = "1.7.0",
                onViewAbout = { currentScreen = AppScreen.ABOUT },
                // Onboarding
                onOnboardingComplete = {
                    prefs.edit().putBoolean("onboarding_complete", true).apply()
                    currentScreen = AppScreen.HOME
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        cancelReturnNotification()
        checkReadiness()
        notificationListenerEnabled = MusicDetectionService.isEnabled(this)
        overlayPermissionGranted = Settings.canDrawOverlays(this)
    }

    private fun checkReadiness() {
        val btManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothEnabled = btManager.adapter?.isEnabled == true
        locationEnabled = isLocationEnabled()
        viewModel.onPermissionsResult(BlePermissionHandler.hasAllPermissions(this))
    }

    private fun isLocationEnabled(): Boolean {
        val lm = getSystemService(LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(lm)
    }

    @SuppressLint("MissingPermission")
    private fun requestBluetoothEnable() {
        try {
            bluetoothEnableLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        } catch (_: SecurityException) {
            // Android 12+ needs BLUETOOTH_CONNECT — request permissions first
            permissionLauncher.launch(BlePermissionHandler.requiredPermissions().toTypedArray())
        }
    }

    private fun requestLocationEnable() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_LOW_POWER, 10000L
        ).build()
        val settingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()
        LocationServices.getSettingsClient(this)
            .checkLocationSettings(settingsRequest)
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    locationSettingsLauncher.launch(
                        IntentSenderRequest.Builder(exception.resolution.intentSender).build()
                    )
                }
            }
    }

    private fun createReturnNotificationChannel() {
        val channel = NotificationChannel(
            RETURN_CHANNEL_ID,
            "Session Return",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Tap to return to HrvXo during a music session"
        }
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }

    private fun showReturnNotification() {
        val returnIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, returnIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, RETURN_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("HrvXo session active")
            .setContentText("Tap to return to HrvXo")
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(RETURN_NOTIFICATION_ID, notification)
    }

    private fun cancelReturnNotification() {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(RETURN_NOTIFICATION_ID)
    }

    private val exportScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private fun exportCsv() {
        exportScope.launch {
            try {
                val csv = historyViewModel.generateCsvExport()
                val file = File(cacheDir, "hrvxo_export.csv")
                file.writeText(csv)
                val uri = FileProvider.getUriForFile(this@MainActivity, "$packageName.provider", file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(intent, "Export HrvXo Data"))
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Export failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            AppModule.shutDown()
        }
    }
}
