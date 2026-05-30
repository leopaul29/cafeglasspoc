package com.example.cafeglasspoc

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

    val steps = listOf(
        "1/3 Royal Tea sachet\nin hot water — 50 sec",
        "Chai powder:\n5 taps",
        "150 ml\nhot milk"
    )
    var currentStep = 0

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

        val displays = displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION)
        if (displays.isNotEmpty()) launchPresentation(displays[0])
    }

    private fun startForegroundCompat() {
        val channelId = "glass_channel"
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(channelId, "Glass Display", NotificationManager.IMPORTANCE_LOW)
        )
        startForeground(1,
            Notification.Builder(this, channelId)
                .setContentTitle("Rokid Barista")
                .setContentText("Glasses display active")
                .setSmallIcon(android.R.drawable.ic_menu_view)
                .build()
        )
    }

    fun launchPresentation(display: Display) {
        glassPresentation?.dismiss()
        glassPresentation = GlassPresentation(
            context = this,
            display = display,
            onNext  = { nextStep() },
            onPrev  = { reset() }
        ).apply { show() }
        glassPresentation?.showRecipeStep(currentStep)
    }

    fun nextStep() {
        if (currentStep < steps.size - 1) {
            currentStep++
            glassPresentation?.showRecipeStep(currentStep)
        }
    }

    fun reset() {
        currentStep = 0
        glassPresentation?.showRecipeStep(currentStep)
    }

    fun getStepCount() = steps.size

    override fun onDisplayAdded(displayId: Int) {
        val display = displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION)
            .firstOrNull { it.displayId == displayId } ?: return
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