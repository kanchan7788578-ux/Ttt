package com.example.ui.components

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SettingsBluetooth
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.AppCopy
import com.example.model.BluetoothTvDevice
import com.example.model.BondStatus
import com.example.model.DeviceConnectionState
import com.example.model.HidRegistrationState
import com.example.ui.theme.AmberWaiting
import com.example.ui.theme.ConsoleObsidian
import com.example.ui.theme.ConsoleObsidianDeep
import com.example.ui.theme.ConsoleOutline
import com.example.ui.theme.ConsoleSurface
import com.example.ui.theme.ConsoleSurfaceElevated
import com.example.ui.theme.CoralPulse
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.EmeraldConnected
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.GamepadUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionSheet(
    copy: AppCopy,
    uiState: GamepadUiState,
    requiredPermissions: Array<String>,
    onPermissionsUpdated: () -> Unit,
    onConnectDevice: (String) -> Unit,
    onDisconnectDevice: () -> Unit,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onPairDevice: (String) -> Unit,
    onRefresh: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        onPermissionsUpdated()
    }

    val enableBtLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        onRefresh()
    }

    val discoverableLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        onRefresh()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ConsoleSurface,
        contentColor = TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = copy.btSheetTitle,
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(EmeraldConnected.copy(alpha = 0.14f))
                                .border(1.dp, EmeraldConnected.copy(alpha = 0.4f), RoundedCornerShape(50))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WifiOff,
                                contentDescription = null,
                                tint = EmeraldConnected,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = copy.offlineBadge,
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldConnected
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = copy.btSheetSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("close_bt_sheet_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = copy.closeBtn,
                        tint = TextSecondary
                    )
                }
            }

            // State Banner (Permissions / Bluetooth Off / Unsupported / Ready)
            when (uiState.hidState) {
                HidRegistrationState.PERMISSION_REQUIRED -> {
                    StatusActionCard(
                        title = copy.permissionTitle,
                        body = copy.permissionBody,
                        accent = AmberWaiting,
                        buttonText = copy.grantPermissionBtn,
                        testTag = "grant_bt_permission_btn",
                        onClick = {
                            if (requiredPermissions.isNotEmpty()) {
                                permissionLauncher.launch(requiredPermissions)
                            } else {
                                onPermissionsUpdated()
                            }
                        }
                    )
                }
                HidRegistrationState.BLUETOOTH_DISABLED -> {
                    StatusActionCard(
                        title = copy.btOffTitle,
                        body = copy.btOffBody,
                        accent = CrimsonAlert,
                        buttonText = copy.turnOnBtBtn,
                        testTag = "turn_on_bt_btn",
                        onClick = {
                            runCatching {
                                enableBtLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                            }
                        }
                    )
                }
                HidRegistrationState.UNSUPPORTED_OS -> {
                    InfoBanner(text = copy.hidUnsupportedOs, accent = CrimsonAlert)
                }
                HidRegistrationState.UNSUPPORTED_HARDWARE -> {
                    InfoBanner(text = copy.hidUnsupportedHw, accent = AmberWaiting)
                }
                HidRegistrationState.REGISTERING -> {
                    InfoBanner(text = copy.hidRegisteringLabel, accent = CyanGlow)
                }
                HidRegistrationState.REGISTERED, HidRegistrationState.ERROR -> {
                    val accent = if (uiState.hidState == HidRegistrationState.REGISTERED) EmeraldConnected else AmberWaiting
                    val text = if (uiState.hidState == HidRegistrationState.REGISTERED) {
                        copy.hidReadyLabel
                    } else {
                        uiState.errorMessage ?: copy.hidRegisteringLabel
                    }
                    InfoBanner(text = text, accent = accent)
                }
            }

            // Quick Discoverable + System BT Settings Actions
            if (uiState.hidState == HidRegistrationState.REGISTERED ||
                uiState.hidState == HidRegistrationState.REGISTERING ||
                uiState.hidState == HidRegistrationState.ERROR
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            runCatching {
                                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                                    putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120)
                                }
                                discoverableLauncher.launch(intent)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("make_discoverable_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanGlow,
                            contentColor = ConsoleObsidianDeep
                        ),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BluetoothSearching,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = copy.makeDiscoverableBtn,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            runCatching {
                                context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                            }
                        },
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("open_bt_settings_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SettingsBluetooth,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = copy.openBtSettingsBtn,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1
                        )
                    }
                }
            }

            // Paired TVs & Devices
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = copy.pairedTvsHeader,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("refresh_paired_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = CyanGlow
                    )
                }
            }

            if (uiState.pairedDevices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ConsoleObsidian)
                        .border(1.dp, ConsoleOutline, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = copy.noPairedTvsEmpty,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.pairedDevices.forEach { device ->
                        PairedDeviceRow(
                            device = device,
                            copy = copy,
                            onConnect = { onConnectDevice(device.address) },
                            onDisconnect = onDisconnectDevice
                        )
                    }
                }
            }

            // Nearby Device Scanner
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = copy.nearbyDevicesHeader,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                OutlinedButton(
                    onClick = { if (uiState.isScanning) onStopScan() else onStartScan() },
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("scan_nearby_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanGlow)
                ) {
                    if (uiState.isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = CyanGlow
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(copy.scanningLabel, style = MaterialTheme.typography.labelMedium)
                    } else {
                        Icon(Icons.Default.Radar, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(copy.scanNearbyBtn, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            if (uiState.discoveredDevices.isEmpty()) {
                Text(
                    text = copy.noNearbyEmpty,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.discoveredDevices.forEach { device ->
                        DiscoveredDeviceRow(
                            device = device,
                            copy = copy,
                            onPair = { onPairDevice(device.address) }
                        )
                    }
                }
            }

            // 3-Step Offline TV Guide Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ConsoleSurfaceElevated)
                    .border(1.dp, ConsoleOutline, RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = CyanGlow,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = copy.guideTitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = CyanGlow
                    )
                }
                Text(text = copy.guideStep1, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Text(text = copy.guideStep2, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Text(text = copy.guideStep3, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun PairedDeviceRow(
    device: BluetoothTvDevice,
    copy: AppCopy,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    val isConnected = device.connectionState == DeviceConnectionState.CONNECTED
    val isConnecting = device.connectionState == DeviceConnectionState.CONNECTING

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isConnected) EmeraldConnected.copy(alpha = 0.1f) else ConsoleObsidian)
            .border(
                width = 1.dp,
                color = if (isConnected) EmeraldConnected.copy(alpha = 0.6f) else ConsoleOutline,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isConnected) EmeraldConnected.copy(alpha = 0.2f) else ConsoleSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        isConnected -> Icons.Default.BluetoothConnected
                        device.isTvOrMedia -> Icons.Default.Tv
                        else -> Icons.Default.Bluetooth
                    },
                    contentDescription = null,
                    tint = if (isConnected) EmeraldConnected else CyanGlow,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = device.address,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        when {
            isConnected -> {
                OutlinedButton(
                    onClick = onDisconnect,
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("disconnect_tv_btn_${device.address}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralPulse)
                ) {
                    Text(copy.disconnectBtn, style = MaterialTheme.typography.labelMedium)
                }
            }
            isConnecting -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = CyanGlow
                    )
                    Text(copy.connectingLabel, style = MaterialTheme.typography.labelMedium, color = CyanGlow)
                }
            }
            else -> {
                Button(
                    onClick = onConnect,
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("connect_tv_btn_${device.address}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ConsoleSurfaceElevated,
                        contentColor = CyanGlow
                    )
                ) {
                    Text(copy.connectBtn, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun DiscoveredDeviceRow(
    device: BluetoothTvDevice,
    copy: AppCopy,
    onPair: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ConsoleObsidian)
            .border(1.dp, ConsoleOutline, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = if (device.isTvOrMedia) Icons.Default.Tv else Icons.Default.Bluetooth,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = device.address,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }
        Button(
            onClick = onPair,
            enabled = device.bondStatus != BondStatus.BONDING,
            modifier = Modifier
                .height(48.dp)
                .testTag("pair_device_btn_${device.address}"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ConsoleSurfaceElevated,
                contentColor = TextPrimary
            )
        ) {
            Text(
                text = if (device.bondStatus == BondStatus.BONDING) copy.pairingInProgress else copy.pairBtn,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun StatusActionCard(
    title: String,
    body: String,
    accent: Color,
    buttonText: String,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(accent.copy(alpha = 0.12f))
            .border(1.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = accent)
        Text(text = body, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
        Button(
            onClick = onClick,
            modifier = Modifier
                .height(48.dp)
                .testTag(testTag),
            colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = ConsoleObsidianDeep),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = buttonText, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun InfoBanner(text: String, accent: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(accent.copy(alpha = 0.12f))
            .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(accent)
        )
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
    }
}
