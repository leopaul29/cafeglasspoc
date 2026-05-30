package com.example.cafeglasspoc

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var glassService: GlassService? = null
    private var isBound = false

    private lateinit var tvCurrentMode: TextView
    private lateinit var btnNext: Button
    private lateinit var btnReset: Button

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            glassService = (binder as GlassService.GlassBinder).getService()
            isBound = true
            updatePhoneUI()
        }
        override fun onServiceDisconnected(name: ComponentName) {
            isBound = false
            glassService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvCurrentMode = findViewById(R.id.tvCurrentMode)
        btnNext       = findViewById(R.id.btnNext)
        btnReset      = findViewById(R.id.btnReset)

        val intent = Intent(this, GlassService::class.java)
        startForegroundService(intent)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)

        btnNext.setOnClickListener {
            glassService?.nextStep()
            updatePhoneUI()
        }

        btnReset.setOnClickListener {
            glassService?.reset()
            updatePhoneUI()
        }
    }

    override fun onResume() {
        super.onResume()
        updatePhoneUI()
    }

    private fun updatePhoneUI() {
        val svc = glassService ?: return
        tvCurrentMode.text = "Step ${svc.currentStep + 1} / ${svc.getStepCount()}"
        btnNext.isEnabled = svc.currentStep < svc.getStepCount() - 1
        btnNext.alpha = if (btnNext.isEnabled) 1f else 0.4f
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) unbindService(connection)
    }
}