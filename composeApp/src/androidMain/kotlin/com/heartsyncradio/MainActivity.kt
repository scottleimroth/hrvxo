package com.heartsyncradio

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
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
import androidx.core.location.LocationManagerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.heartsyncradio.di.AppModule
import com.heartsyncradio.di.DeviceMode
import com.heartsyncradio.music.MusicDetectionService
import com.heartsyncradio.permission.BlePermissionHandler
import com.heartsyncradio.viewmodel.HomeViewModel
import com.heartsyncradio.viewmodel.SessionViewModel

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var sessionViewModel: SessionViewModel
    private var currentScreen by mutableStateOf(AppScreen.HOME)
    private var notificationListenerEnabled by mutableStateOf(false)
    private var overlayPermissionGranted by mutableStateOf(false)
    private var bluetoothEnabled by mutableStateOf(false)
    private var locationEnabled by mutableStateOf(false)

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

        viewModel = ViewModelProvider(
            this,
            AppModule.provideHomeViewModelFactory(this)
        )[HomeViewModel::class.java]

        sessionViewModel = ViewModelProvider(
            this,
            AppModule.provideSessionViewModelFactory(this)
        )[SessionViewModel::class.java]

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

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            AppModule.shutDown()
        }
    }
}
