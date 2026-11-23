package com.example.bluetooth

import com.example.model.GamepadInputState
import kotlin.math.roundToInt

/**
 * Industry-standard Bluetooth HID Report Descriptor for a Dual-Stick Gamepad
 * plus TV Consumer Control keys (Home, Back, Vol+/-, Play/Pause).
 *
 * Report ID 1 (7 bytes):
 *   Bytes 0..1 : 16 buttons (A, B, C, X, Y, Z, L1, R1, L2, R2, Select, Start, Home, L3, R3, Extra)
 *   Byte 2     : D-Pad Hat Switch (low 4 bits: 0..7 directional, 8 null/center) + 4 bits padding
 *   Byte 3     : Left Stick X (-127..127)
 *   Byte 4     : Left Stick Y (-127..127)
 *   Byte 5     : Right Stick X / Z (-127..127)
 *   Byte 6     : Right Stick Y / Rz (-127..127)
 *
 * Report ID 2 (2 bytes):
 *   Bytes 0..1 : 16-bit Consumer Control usage (little-endian)
 */
object GamepadHidDescriptor {
    const val REPORT_ID_GAMEPAD = 1
    const val REPORT_ID_CONSUMER = 2

    val DESCRIPTOR: ByteArray = byteArrayOf(
        // --- Report ID 1: Gamepad ---
        0x05.toByte(), 0x01.toByte(),       // Usage Page (Generic Desktop Ctrls)
        0x09.toByte(), 0x05.toByte(),       // Usage (Game Pad)
        0xA1.toByte(), 0x01.toByte(),       // Collection (Application)
        0x85.toByte(), 0x01.toByte(),       //   Report ID (1)

        // 16 Buttons (2 bytes)
        0x05.toByte(), 0x09.toByte(),       //   Usage Page (Button)
        0x19.toByte(), 0x01.toByte(),       //   Usage Minimum (0x01)
        0x29.toByte(), 0x10.toByte(),       //   Usage Maximum (0x10)
        0x15.toByte(), 0x00.toByte(),       //   Logical Minimum (0)
        0x25.toByte(), 0x01.toByte(),       //   Logical Maximum (1)
        0x75.toByte(), 0x01.toByte(),       //   Report Size (1)
        0x95.toByte(), 0x10.toByte(),       //   Report Count (16)
        0x81.toByte(), 0x02.toByte(),       //   Input (Data,Var,Abs)

        // D-Pad Hat Switch (4 bits) + 4 bits padding (1 byte)
        0x05.toByte(), 0x01.toByte(),       //   Usage Page (Generic Desktop Ctrls)
        0x09.toByte(), 0x39.toByte(),       //   Usage (Hat switch)
        0x15.toByte(), 0x00.toByte(),       //   Logical Minimum (0)
        0x25.toByte(), 0x07.toByte(),       //   Logical Maximum (7)
        0x35.toByte(), 0x00.toByte(),       //   Physical Minimum (0)
        0x46.toByte(), 0x3B.toByte(), 0x01.toByte(), // Physical Maximum (315)
        0x65.toByte(), 0x14.toByte(),       //   Unit (Degrees)
        0x75.toByte(), 0x04.toByte(),       //   Report Size (4)
        0x95.toByte(), 0x01.toByte(),       //   Report Count (1)
        0x81.toByte(), 0x42.toByte(),       //   Input (Data,Var,Abs,Null)
        0x75.toByte(), 0x04.toByte(),       //   Report Size (4)
        0x95.toByte(), 0x01.toByte(),       //   Report Count (1)
        0x81.toByte(), 0x03.toByte(),       //   Input (Const,Var,Abs)

        // Reset Physical/Unit global items before Analog Axes
        0x35.toByte(), 0x00.toByte(),       //   Physical Minimum (0)
        0x45.toByte(), 0x00.toByte(),       //   Physical Maximum (0)
        0x65.toByte(), 0x00.toByte(),       //   Unit (None)

        // 4 Analog Axes: Left X, Left Y, Right X (Z), Right Y (Rz)
        0x05.toByte(), 0x01.toByte(),       //   Usage Page (Generic Desktop Ctrls)
        0x09.toByte(), 0x30.toByte(),       //   Usage (X)
        0x09.toByte(), 0x31.toByte(),       //   Usage (Y)
        0x09.toByte(), 0x32.toByte(),       //   Usage (Z)
        0x09.toByte(), 0x35.toByte(),       //   Usage (Rz)
        0x15.toByte(), 0x81.toByte(),       //   Logical Minimum (-127)
        0x25.toByte(), 0x7F.toByte(),       //   Logical Maximum (127)
        0x75.toByte(), 0x08.toByte(),       //   Report Size (8)
        0x95.toByte(), 0x04.toByte(),       //   Report Count (4)
        0x81.toByte(), 0x02.toByte(),       //   Input (Data,Var,Abs)
        0xC0.toByte(),                      // End Collection

        // --- Report ID 2: Consumer Control (TV Navigation) ---
        0x05.toByte(), 0x0C.toByte(),       // Usage Page (Consumer)
        0x09.toByte(), 0x01.toByte(),       // Usage (Consumer Control)
        0xA1.toByte(), 0x01.toByte(),       // Collection (Application)
        0x85.toByte(), 0x02.toByte(),       //   Report ID (2)
        0x19.toByte(), 0x00.toByte(),       //   Usage Minimum (0)
        0x2A.toByte(), 0xFF.toByte(), 0x03.toByte(), // Usage Maximum (0x03FF)
        0x15.toByte(), 0x00.toByte(),       //   Logical Minimum (0)
        0x26.toByte(), 0xFF.toByte(), 0x03.toByte(), // Logical Maximum (0x03FF)
        0x75.toByte(), 0x10.toByte(),       //   Report Size (16)
        0x95.toByte(), 0x01.toByte(),       //   Report Count (1)
        0x81.toByte(), 0x00.toByte(),       //   Input (Data,Array,Abs)
        0xC0.toByte()                       // End Collection
    )

    fun buildGamepadReport(state: GamepadInputState): ByteArray {
        val buttonsLo = (state.buttonsMask and 0xFF).toByte()
        val buttonsHi = ((state.buttonsMask ushr 8) and 0xFF).toByte()
        val hatByte = (state.dPad.hatValue and 0x0F).toByte()
        val lx = floatAxisToByte(state.leftX)
        val ly = floatAxisToByte(state.leftY)
        val rx = floatAxisToByte(state.rightX)
        val ry = floatAxisToByte(state.rightY)

        return byteArrayOf(buttonsLo, buttonsHi, hatByte, lx, ly, rx, ry)
    }

    fun buildConsumerReport(usageCode: Int): ByteArray {
        val lo = (usageCode and 0xFF).toByte()
        val hi = ((usageCode ushr 8) and 0xFF).toByte()
        return byteArrayOf(lo, hi)
    }

    private fun floatAxisToByte(value: Float): Byte {
        val clamped = value.coerceIn(-1f, 1f)
        return (clamped * 127f).roundToInt().coerceIn(-127, 127).toByte()
    }
}
