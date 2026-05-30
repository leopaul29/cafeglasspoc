package com.example.cafeglasspoc

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private var glassService: GlassService? = null
    private var isBound = false

    private lateinit var tvCurrentMode: TextView
    private lateinit var btnNext: Button
    private lateinit var btnReset: Button
    private lateinit var gestureDetector: GestureDetector

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            glassService = (binder as GlassService.GlassBinder).getService()
            isBound = true
            updatePhoneUI()
        }
        override fun onServiceDisconnected(name: ComponentName) {
            isBound = false
            glassService = null
            updatePhoneUI()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvCurrentMode = findViewById(R.id.tvCurrentMode)
        btnNext       = findViewById(R.id.btnNext)
        btnReset      = findViewById(R.id.btnReset)

        btnNext.isEnabled = false
        btnNext.alpha = 0.4f

        val intent = Intent(this, GlassService::class.java)
        startForegroundService(intent)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)

        // Phone button still works as backup
        btnNext.setOnClickListener {
            advance()
        }

        btnReset.setOnClickListener {
            val svc = glassService ?: return@setOnClickListener
            svc.reset()
            updatePhoneUI()
        }

        // Swipe left OR tap anywhere on the phone screen = next step
        gestureDetector = GestureDetector(this,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    advance()
                    return true
                }
                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    val dx = e2.x - (e1?.x ?: e2.x)
                    val dy = e2.y - (e1?.y ?: e2.y)
                    if (abs(dx) > abs(dy) && dx < -100 && abs(velocityX) > 100) {
                        advance() // swipe left = next
                    }
                    return true
                }
            })
    }

    // Route all touch events through the gesture detector
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    private fun advance() {
        val svc = glassService ?: return
        svc.nextStep()
        updatePhoneUI()
    }

    override fun onResume() {
        super.onResume()
        updatePhoneUI()
    }

    private fun updatePhoneUI() {
        val svc = glassService
        if (svc == null) {
            tvCurrentMode.text = "Connecting..."
            btnNext.isEnabled = false
            btnNext.alpha = 0.4f
            return
        }
        tvCurrentMode.text = "Step ${svc.currentStep + 1} / ${svc.getStepCount()}"
        val canGoNext = svc.currentStep < svc.getStepCount() - 1
        btnNext.isEnabled = canGoNext
        btnNext.alpha = if (canGoNext) 1f else 0.4f
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }
}