package com.example.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppCopy
import com.example.data.StringsProvider
import com.example.model.ControllerLayoutMode
import com.example.model.GamepadButton
import com.example.model.OrientationPreference
import com.example.ui.components.AnalogJoystick
import com.example.ui.components.CenterSystemCluster
import com.example.ui.components.ConnectionSheet
import com.example.ui.components.CrossDPad
import com.example.ui.components.DiamondFaceButtons
import com.example.ui.components.SettingsSheet
import com.example.ui.components.TactileFaceButton
import com.example.ui.components.TopHudBar
import com.example.ui.components.TriggerShoulderButton
import com.example.ui.components.TvRemoteActionStrip
import com.example.ui.theme.ButtonGreenA
import com.example.ui.theme.ButtonRedB
import com.example.ui.theme.ConsoleObsidian
import com.example.ui.theme.ConsoleObsidianDeep
import com.example.ui.theme.ConsoleOutline
import com.example.ui.theme.ConsoleSurface
import com.example.ui.theme.ConsoleSurfaceElevated
import com.example.ui.theme.CoralPulse
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.EmeraldConnected
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.GamepadUiState
import com.example.viewmodel.GamepadViewModel

@Composable
fun GamepadScreen(viewModel: GamepadViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val copy = remember(uiState.settings.language) {
        StringsProvider.forLanguage(uiState.settings.language)
    }

    var showBluetoothSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    // Refresh Bluetooth state whenever returning to foreground
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onPermissionResultOrResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Keep screen on during gameplay if enabled
    LaunchedEffect(uiState.settings.keepScreenOn, activity) {
        val window = activity?.window ?: return@LaunchedEffect
        if (uiState.settings.keepScreenOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Apply requested orientation preference
    LaunchedEffect(uiState.settings.orientationPreference, activity) {
        activity?.requestedOrientation = when (uiState.settings.orientationPreference) {
            OrientationPreference.AUTO -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            OrientationPreference.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            OrientationPreference.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    val cyanGlow = CyanGlow
    val coralGlow = CoralPulse
    val bgDeep = ConsoleObsidianDeep
    val bgBase = ConsoleObsidian

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(colors = listOf(bgBase, bgDeep))
            )
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(cyanGlow.copy(alpha = 0.08f), bgBase.copy(alpha = 0f)),
                        center = Offset(size.width * 0.18f, size.height * 0.55f),
                        radius = size.maxDimension * 0.45f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(coralGlow.copy(alpha = 0.07f), bgBase.copy(alpha = 0f)),
                        center = Offset(size.width * 0.82f, size.height * 0.55f),
                        radius = size.maxDimension * 0.45f
                    )
                )
            }
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        val containerWidth = maxWidth
        val containerHeight = maxHeight
        val isLandscape = containerWidth > containerHeight

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopHudBar(
                copy = copy,
                language = uiState.settings.language,
                hidState = uiState.hidState,
                connectedDevice = uiState.connectedDevice,
                layoutMode = uiState.settings.layoutMode,
                isLandscape = isLandscape,
                onOpenBluetoothSheet = { showBluetoothSheet = true },
                onSelectLayoutMode = viewModel::setLayoutMode,
                onToggleOrientation = {
                    val nextPref = if (isLandscape) {
                        OrientationPreference.PORTRAIT
                    } else {
                        OrientationPreference.LANDSCAPE
                    }
                    viewModel.setOrientationPreference(nextPref)
                },
                onOpenSettings = { showSettingsSheet = true }
            )

            if (isLandscape) {
                LandscapeControllerContent(
                    uiState = uiState,
                    copy = copy,
                    availableWidth = containerWidth,
                    availableHeight = containerHeight - 60.dp,
                    viewModel = viewModel,
                    onOpenBluetoothSheet = { showBluetoothSheet = true },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            } else {
                PortraitControllerContent(
                    uiState = uiState,
                    copy = copy,
                    availableWidth = containerWidth,
                    availableHeight = containerHeight - 110.dp,
                    viewModel = viewModel,
                    onOpenBluetoothSheet = { showBluetoothSheet = true },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            }
        }
    }

    if (showBluetoothSheet) {
        ConnectionSheet(
            copy = copy,
            uiState = uiState,
            requiredPermissions = viewModel.bluetoothController.requiredRuntimePermissions(),
            onPermissionsUpdated = viewModel::onPermissionResultOrResume,
            onConnectDevice = viewModel::connectToDevice,
            onDisconnectDevice = viewModel::disconnectDevice,
            onStartScan = viewModel::startDiscovery,
            onStopScan = viewModel::stopDiscovery,
            onPairDevice = viewModel::pairDevice,
            onRefresh = viewModel::refreshDevices,
            onDismiss = { showBluetoothSheet = false }
        )
    }

    if (showSettingsSheet) {
        SettingsSheet(
            copy = copy,
            uiState = uiState,
            onSelectHaptic = viewModel::setHapticStrength,
            onDeadzoneChanged = viewModel::setDeadzonePercent,
            onKeepScreenOnChanged = viewModel::setKeepScreenOn,
            onSelectOrientation = viewModel::setOrientationPreference,
            onSelectLanguage = viewModel::setLanguage,
            onDismiss = { showSettingsSheet = false }
        )
    }
}

@Composable
private fun LandscapeControllerContent(
    uiState: GamepadUiState,
    copy: AppCopy,
    availableWidth: Dp,
    availableHeight: Dp,
    viewModel: GamepadViewModel,
    onOpenBluetoothSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val compactHeight = availableHeight < 320.dp
    val dpadSize = (availableHeight * 0.48f).coerceIn(112.dp, 164.dp)
    val stickSize = (availableHeight * 0.42f).coerceIn(96.dp, 142.dp)
    val faceBtnSize = (availableHeight * 0.19f).coerceIn(48.dp, 64.dp)

    Column(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Shoulder Triggers + Telemetry Strip
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.widthIn(max = 220.dp).weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TriggerShoulderButton(
                    button = GamepadButton.L2,
                    isPressed = uiState.inputState.isPressed(GamepadButton.L2),
                    testTag = "btn_l2",
                    isPrimaryTrigger = true,
                    onStateChange = { viewModel.onButtonChanged(GamepadButton.L2, it) },
                    modifier = Modifier.weight(1f)
                )
                TriggerShoulderButton(
                    button = GamepadButton.L1,
                    isPressed = uiState.inputState.isPressed(GamepadButton.L1),
                    testTag = "btn_l1",
                    onStateChange = { viewModel.onButtonChanged(GamepadButton.L1, it) },
                    modifier = Modifier.weight(1f)
                )
            }

            TelemetryStatusPill(
                uiState = uiState,
                copy = copy,
                onTapConnect = onOpenBluetoothSheet,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Row(
                modifier = Modifier.widthIn(max = 220.dp).weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TriggerShoulderButton(
                    button = GamepadButton.R1,
                    isPressed = uiState.inputState.isPressed(GamepadButton.R1),
                    testTag = "btn_r1",
                    onStateChange = { viewModel.onButtonChanged(GamepadButton.R1, it) },
                    modifier = Modifier.weight(1f)
                )
                TriggerShoulderButton(
                    button = GamepadButton.R2,
                    isPressed = uiState.inputState.isPressed(GamepadButton.R2),
                    testTag = "btn_r2",
                    isPrimaryTrigger = true,
                    onStateChange = { viewModel.onButtonChanged(GamepadButton.R2, it) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Main Ergonomic Thumb Zone
        when (uiState.settings.layoutMode) {
            ControllerLayoutMode.PRO_DUAL_STICK -> {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left Side: D-Pad + Left Analog Stick
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CrossDPad(
                            currentDirection = uiState.inputState.dPad,
                            size = dpadSize,
                            onDirectionChanged = viewModel::onDPadChanged
                        )
                        AnalogJoystick(
                            label = "L",
                            deadzonePercent = uiState.settings.deadzonePercent,
                            size = stickSize,
                            testTag = "left_analog_stick",
                            onStickMoved = viewModel::onLeftStickChanged,
                            modifier = Modifier.padding(top = if (compactHeight) 14.dp else 28.dp)
                        )
                    }

                    // Center System Bridge
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(horizontal = 6.dp)
                    ) {
                        CenterSystemCluster(
                            inputState = uiState.inputState,
                            onButtonChanged = viewModel::onButtonChanged,
                            compact = true
                        )
                        if (!compactHeight && availableWidth > 680.dp) {
                            TvRemoteActionStrip(
                                copy = copy,
                                onKeyTap = viewModel::onTvRemoteKeyTap,
                                modifier = Modifier.widthIn(max = 310.dp)
                            )
                        }
                    }

                    // Right Side: Right Analog Stick + Diamond ABXY
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        AnalogJoystick(
                            label = "R",
                            deadzonePercent = uiState.settings.deadzonePercent,
                            size = stickSize,
                            testTag = "right_analog_stick",
                            onStickMoved = viewModel::onRightStickChanged,
                            modifier = Modifier.padding(top = if (compactHeight) 14.dp else 28.dp)
                        )
                        DiamondFaceButtons(
                            inputState = uiState.inputState,
                            buttonSize = faceBtnSize,
                            onButtonChanged = viewModel::onButtonChanged
                        )
                    }
                }
            }

            ControllerLayoutMode.RETRO_ARCADE -> {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CrossDPad(
                        currentDirection = uiState.inputState.dPad,
                        size = (dpadSize * 1.18f).coerceAtMost(185.dp),
                        onDirectionChanged = viewModel::onDPadChanged
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        CenterSystemCluster(
                            inputState = uiState.inputState,
                            onButtonChanged = viewModel::onButtonChanged
                        )
                        TvRemoteActionStrip(
                            copy = copy,
                            onKeyTap = viewModel::onTvRemoteKeyTap,
                            modifier = Modifier.widthIn(max = 340.dp)
                        )
                    }

                    DiamondFaceButtons(
                        inputState = uiState.inputState,
                        buttonSize = (faceBtnSize * 1.12f).coerceAtMost(72.dp),
                        onButtonChanged = viewModel::onButtonChanged
                    )
                }
            }

            ControllerLayoutMode.TV_REMOTE -> {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CrossDPad(
                        currentDirection = uiState.inputState.dPad,
                        size = (dpadSize * 1.18f).coerceAtMost(185.dp),
                        onDirectionChanged = viewModel::onDPadChanged
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = copy.tvRemoteLabel,
                            style = MaterialTheme.typography.titleSmall,
                            color = CyanGlow
                        )
                        TvRemoteActionStrip(
                            copy = copy,
                            onKeyTap = viewModel::onTvRemoteKeyTap,
                            modifier = Modifier.fillMaxWidth()
                        )
                        CenterSystemCluster(
                            inputState = uiState.inputState,
                            onButtonChanged = viewModel::onButtonChanged
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TactileFaceButton(
                            button = GamepadButton.B,
                            accentColor = ButtonRedB,
                            isPressed = uiState.inputState.isPressed(GamepadButton.B),
                            size = 72.dp,
                            testTag = "btn_b",
                            onStateChange = { viewModel.onButtonChanged(GamepadButton.B, it) }
                        )
                        TactileFaceButton(
                            button = GamepadButton.A,
                            accentColor = ButtonGreenA,
                            isPressed = uiState.inputState.isPressed(GamepadButton.A),
                            size = 82.dp,
                            testTag = "btn_a",
                            onStateChange = { viewModel.onButtonChanged(GamepadButton.A, it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PortraitControllerContent(
    uiState: GamepadUiState,
    copy: AppCopy,
    availableWidth: Dp,
    availableHeight: Dp,
    viewModel: GamepadViewModel,
    onOpenBluetoothSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentMaxWidth = availableWidth.coerceAtMost(560.dp)
    val dpadSize = (contentMaxWidth * 0.38f).coerceIn(124.dp, 168.dp)
    val faceBtnSize = (contentMaxWidth * 0.145f).coerceIn(48.dp, 62.dp)
    val stickSize = (contentMaxWidth * 0.34f).coerceIn(108.dp, 148.dp)

    Column(
        modifier = modifier
            .widthIn(max = 580.dp)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Offline Bluetooth Connection Card / TV Quick Bar
        if (uiState.connectedDevice == null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ConsoleSurface)
                    .border(1.dp, ConsoleOutline, RoundedCornerShape(16.dp))
                    .clickable(onClick = onOpenBluetoothSheet)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(CyanGlow.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BluetoothSearching,
                            contentDescription = null,
                            tint = CyanGlow,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = copy.tapToConnectTv,
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
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
                }
            }
        } else {
            TelemetryStatusPill(
                uiState = uiState,
                copy = copy,
                onTapConnect = onOpenBluetoothSheet,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Shoulder Triggers Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TriggerShoulderButton(
                button = GamepadButton.L2,
                isPressed = uiState.inputState.isPressed(GamepadButton.L2),
                testTag = "btn_l2",
                isPrimaryTrigger = true,
                onStateChange = { viewModel.onButtonChanged(GamepadButton.L2, it) },
                modifier = Modifier.weight(1f)
            )
            TriggerShoulderButton(
                button = GamepadButton.L1,
                isPressed = uiState.inputState.isPressed(GamepadButton.L1),
                testTag = "btn_l1",
                onStateChange = { viewModel.onButtonChanged(GamepadButton.L1, it) },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(6.dp))
            TriggerShoulderButton(
                button = GamepadButton.R1,
                isPressed = uiState.inputState.isPressed(GamepadButton.R1),
                testTag = "btn_r1",
                onStateChange = { viewModel.onButtonChanged(GamepadButton.R1, it) },
                modifier = Modifier.weight(1f)
            )
            TriggerShoulderButton(
                button = GamepadButton.R2,
                isPressed = uiState.inputState.isPressed(GamepadButton.R2),
                testTag = "btn_r2",
                isPrimaryTrigger = true,
                onStateChange = { viewModel.onButtonChanged(GamepadButton.R2, it) },
                modifier = Modifier.weight(1f)
            )
        }

        // Main Controls
        when (uiState.settings.layoutMode) {
            ControllerLayoutMode.PRO_DUAL_STICK -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CrossDPad(
                        currentDirection = uiState.inputState.dPad,
                        size = dpadSize,
                        onDirectionChanged = viewModel::onDPadChanged
                    )
                    DiamondFaceButtons(
                        inputState = uiState.inputState,
                        buttonSize = faceBtnSize,
                        onButtonChanged = viewModel::onButtonChanged
                    )
                }

                CenterSystemCluster(
                    inputState = uiState.inputState,
                    onButtonChanged = viewModel::onButtonChanged
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AnalogJoystick(
                        label = "L",
                        deadzonePercent = uiState.settings.deadzonePercent,
                        size = stickSize,
                        testTag = "left_analog_stick",
                        onStickMoved = viewModel::onLeftStickChanged
                    )
                    AnalogJoystick(
                        label = "R",
                        deadzonePercent = uiState.settings.deadzonePercent,
                        size = stickSize,
                        testTag = "right_analog_stick",
                        onStickMoved = viewModel::onRightStickChanged
                    )
                }
            }

            ControllerLayoutMode.RETRO_ARCADE -> {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CrossDPad(
                        currentDirection = uiState.inputState.dPad,
                        size = (dpadSize * 1.1f).coerceAtMost(180.dp),
                        onDirectionChanged = viewModel::onDPadChanged
                    )
                    DiamondFaceButtons(
                        inputState = uiState.inputState,
                        buttonSize = (faceBtnSize * 1.1f).coerceAtMost(68.dp),
                        onButtonChanged = viewModel::onButtonChanged
                    )
                }

                CenterSystemCluster(
                    inputState = uiState.inputState,
                    onButtonChanged = viewModel::onButtonChanged
                )

                TvRemoteActionStrip(
                    copy = copy,
                    onKeyTap = viewModel::onTvRemoteKeyTap,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            ControllerLayoutMode.TV_REMOTE -> {
                TvRemoteActionStrip(
                    copy = copy,
                    onKeyTap = viewModel::onTvRemoteKeyTap,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    CrossDPad(
                        currentDirection = uiState.inputState.dPad,
                        size = (dpadSize * 1.15f).coerceAtMost(190.dp),
                        onDirectionChanged = viewModel::onDPadChanged
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TactileFaceButton(
                            button = GamepadButton.A,
                            accentColor = ButtonGreenA,
                            isPressed = uiState.inputState.isPressed(GamepadButton.A),
                            size = 76.dp,
                            testTag = "btn_a",
                            onStateChange = { viewModel.onButtonChanged(GamepadButton.A, it) }
                        )
                        TactileFaceButton(
                            button = GamepadButton.B,
                            accentColor = ButtonRedB,
                            isPressed = uiState.inputState.isPressed(GamepadButton.B),
                            size = 66.dp,
                            testTag = "btn_b",
                            onStateChange = { viewModel.onButtonChanged(GamepadButton.B, it) }
                        )
                    }
                }

                CenterSystemCluster(
                    inputState = uiState.inputState,
                    onButtonChanged = viewModel::onButtonChanged
                )
            }
        }

        // Bottom live telemetry bar
        if (availableHeight > 450.dp) {
            TelemetryFooterBar(uiState = uiState, copy = copy)
        }
    }
}

@Composable
private fun TelemetryStatusPill(
    uiState: GamepadUiState,
    copy: AppCopy,
    onTapConnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val active = uiState.inputState.activeButtonLabels()
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(ConsoleSurface)
            .border(1.dp, ConsoleOutline, RoundedCornerShape(50))
            .clickable(onClick = onTapConnect)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (uiState.connectedDevice != null) EmeraldConnected else CyanGlow)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (active.isNotEmpty()) {
                active.joinToString(" + ")
            } else {
                "${copy.offlineBadge} • TX ${uiState.packetsSent}"
            },
            style = MaterialTheme.typography.labelSmall,
            color = if (active.isNotEmpty()) CyanGlow else TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TelemetryFooterBar(
    uiState: GamepadUiState,
    copy: AppCopy
) {
    val active = uiState.inputState.activeButtonLabels()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ConsoleSurfaceElevated.copy(alpha = 0.65f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (active.isEmpty()) copy.noActiveInput else active.joinToString(" + "),
            style = MaterialTheme.typography.labelSmall,
            color = if (active.isEmpty()) TextMuted else EmeraldConnected,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "TX: ${uiState.packetsSent}",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}
