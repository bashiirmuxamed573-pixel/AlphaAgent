package com.alpha.agent

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 60, 40, 40)
        }

        val title = TextView(this).apply {
            text = "ALPHA AGENT"
            textSize = 30f
        }

        val info = TextView(this).apply {
            text = "Gemini AI • Somali • WhatsApp"
            textSize = 18f
            setPadding(0, 20, 0, 30)
        }

        val accessibility = Button(this).apply {
            text = "Fur Accessibility Settings"
            setOnClickListener {
                startActivity(
                    Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                )
            }
        }

        layout.addView(title)
        layout.addView(info)
        layout.addView(accessibility)

        setContentView(layout)
    }
}
