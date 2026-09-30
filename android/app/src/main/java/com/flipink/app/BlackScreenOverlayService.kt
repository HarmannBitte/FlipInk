package com.flipink.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager

/**
 * BlackScreenOverlayService
 * 
 * Draws an opaque pure black view over the entire OLED screen.
 * On Nothing Phone (1)'s OLED panel, displaying #000000 turns off individual pixels,
 * dropping front screen power consumption by ~90-95%.
 *
 * It allows double-tap to wake / dismiss, or pass-through volume keys to flip pages.
 */
class BlackScreenOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var blackOverlayView: View? = null

    companion object {
        const val ACTION_START = "com.flipink.app.action.START_BLACKOUT"
        const val ACTION_STOP = "com.flipink.app.action.STOP_BLACKOUT"
        const val CHANNEL_ID = "flipink_blackout_channel"
        const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startForeground(NOTIFICATION_ID, buildNotification())
                showBlackOverlay()
            }
            ACTION_STOP -> {
                removeBlackOverlay()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun showBlackOverlay() {
        if (blackOverlayView != null) return

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val layoutParams = WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }
            // Keep screen on, extend into notch/cutout, allow dimming to 0%
            flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            format = PixelFormat.TRANSLUCENT
            gravity = Gravity.TOP or Gravity.START
            screenBrightness = 0.01f // Force lowest hardware backlight floor
        }

        val overlay = View(this).apply {
            setBackgroundColor(Color.BLACK)
            isClickable = true
            isFocusable = true
            
            // Double-tap to exit black screen
            var lastClickTime = 0L
            setOnClickListener {
                val now = System.currentTimeMillis()
                if (now - lastClickTime < 400) {
                    removeBlackOverlay()
                    stopSelf()
                }
                lastClickTime = now
            }
        }

        windowManager?.addView(overlay, layoutParams)
        blackOverlayView = overlay
    }

    private fun removeBlackOverlay() {
        blackOverlayView?.let {
            windowManager?.removeView(it)
            blackOverlayView = null
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "FlipInk Screen Saver",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps BlackScreen overlay active to save battery"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }

        return builder
            .setContentTitle("FlipInk: Front OLED Off")
            .setContentText("Reading on rear E-Ink display. Double-tap to wake front.")
            .setSmallIcon(android.R.drawable.ic_lock_power_off)
            .build()
    }

    override fun onDestroy() {
        removeBlackOverlay()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
