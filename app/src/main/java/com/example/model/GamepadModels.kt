package com.example.model

enum class ControllerLayoutMode {
    PRO_DUAL_STICK,
    RETRO_ARCADE,
    TV_REMOTE
}

enum class HapticStrength {
    OFF,
    LIGHT,
    MEDIUM,
    STRONG
}

enum class AppLanguage {
    BENGALI,
    ENGLISH
}

enum class OrientationPreference {
    AUTO,
    LANDSCAPE,
    PORTRAIT
}

enum class GamepadButton(val bitIndex: Int, val label: String) {
    A(0, "A"),
    B(1, "B"),
    X(3, "X"),
    Y(4, "Y"),
    L1(6, "L1"),
    R1(7, "R1"),
    L2(8, "L2"),
    R2(9, "R2"),
    SELECT(10, "SELECT"),
    START(11, "START"),
    HOME(12, "HOME"),
    L3(13, "L3"),
    R3(14, "R3")
}

enum class TvRemoteKey(val consumerUsage: Int) {
    HOME(0x0223),
    BACK(0x0224),
    VOLUME_UP(0x00E9),
    VOLUME_DOWN(0x00EA),
    PLAY_PAUSE(0x00CD),
    MUTE(0x00E2)
}

enum class DPadDirection(val hatValue: Int) {
    UP(0),
    UP_RIGHT(1),
    RIGHT(2),
    DOWN_RIGHT(3),
    DOWN(4),
    DOWN_LEFT(5),
    LEFT(6),
    UP_LEFT(7),
    CENTER(8)
}

data class GamepadInputState(
    val buttonsMask: Int = 0,
    val dPad: DPadDirection = DPadDirection.CENTER,
    val leftX: Float = 0f,
    val leftY: Float = 0f,
    val rightX: Float = 0f,
    val rightY: Float = 0f
) {
    fun isPressed(button: GamepadButton): Boolean =
        (buttonsMask and (1 shl button.bitIndex)) != 0

    fun withButton(button: GamepadButton, pressed: Boolean): GamepadInputState {
        val bit = 1 shl button.bitIndex
        val nextMask = if (pressed) buttonsMask or bit else buttonsMask and bit.inv()
        return copy(buttonsMask = nextMask)
    }

    fun activeButtonLabels(): List<String> = buildList {
        GamepadButton.entries.forEach { btn ->
            if (isPressed(btn)) add(btn.label)
        }
        if (dPad != DPadDirection.CENTER) add("D-${dPad.name}")
    }
}

enum class DeviceConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED
}

enum class BondStatus {
    NONE,
    BONDING,
    BONDED
}

data class BluetoothTvDevice(
    val name: String,
    val address: String,
    val isTvOrMedia: Boolean,
    val bondStatus: BondStatus,
    val connectionState: DeviceConnectionState
)

enum class HidRegistrationState {
    UNSUPPORTED_HARDWARE,
    UNSUPPORTED_OS,
    PERMISSION_REQUIRED,
    BLUETOOTH_DISABLED,
    REGISTERING,
    REGISTERED,
    ERROR
}

data class AppSettings(
    val layoutMode: ControllerLayoutMode = ControllerLayoutMode.PRO_DUAL_STICK,
    val hapticStrength: HapticStrength = HapticStrength.MEDIUM,
    val deadzonePercent: Int = 10,
    val keepScreenOn: Boolean = true,
    val language: AppLanguage = AppLanguage.BENGALI,
    val orientationPreference: OrientationPreference = OrientationPreference.AUTO
)
