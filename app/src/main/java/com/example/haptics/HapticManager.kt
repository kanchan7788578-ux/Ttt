package com.example.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.model.HapticStrength

class HapticManager(context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun pulse(strength: HapticStrength, isHeavyButton: Boolean = false) {
        if (strength == HapticStrength.OFF) return
        val v = vibrator ?: return
        if (!v.hasVibrator()) return

        val baseMs = when (strength) {
            HapticStrength.OFF -> return
            HapticStrength.LIGHT -> 10L
            HapticStrength.MEDIUM -> 18L
            HapticStrength.STRONG -> 28L
        }
        val durationMs = if (isHeavyButton) baseMs + 6L else baseMs

        val amplitude = when (strength) {
            HapticStrength.OFF -> 0
            HapticStrength.LIGHT -> 70
            HapticStrength.MEDIUM -> 145
            HapticStrength.STRONG -> 235
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(durationMs, amplitude)
            v.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(durationMs)
        }
    }
}
