package com.example

import com.example.bluetooth.GamepadHidDescriptor
import com.example.model.DPadDirection
import com.example.model.GamepadButton
import com.example.model.GamepadInputState
import com.example.model.TvRemoteKey
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class GamepadHidDescriptorTest {

    @Test
    fun idleState_encodesCenteredZeroReport() {
        val report = GamepadHidDescriptor.buildGamepadReport(GamepadInputState())
        assertEquals(7, report.size)
        assertArrayEquals(
            byteArrayOf(0x00, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00),
            report
        )
    }

    @Test
    fun buttonsAndAxes_encodeProperBitmaskAndSignedBytes() {
        val state = GamepadInputState()
            .withButton(GamepadButton.A, true)
            .withButton(GamepadButton.L1, true)
            .withButton(GamepadButton.START, true)
            .copy(
                dPad = DPadDirection.UP_RIGHT,
                leftX = -1f,
                leftY = 1f,
                rightX = 0.5f,
                rightY = -0.5f
            )

        val report = GamepadHidDescriptor.buildGamepadReport(state)
        // A = bit 0 (1), L1 = bit 6 (64) -> lo = 65 (0x41)
        // START = bit 11 -> hi = bit 3 (8 = 0x08)
        assertEquals(0x41.toByte(), report[0])
        assertEquals(0x08.toByte(), report[1])
        assertEquals(1.toByte(), report[2]) // UP_RIGHT = 1
        assertEquals((-127).toByte(), report[3])
        assertEquals(127.toByte(), report[4])
        assertEquals(64.toByte(), report[5])
        assertEquals((-63).toByte(), report[6])
    }

    @Test
    fun consumerControlKey_encodesLittleEndian() {
        val homeReport = GamepadHidDescriptor.buildConsumerReport(TvRemoteKey.HOME.consumerUsage)
        assertArrayEquals(byteArrayOf(0x23, 0x02), homeReport)
    }
}
