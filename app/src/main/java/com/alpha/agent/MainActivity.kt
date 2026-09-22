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
            text = "Alpha Agent"
            textSize = 28f
        }

        val info = TextView(this).apply {
            text = "WhatsApp AI Agent\\n\\nDaar Accessibility Service si Alpha Agent u shaqeeyo."
            textSize = 18f
        }

        val button = Button(this).apply {
            text = "Fur Accessibility Settings"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }

        layout.addView(title)
        layout.addView(info)
        layout.addView(button)

        setContentView(layout)
    }
}
