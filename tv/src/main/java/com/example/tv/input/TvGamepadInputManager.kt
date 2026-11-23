package com.example.tv.input

import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import com.example.tv.model.HardwarePadState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.abs

class TvGamepadInputManager {

    private val _padState = MutableStateFlow(HardwarePadState())
    val padState: StateFlow<HardwarePadState> = _padState.asStateFlow()

    fun onKeyEvent(event: KeyEvent): Boolean {
        val isDown = event.action == KeyEvent.ACTION_DOWN
        if (event.action != KeyEvent.ACTION_DOWN && event.action != KeyEvent.ACTION_UP) {
            return false
        }
        val sourceName = event.device?.name?.takeIf { it.isNotBlank() }

        var handled = true
        _padState.update { current ->
            val base = if (sourceName != null) current.copy(activeControllerName = sourceName) else current
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> base.copy(up = isDown)
                KeyEvent.KEYCODE_DPAD_DOWN -> base.copy(down = isDown)
                KeyEvent.KEYCODE_DPAD_LEFT -> base.copy(left = isDown)
                KeyEvent.KEYCODE_DPAD_RIGHT -> base.copy(right = isDown)
                KeyEvent.KEYCODE_BUTTON_A, KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER ->
                    base.copy(buttonA = isDown)
                KeyEvent.KEYCODE_BUTTON_B -> base.copy(buttonB = isDown)
                KeyEvent.KEYCODE_BUTTON_X -> base.copy(buttonX = isDown)
                KeyEvent.KEYCODE_BUTTON_Y -> base.copy(buttonY = isDown)
                KeyEvent.KEYCODE_BUTTON_L1 -> base.copy(buttonL1 = isDown)
                KeyEvent.KEYCODE_BUTTON_R1 -> base.copy(buttonR1 = isDown)
                KeyEvent.KEYCODE_BUTTON_L2 -> base.copy(buttonL2 = isDown)
                KeyEvent.KEYCODE_BUTTON_R2 -> base.copy(buttonR2 = isDown)
                KeyEvent.KEYCODE_BUTTON_SELECT -> base.copy(buttonSelect = isDown)
                KeyEvent.KEYCODE_BUTTON_START -> base.copy(buttonStart = isDown)
                else -> {
                    handled = false
                    base
                }
            }
        }
        return handled
    }

    fun onGenericMotionEvent(event: MotionEvent): Boolean {
        val isFromJoystickOrGamepad =
            (event.source and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK ||
                (event.source and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD
        if (!isFromJoystickOrGamepad || event.action != MotionEvent.ACTION_MOVE) {
            return false
        }

        val device = event.device
        val sourceName = device?.name?.takeIf { it.isNotBlank() }
        val lx = cleanAxis(event.getAxisValue(MotionEvent.AXIS_X), device, MotionEvent.AXIS_X)
        val ly = cleanAxis(event.getAxisValue(MotionEvent.AXIS_Y), device, MotionEvent.AXIS_Y)
        val rx = cleanAxis(event.getAxisValue(MotionEvent.AXIS_Z), device, MotionEvent.AXIS_Z)
        val ry = cleanAxis(event.getAxisValue(MotionEvent.AXIS_RZ), device, MotionEvent.AXIS_RZ)
        val hatX = event.getAxisValue(MotionEvent.AXIS_HAT_X)
        val hatY = event.getAxisValue(MotionEvent.AXIS_HAT_Y)

        _padState.update { current ->
            current.copy(
                leftX = lx,
                leftY = ly,
                rightX = rx,
                rightY = ry,
                left = hatX < -0.5f,
                right = hatX > 0.5f,
                up = hatY < -0.5f,
                down = hatY > 0.5f,
                activeControllerName = sourceName ?: current.activeControllerName
            )
        }
        return true
    }

    fun setTouchButton(updateFn: (HardwarePadState) -> HardwarePadState) {
        _padState.update(updateFn)
    }

    private fun cleanAxis(value: Float, device: InputDevice?, axis: Int): Float {
        val flat = device?.getMotionRange(axis, InputDevice.SOURCE_JOYSTICK)?.flat ?: 0.08f
        return if (abs(value) < maxOf(flat, 0.08f)) 0f else value.coerceIn(-1f, 1f)
    }
}
