package com.example.adskipper

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = (24 * resources.displayMetrics.density).toInt()
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }
        status = TextView(this).apply { textSize = 18f }
        val accessBtn = Button(this).apply {
            text = "Open Accessibility settings"
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        }
        val batteryBtn = Button(this).apply {
            text = "Open battery optimization settings"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            }
        }
        layout.addView(status)
        layout.addView(accessBtn)
        layout.addView(batteryBtn)
        setContentView(layout)
    }

    override fun onResume() {
        super.onResume()
        status.text = if (isServiceEnabled()) "Service is ON" else "Service is OFF"
    }

    private fun isServiceEnabled(): Boolean {
        val me = ComponentName(this, SkipAdService::class.java)
        val enabled = Settings.Secure.getString(
            contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabled.split(':').any { ComponentName.unflattenFromString(it) == me }
    }
}
