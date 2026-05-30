package com.example.cafeglasspoc

import android.app.Presentation
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Display
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.TextView

class GlassPresentation(
    context: Context,
    display: Display,
    private val onNext: () -> Unit,
    private val onPrev: () -> Unit
) : Presentation(context, display) {

    private val steps = listOf(
        "1/3 Royal Tea sachet\nin hot water — 50 sec",
        "Chai powder:\n5 taps",
        "150 ml\nhot milk"
    )
    private var pendingStep: Int = 0

    private var tvTitle: TextView? = null
    private var tvStep1: TextView? = null
    private var tvStep2: TextView? = null
    private var tvStep3: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.presentation_glass)

        tvTitle = findViewById(R.id.tvTitle)
        tvStep1 = findViewById(R.id.tvStep1)
        tvStep2 = findViewById(R.id.tvStep2)
        tvStep3 = findViewById(R.id.tvStep3)

        // Post to ensure the window is fully attached before rendering
        window?.decorView?.post {
            renderStep(pendingStep)
        }
    }

    fun showRecipeStep(activeIndex: Int) {
        pendingStep = activeIndex
        // Post so we never call renderStep on a not-yet-attached window
        window?.decorView?.post {
            if (tvStep1 != null) renderStep(activeIndex)
        }
    }

    // Glasses touchpad → D-pad key events (OS translates automatically)
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_RIGHT,
                KeyEvent.KEYCODE_DPAD_CENTER,
                KeyEvent.KEYCODE_ENTER -> {
                    onNext()
                    return true
                }
                KeyEvent.KEYCODE_DPAD_LEFT,
                KeyEvent.KEYCODE_BACK -> {
                    onPrev()
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private fun renderStep(activeIndex: Int) {
        tvTitle?.text = "[ APPRENTICE : CHAI MILK TEA (S) ]"

        val stepViews = listOf(tvStep1, tvStep2, tvStep3)
        val labels    = listOf("STEP 1 ▶", "STEP 2 ▶", "STEP 3 ▶")

        stepViews.forEachIndexed { index, tv ->
            tv?.text = "${labels[index]}\n${steps[index]}"
            if (index == activeIndex) {
                tv?.setTextColor(Color.parseColor("#00FFCC"))
                tv?.alpha    = 1f
                tv?.textSize = 32f
            } else {
                tv?.setTextColor(Color.parseColor("#FF3366"))
                tv?.alpha    = 0.4f
                tv?.textSize = 22f
            }
        }
    }
}