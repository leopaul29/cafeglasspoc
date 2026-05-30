package com.example.cafeglasspoc

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.view.Display
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), DisplayManager.DisplayListener {

    private lateinit var displayManager: DisplayManager
    private var glassPresentation: GlassPresentation? = null

    private lateinit var tvCurrentMode: TextView
    private lateinit var btnNext: Button
    private lateinit var btnRush: Button
    private lateinit var btnReset: Button

    // Recipe state
    private val steps = listOf(
        "1/3 Royal Tea sachet\nin hot water — 50 sec",
        "Chai powder:\n5 taps",
        "150 ml\nhot milk"
    )
    private var currentStep = 0
    private var isRushMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvCurrentMode = findViewById(R.id.tvCurrentMode)
        btnNext       = findViewById(R.id.btnNext)
        btnRush       = findViewById(R.id.btnRush)
        btnReset      = findViewById(R.id.btnReset)

        displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        displayManager.registerDisplayListener(this, null)

        // Try to connect to an already-connected external display
        tryConnectExternalDisplay()

        btnNext.setOnClickListener {
            if (isRushMode) return@setOnClickListener
            if (currentStep < steps.size - 1) {
                currentStep++
                updateGlassRecipeView()
                updatePhoneUI()
            } else {
                Toast.makeText(this, "Recipe complete! ✅", Toast.LENGTH_SHORT).show()
            }
        }

        btnRush.setOnClickListener {
            isRushMode = !isRushMode
            if (isRushMode) {
                glassPresentation?.showRushMode()
                tvCurrentMode.text = "MODE: RUSH 🔥"
                btnRush.text = "← BACK TO RECIPE"
            } else {
                updateGlassRecipeView()
                updatePhoneUI()
            }
        }

        btnReset.setOnClickListener {
            isRushMode = false
            currentStep = 0
            updateGlassRecipeView()
            updatePhoneUI()
            btnRush.text = "START RUSH MODE 🔥"
        }
    }

    private fun tryConnectExternalDisplay() {
        val displays = displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION)
        if (displays.isNotEmpty()) {
            launchPresentation(displays[0])
        } else {
            tvCurrentMode.text = "MODE: RECIPE (no glasses detected)"
            Toast.makeText(this, "No external display found. Connect Rokid glasses.", Toast.LENGTH_LONG).show()
        }
    }

    private fun launchPresentation(display: Display) {
        glassPresentation?.dismiss()
        glassPresentation = GlassPresentation(this, display).apply {
            show()
        }
        // Push initial state
        glassPresentation?.showRecipeStep(steps, currentStep)
        updatePhoneUI()
    }

    private fun updateGlassRecipeView() {
        glassPresentation?.showRecipeStep(steps, currentStep)
    }

    private fun updatePhoneUI() {
        tvCurrentMode.text = "MODE: RECIPE — Step ${currentStep + 1} / ${steps.size}"
        btnNext.isEnabled = currentStep < steps.size - 1
        btnNext.alpha = if (btnNext.isEnabled) 1f else 0.4f
        isRushMode = false
        btnRush.text = "START RUSH MODE 🔥"
    }

    // ── DisplayManager.DisplayListener ──────────────────────────────────────

    override fun onDisplayAdded(displayId: Int) {
        val display = displayManager.getDisplay(displayId) ?: return
        if (display.flags and Display.FLAG_PRESENTATION != 0 ||
            displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION)
                .any { it.displayId == displayId }
        ) {
            runOnUiThread { launchPresentation(display) }
        }
    }

    override fun onDisplayRemoved(displayId: Int) {
        if (glassPresentation?.display?.displayId == displayId) {
            runOnUiThread {
                glassPresentation?.dismiss()
                glassPresentation = null
                tvCurrentMode.text = "MODE: RECIPE (glasses disconnected)"
            }
        }
    }

    override fun onDisplayChanged(displayId: Int) {}

    // ── Lifecycle ────────────────────────────────────────────────────────────

    override fun onDestroy() {
        super.onDestroy()
        displayManager.unregisterDisplayListener(this)
        glassPresentation?.dismiss()
    }
}