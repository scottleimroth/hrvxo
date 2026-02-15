package com.heartsyncradio.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.heartsyncradio.hrv.HrvMetrics
import com.heartsyncradio.model.ConnectionState
import com.heartsyncradio.model.HeartRateData
import com.heartsyncradio.model.PolarDeviceInfo
import com.heartsyncradio.ui.components.DeviceListItem
import com.heartsyncradio.ui.components.HeartRateDisplay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    connectionState: ConnectionState,
    heartRateData: HeartRateData?,
    scannedDevices: List<PolarDeviceInfo>,
    isScanning: Boolean,
    batteryLevel: Int?,
    error: String?,
    permissionsGranted: Boolean,
    hrvMetrics: HrvMetrics?,
    selectedDeviceMode: String?,
    bluetoothEnabled: Boolean = true,
    locationEnabled: Boolean = true,
    onSelectDeviceMode: (String) -> Unit,
    onChangeDeviceMode: () -> Unit,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onConnectDevice: (String) -> Unit,
    onDisconnect: () -> Unit,
    onClearError: () -> Unit,
    onRequestPermissions: () -> Unit,
    onRequestBluetooth: () -> Unit = {},
    onRequestLocation: () -> Unit = {},
    onStartSession: () -> Unit = {},
    isDarkTheme: Boolean = false,
    onToggleDarkTheme: () -> Unit = {},
    onViewAbout: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    quickStatStreak: Int = 0,
    quickStatAvgCoherence: Double = 0.0,
    quickStatTotalSongs: Long = 0
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HrvXo") },
                actions = {
                    IconButton(onClick = onViewAbout) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "About"
                        )
                    }
                    IconButton(onClick = onToggleDarkTheme) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDarkTheme) "Light mode" else "Dark mode"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = bottomBar
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Quick stats dashboard (only shown when user has data)
            if (quickStatTotalSongs > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickStatChip(
                        label = "Streak",
                        value = "${quickStatStreak}d",
                        modifier = Modifier.weight(1f)
                    )
                    QuickStatChip(
                        label = "Avg",
                        value = "${(quickStatAvgCoherence * 100).toInt()}%",
                        valueColor = coherenceColor(quickStatAvgCoherence),
                        modifier = Modifier.weight(1f)
                    )
                    QuickStatChip(
                        label = "Songs",
                        value = "$quickStatTotalSongs",
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Connection status bar
            val connectionCardColor by animateColorAsState(
                targetValue = when (connectionState) {
                    ConnectionState.CONNECTED -> MaterialTheme.colorScheme.primaryContainer
                    ConnectionState.CONNECTING,
                    ConnectionState.DISCONNECTING -> MaterialTheme.colorScheme.secondaryContainer
                    ConnectionState.DISCONNECTED -> MaterialTheme.colorScheme.surfaceVariant
                }
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = connectionCardColor
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Only show dynamic status text
                    when (connectionState) {
                        ConnectionState.CONNECTING -> Text(
                            text = "Connecting...",
                            style = MaterialTheme.typography.labelLarge
                        )
                        ConnectionState.DISCONNECTING -> Text(
                            text = "Disconnecting...",
                            style = MaterialTheme.typography.labelLarge
                        )
                        ConnectionState.CONNECTED -> {
                            Spacer(modifier = Modifier.weight(1f))
                            batteryLevel?.let {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "BAT $it%",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                        ConnectionState.DISCONNECTED -> {
                            // Empty - color speaks for itself
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error banner
            error?.let { errorMsg ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = errorMsg,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        TextButton(onClick = onClearError) {
                            Text("Dismiss")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Main content
            when (connectionState) {
                ConnectionState.CONNECTED -> {
                    HeartRateDisplay(
                        heartRateData = heartRateData,
                        hrvMetrics = hrvMetrics,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onStartSession,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Start Music Session")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = onDisconnect,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Disconnect",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                ConnectionState.CONNECTING,
                ConnectionState.DISCONNECTING -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (connectionState == ConnectionState.CONNECTING)
                                    "Connecting..." else "Disconnecting..."
                            )
                        }
                    }
                }

                ConnectionState.DISCONNECTED -> {
                    if (selectedDeviceMode == null) {
                        // Device type chooser
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Text(
                                    text = "Choose Your Sensor",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(24.dp))

                                Card(
                                    onClick = { onSelectDeviceMode("polar") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "Polar H10",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Uses the Polar SDK for enhanced features",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Card(
                                    onClick = { onSelectDeviceMode("ble") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "Other BLE HR Monitor",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Any Bluetooth heart rate chest strap",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Scan UI with pre-flight readiness checks
                        val allReady = permissionsGranted && bluetoothEnabled && locationEnabled

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (selectedDeviceMode == "polar") "Polar H10" else "BLE HR Monitor",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = onChangeDeviceMode) {
                                Text("Change Device")
                            }
                        }

                        // Readiness warning card
                        if (!allReady) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Required to scan for devices:",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    if (!bluetoothEnabled) {
                                        TextButton(onClick = onRequestBluetooth) {
                                            Text("Enable Bluetooth")
                                        }
                                    }
                                    if (!locationEnabled) {
                                        TextButton(onClick = onRequestLocation) {
                                            Text("Enable Location Services")
                                        }
                                    }
                                    if (!permissionsGranted) {
                                        TextButton(onClick = onRequestPermissions) {
                                            Text("Grant BLE Permissions")
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { if (isScanning) onStopScan() else onStartScan() },
                                enabled = allReady || isScanning
                            ) {
                                if (isScanning) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Stop")
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Scan")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (scannedDevices.isEmpty() && !isScanning) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "No devices nearby",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Tap Scan to search",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            LazyColumn(modifier = Modifier.weight(1f)) {
                                items(
                                    items = scannedDevices,
                                    key = { it.deviceId }
                                ) { device ->
                                    DeviceListItem(
                                        device = device,
                                        onConnect = { onConnectDevice(device.deviceId) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickStatChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color? = null
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor ?: MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
