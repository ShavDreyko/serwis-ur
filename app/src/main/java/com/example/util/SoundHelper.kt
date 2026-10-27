package com.example.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.data.model.TaskStatus

object SoundHelper {
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90)
        } catch (e: Exception) {
            toneGenerator = null
        }
    }

    fun playStatusChangeSound(context: Context, newStatus: TaskStatus) {
        try {
            when (newStatus) {
                TaskStatus.W_TRAKCIE -> {
                    // Crisp energetic starting tone
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 180)
                    vibrate(context, 80)
                }
                TaskStatus.ZAKONCZONE -> {
                    // Success acknowledgment double tone
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 250)
                    vibrate(context, 120)
                }
                TaskStatus.WSTRZYMANE -> {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 160)
                    vibrate(context, 60)
                }
                TaskStatus.NOWE -> {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
                    vibrate(context, 50)
                }
            }
        } catch (e: Exception) {
            // Audio error fallback
        }
    }

    fun playCriticalAlertSound(context: Context) {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 400)
            vibrate(context, 250)
        } catch (e: Exception) {
            // Audio error fallback
        }
    }

    private fun vibrate(context: Context, durationMs: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(durationMs)
                }
            }
        } catch (e: Exception) {
            // Ignore vibration error
        }
    }
}
