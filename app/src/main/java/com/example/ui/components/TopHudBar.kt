package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AppCopy
import com.example.model.AppLanguage
import com.example.model.BluetoothTvDevice
import com.example.model.ControllerLayoutMode
import com.example.model.HidRegistrationState
import com.example.ui.theme.AmberWaiting
import com.example.ui.theme.ConsoleOutline
import com.example.ui.theme.ConsoleSurface
import com.example.ui.theme.ConsoleSurfaceElevated
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.EmeraldConnected
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TopHudBar(
    copy: AppCopy,
    language: AppLanguage,
    hidState: HidRegistrationState,
    connectedDevice: BluetoothTvDevice?,
    layoutMode: ControllerLayoutMode,
    isLandscape: Boolean,
    onOpenBluetoothSheet: () -> Unit,
    onSelectLayoutMode: (ControllerLayoutMode) -> Unit,
    onToggleOrientation: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBn = language == AppLanguage.BENGALI
    val isConnected = connectedDevice != null

    val pulseTransition = rememberInfiniteTransition(label = "bt_pulse")
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val statusColor by animateColorAsState(
        targetValue = when {
            isConnected -> EmeraldConnected
            hidState == HidRegistrationState.BLUETOOTH_DISABLED ||
                hidState == HidRegistrationState.PERMISSION_REQUIRED -> CrimsonAlert
            else -> AmberWaiting
        },
        label = "status_color"
    )

    if (isLandscape) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BtStatusPill(
                copy = copy,
                connectedDevice = connectedDevice,
                hidState = hidState,
                statusColor = statusColor,
                pulseAlpha = pulseAlpha,
                onClick = onOpenBluetoothSheet,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(modifier = Modifier.width(8.dp))
            ModeSwitcherRow(
                copy = copy,
                isBn = isBn,
                layoutMode = layoutMode,
                onSelectLayoutMode = onSelectLayoutMode
            )
            Spacer(modifier = Modifier.width(8.dp))
            ActionIconsRow(
                onToggleOrientation = onToggleOrientation,
                onOpenSettings = onOpenSettings
            )
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BtStatusPill(
                    copy = copy,
                    connectedDevice = connectedDevice,
                    hidState = hidState,
                    statusColor = statusColor,
                    pulseAlpha = pulseAlpha,
                    onClick = onOpenBluetoothSheet,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                ActionIconsRow(
                    onToggleOrientation = onToggleOrientation,
                    onOpenSettings = onOpenSettings
                )
            }
            ModeSwitcherRow(
                copy = copy,
                isBn = isBn,
                layoutMode = layoutMode,
                onSelectLayoutMode = onSelectLayoutMode,
                equalWeight = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun BtStatusPill(
    copy: AppCopy,
    connectedDevice: BluetoothTvDevice?,
    hidState: HidRegistrationState,
    statusColor: Color,
    pulseAlpha: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isConnected = connectedDevice != null
    Row(
        modifier = modifier
            .height(48.dp)
            .testTag("bt_status_pill")
            .clip(RoundedCornerShape(50))
            .background(ConsoleSurface)
            .border(1.2.dp, statusColor.copy(alpha = 0.55f), RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .alpha(if (isConnected) 1f else pulseAlpha)
                .clip(CircleShape)
                .background(statusColor)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = when {
                isConnected -> Icons.Default.BluetoothConnected
                hidState == HidRegistrationState.BLUETOOTH_DISABLED -> Icons.Default.BluetoothDisabled
                else -> Icons.Default.Bluetooth
            },
            contentDescription = stringResource(R.string.cd_bt_settings),
            tint = statusColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (isConnected) {
                "${copy.connectedPrefix} ${connectedDevice?.name}"
            } else {
                copy.notConnectedLabel
            },
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ModeSwitcherRow(
    copy: AppCopy,
    isBn: Boolean,
    layoutMode: ControllerLayoutMode,
    onSelectLayoutMode: (ControllerLayoutMode) -> Unit,
    equalWeight: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(ConsoleSurface)
            .border(1.dp, ConsoleOutline, RoundedCornerShape(50))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ControllerLayoutMode.entries.forEach { mode ->
            val selected = mode == layoutMode
            Box(
                modifier = Modifier
                    .then(if (equalWeight) Modifier.weight(1f) else Modifier)
                    .height(40.dp)
                    .testTag("mode_chip_${mode.name.lowercase()}")
                    .clip(RoundedCornerShape(50))
                    .background(if (selected) CyanGlow.copy(alpha = 0.2f) else ConsoleSurface)
                    .border(
                        width = if (selected) 1.dp else 0.dp,
                        color = if (selected) CyanGlow else ConsoleSurface,
                        shape = RoundedCornerShape(50)
                    )
                    .clickable { onSelectLayoutMode(mode) }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = copy.layoutModeName(mode, isBn),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) CyanGlow else TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ActionIconsRow(
    onToggleOrientation: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        IconButton(
            onClick = onToggleOrientation,
            modifier = Modifier
                .size(48.dp)
                .testTag("btn_rotate_orientation")
                .clip(CircleShape)
                .background(ConsoleSurfaceElevated)
        ) {
            Icon(
                imageVector = Icons.Default.ScreenRotation,
                contentDescription = stringResource(R.string.cd_rotate_orientation),
                tint = TextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier
                .size(48.dp)
                .testTag("settings_button")
                .clip(CircleShape)
                .background(ConsoleSurfaceElevated)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = stringResource(R.string.cd_controller_settings),
                tint = TextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
