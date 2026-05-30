package com.hackathon.rokidpoc

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.os.Binder
import android.os.IBinder
import android.view.Display

class GlassService : Service(), DisplayManager.DisplayListener {

    private lateinit var displayManager: DisplayManager
    var glassPresentation: GlassPresentation? = null
        private set

    // Steps stored here so the presentation survives activity recreation
    private val steps = listOf(
        "1/3 Royal Tea sachet\nin hot water — 50 sec",
        "Chai powder:\n5 taps",
        "150 ml\nhot milk"
    )
    var currentStep = 0
    var isRushMode = false

    inner class GlassBinder : Binder() {
        fun getService(): GlassService = this@GlassService
    }

    private val binder = GlassBinder()

    override fun onBind(intent: Intent): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        startForegroundCompat()

        displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        displayManager.registerDisplayListener(this, null)

        // Connect to any already-present external display
        val displays = displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION)
        if (displays.isNotEmpty()) {
            launchPresentation(displays[0])
        }
    }

    private fun startForegroundCompat() {
        val channelId = "glass_service_channel"
        val channel = NotificationChannel(
            channelId, "Glass Display Service",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        val notification = Notification.Builder(this, channelId)
            .setContentTitle("Rokid Barista")
            .setContentText("Glasses display active")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .build()

        startForeground(1, notification)
    }

    fun launchPresentation(display: Display) {
        glassPresentation?.dismiss()
        glassPresentation = GlassPresentation(this, display).apply { show() }
        refreshGlassState()
    }

    fun refreshGlassState() {
        if (isRushMode) {
            glassPresentation?.showRushMode()
        } else {
            glassPresentation?.showRecipeStep(steps, currentStep)
        }
    }

    fun nextStep() {
        if (!isRushMode && currentStep < steps.size - 1) {
            currentStep++
            glassPresentation?.showRecipeStep(steps, currentStep)
        }
    }

    fun toggleRush() {
        isRushMode = !isRushMode
        refreshGlassState()
    }

    fun reset() {
        isRushMode = false
        currentStep = 0
        refreshGlassState()
    }

    fun getStepCount() = steps.size

    // DisplayListener — auto-reconnect when glasses reconnect
    override fun onDisplayAdded(displayId: Int) {
        val displays = displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION)
        val display = displays.firstOrNull { it.displayId == displayId } ?: return
        launchPresentation(display)
    }

    override fun onDisplayRemoved(displayId: Int) {
        if (glassPresentation?.display?.displayId == displayId) {
            glassPresentation?.dismiss()
            glassPresentation = null
        }
    }

    override fun onDisplayChanged(displayId: Int) {}

    override fun onDestroy() {
        super.onDestroy()
        displayManager.unregisterDisplayListener(this)
        glassPresentation?.dismiss()
    }
}