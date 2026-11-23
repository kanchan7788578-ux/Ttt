package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetooth.BluetoothHidController
import com.example.data.SettingsRepository
import com.example.haptics.HapticManager
import com.example.model.AppLanguage
import com.example.model.AppSettings
import com.example.model.BluetoothTvDevice
import com.example.model.ControllerLayoutMode
import com.example.model.DPadDirection
import com.example.model.GamepadButton
import com.example.model.GamepadInputState
import com.example.model.HapticStrength
import com.example.model.HidRegistrationState
import com.example.model.OrientationPreference
import com.example.model.TvRemoteKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.hypot

data class GamepadUiState(
    val inputState: GamepadInputState = GamepadInputState(),
    val hidState: HidRegistrationState = HidRegistrationState.REGISTERING,
    val pairedDevices: List<BluetoothTvDevice> = emptyList(),
    val discoveredDevices: List<BluetoothTvDevice> = emptyList(),
    val connectedDevice: BluetoothTvDevice? = null,
    val isScanning: Boolean = false,
    val packetsSent: Long = 0L,
    val settings: AppSettings = AppSettings(),
    val errorMessage: String? = null
)

class GamepadViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)
    private val hapticManager = HapticManager(application)
    val bluetoothController = BluetoothHidController(application)

    private val _inputState = MutableStateFlow(GamepadInputState())

    private var lastAnalogReportMs = 0L

    private val btSnapshotFlow = combine(
        bluetoothController.hidState,
        bluetoothController.pairedDevices,
        bluetoothController.discoveredDevices,
        bluetoothController.connectedDevice,
        bluetoothController.isScanning
    ) { hid, paired, discovered, connected, scanning ->
        BtSnapshot(hid, paired, discovered, connected, scanning)
    }

    val uiState: StateFlow<GamepadUiState> = combine(
        _inputState,
        btSnapshotFlow,
        bluetoothController.packetsSent,
        settingsRepository.settingsFlow,
        bluetoothController.errorMessage
    ) { input, bt, packets, settings, error ->
        GamepadUiState(
            inputState = input,
            hidState = bt.hidState,
            pairedDevices = bt.pairedDevices,
            discoveredDevices = bt.discoveredDevices,
            connectedDevice = bt.connectedDevice,
            isScanning = bt.isScanning,
            packetsSent = packets,
            settings = settings,
            errorMessage = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GamepadUiState()
    )

    private data class BtSnapshot(
        val hidState: HidRegistrationState,
        val pairedDevices: List<BluetoothTvDevice>,
        val discoveredDevices: List<BluetoothTvDevice>,
        val connectedDevice: BluetoothTvDevice?,
        val isScanning: Boolean
    )

    init {
        bluetoothController.initialize()
    }

    fun onPermissionResultOrResume() {
        bluetoothController.refreshStateAndRegisterProfile()
    }

    fun onButtonChanged(button: GamepadButton, pressed: Boolean) {
        val current = _inputState.value
        if (current.isPressed(button) == pressed) return

        val next = current.withButton(button, pressed)
        _inputState.value = next

        if (pressed) {
            val heavy = button == GamepadButton.L2 || button == GamepadButton.R2 ||
                button == GamepadButton.A || button == GamepadButton.B
            hapticManager.pulse(uiState.value.settings.hapticStrength, isHeavyButton = heavy)
        }
        bluetoothController.sendGamepadState(next)
    }

    fun onDPadChanged(direction: DPadDirection) {
        val current = _inputState.value
        if (current.dPad == direction) return

        val next = current.copy(dPad = direction)
        _inputState.value = next

        if (direction != DPadDirection.CENTER) {
            hapticManager.pulse(uiState.value.settings.hapticStrength, isHeavyButton = false)
        }
        bluetoothController.sendGamepadState(next)
    }

    fun onLeftStickChanged(rawX: Float, rawY: Float) {
        val (x, y) = applyDeadzone(rawX, rawY, uiState.value.settings.deadzonePercent)
        val current = _inputState.value
        if (current.leftX == x && current.leftY == y) return

        val next = current.copy(leftX = x, leftY = y)
        _inputState.value = next
        sendAnalogThrottled(next, isCenter = (x == 0f && y == 0f))
    }

    fun onRightStickChanged(rawX: Float, rawY: Float) {
        val (x, y) = applyDeadzone(rawX, rawY, uiState.value.settings.deadzonePercent)
        val current = _inputState.value
        if (current.rightX == x && current.rightY == y) return

        val next = current.copy(rightX = x, rightY = y)
        _inputState.value = next
        sendAnalogThrottled(next, isCenter = (x == 0f && y == 0f))
    }

    fun onTvRemoteKeyTap(key: TvRemoteKey) {
        hapticManager.pulse(uiState.value.settings.hapticStrength, isHeavyButton = false)
        bluetoothController.sendConsumerKeyTap(key)
    }

    fun connectToDevice(address: String) {
        bluetoothController.connectToDevice(address)
    }

    fun disconnectDevice() {
        bluetoothController.disconnectCurrent()
    }

    fun startDiscovery() {
        bluetoothController.startDiscovery()
    }

    fun stopDiscovery() {
        bluetoothController.stopDiscovery()
    }

    fun pairDevice(address: String) {
        bluetoothController.pairWithDevice(address)
    }

    fun refreshDevices() {
        bluetoothController.refreshStateAndRegisterProfile()
    }

    fun clearError() {
        bluetoothController.clearError()
    }

    fun setLayoutMode(mode: ControllerLayoutMode) {
        viewModelScope.launch { settingsRepository.updateLayoutMode(mode) }
    }

    fun setHapticStrength(strength: HapticStrength) {
        viewModelScope.launch {
            settingsRepository.updateHapticStrength(strength)
            hapticManager.pulse(strength, isHeavyButton = false)
        }
    }

    fun setDeadzonePercent(percent: Int) {
        viewModelScope.launch { settingsRepository.updateDeadzonePercent(percent) }
    }

    fun setKeepScreenOn(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateKeepScreenOn(enabled) }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { settingsRepository.updateLanguage(language) }
    }

    fun setOrientationPreference(pref: OrientationPreference) {
        viewModelScope.launch { settingsRepository.updateOrientationPreference(pref) }
    }

    private fun sendAnalogThrottled(state: GamepadInputState, isCenter: Boolean) {
        val now = System.currentTimeMillis()
        // Always send immediately when snapping back to center, otherwise ~80Hz max
        if (isCenter || now - lastAnalogReportMs >= 12L) {
            lastAnalogReportMs = now
            bluetoothController.sendGamepadState(state)
        }
    }

    private fun applyDeadzone(x: Float, y: Float, deadzonePercent: Int): Pair<Float, Float> {
        val dz = (deadzonePercent / 100f).coerceIn(0f, 0.35f)
        val mag = hypot(x, y)
        if (mag < dz || mag == 0f) return 0f to 0f
        val clampedMag = mag.coerceAtMost(1f)
        val scaledMag = ((clampedMag - dz) / (1f - dz)).coerceIn(0f, 1f)
        val scale = scaledMag / mag
        return (x * scale) to (y * scale)
    }

    override fun onCleared() {
        super.onCleared()
        bluetoothController.release()
    }
}
