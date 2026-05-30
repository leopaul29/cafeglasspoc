package com.example.cafeglasspoc

import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvStep1: TextView
    private lateinit var tvStep2: TextView
    private lateinit var tvStep3: TextView
    private lateinit var btnReset: Button

    private val steps = listOf(
        "1/3 Royal Tea sachet\nin hot water — 50 sec",
        "Chai powder:\n5 taps",
        "150 ml\nhot milk"
    )
    private val labels = listOf("STEP 1 ▶", "STEP 2 ▶", "STEP 3 ▶")
    private var currentStep = 0

    // Debounce: ignore taps within 600ms of each other
    private var lastTapTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStep1  = findViewById(R.id.tvStep1)
        tvStep2  = findViewById(R.id.tvStep2)
        tvStep3  = findViewById(R.id.tvStep3)
        btnReset = findViewById(R.id.btnReset)

        btnReset.setOnClickListener {
            currentStep = 0
            render()
        }

        val rootView = findViewById<android.view.View>(android.R.id.content)
        rootView.setOnClickListener {
            val now = System.currentTimeMillis()
            if (now - lastTapTime > 600) {
                lastTapTime = now
                advance()
            }
        }

        render()
    }

    // No dispatchTouchEvent override — removed entirely

    private fun advance() {
        if (currentStep < steps.size - 1) {
            currentStep++
            render()
        }
    }

    private fun render() {
        val stepViews = listOf(tvStep1, tvStep2, tvStep3)
        stepViews.forEachIndexed { index, tv ->
            tv.text = "${labels[index]}\n${steps[index]}"
            if (index == currentStep) {
                tv.setTextColor(Color.parseColor("#00FFCC"))
                tv.alpha = 1f
            } else {
                tv.setTextColor(Color.parseColor("#FF3366"))
                tv.alpha = 0.4f
            }
        }
    }
}