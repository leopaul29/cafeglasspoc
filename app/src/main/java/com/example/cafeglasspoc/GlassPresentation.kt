package com.example.cafeglasspoc

import android.app.Presentation
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Display
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.TextView
import kotlin.math.abs

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

    // Gesture detector for touchpad swipes
    private lateinit var gestureDetector: GestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContentView(R.layout.presentation_glass)

        tvTitle = findViewById(R.id.tvTitle)
        tvStep1 = findViewById(R.id.tvStep1)
        tvStep2 = findViewById(R.id.tvStep2)
        tvStep3 = findViewById(R.id.tvStep3)

        // Swipe gesture: left = next, right = prev
        gestureDetector = GestureDetector(context,
            object : GestureDetector.SimpleOnGestureListener() {

                private val SWIPE_MIN_DISTANCE = 50
                private val SWIPE_MIN_VELOCITY = 100

                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    val deltaX = (e2.x - (e1?.x ?: e2.x))
                    val deltaY = (e2.y - (e1?.y ?: e2.y))

                    // Horizontal swipe dominates
                    if (abs(deltaX) > abs(deltaY)) {
                        if (deltaX < -SWIPE_MIN_DISTANCE &&
                            abs(velocityX) > SWIPE_MIN_VELOCITY) {
                            onNext() // swipe left = next
                            return true
                        } else if (deltaX > SWIPE_MIN_DISTANCE &&
                            abs(velocityX) > SWIPE_MIN_VELOCITY) {
                            onPrev() // swipe right = prev/reset
                            return true
                        }
                    }
                    return false
                }

                // Single tap also advances
                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    onNext()
                    return true
                }
            })

        renderStep(pendingStep)
    }

    // Route ALL touch events through the gesture detector
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    // Also catch hardware key events from the glasses touchpad
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_RIGHT,
                KeyEvent.KEYCODE_DPAD_CENTER,
                KeyEvent.KEYCODE_ENTER,
                KeyEvent.KEYCODE_BUTTON_R1 -> {
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

    fun showRecipeStep(activeIndex: Int) {
        pendingStep = activeIndex
        if (tvStep1 != null) renderStep(activeIndex)
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