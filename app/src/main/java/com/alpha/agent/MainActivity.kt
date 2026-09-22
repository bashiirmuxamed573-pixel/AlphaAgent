package com.alpha.agent

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val prefs by lazy {
        getSharedPreferences("alpha_agent", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(32, 60, 32, 40)
            setBackgroundColor(Color.rgb(5, 5, 5))
        }

        val title = TextView(this).apply {
            text = "ALPHA"
            textSize = 40f
            setTextColor(Color.rgb(212, 175, 55))
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "AI WHATSAPP AGENT"
            textSize = 17f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 35)
        }

        val keyTitle = TextView(this).apply {
            text = "Gemini API Key"
            textSize = 16f
            setTextColor(Color.rgb(212, 175, 55))
        }

        val keyInput = EditText(this).apply {
            hint = "Geli Gemini API key-ga"
            setHintTextColor(Color.GRAY)
            setTextColor(Color.WHITE)
            inputType = 0x00000081
            setSingleLine(true)
        }

        keyInput.setText(
            prefs.getString("gemini_api_key", "")
        )

        val saveButton = Button(this).apply {
            text = "SAVE API KEY"
        }

        val testButton = Button(this).apply {
            text = "TEST GEMINI"
        }

        val accessButton = Button(this).apply {
            text = "FUR ACCESSIBILITY"
        }

        val status = TextView(this).apply {
            text = "Status: Diyaar"
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 25, 0, 0)
        }

        saveButton.setOnClickListener {
            val key = keyInput.text.toString().trim()

            if (key.isEmpty()) {
                status.text = "Status: API key geli marka hore"
                return@setOnClickListener
            }

            prefs.edit()
                .putString("gemini_api_key", key)
                .apply()

            status.text = "Status: API key waa la kaydiyey ✓"
        }

        testButton.setOnClickListener {
            val key = prefs.getString("gemini_api_key", "")

            if (key.isNullOrBlank()) {
                status.text = "Status: API key ma jiro"
                return@setOnClickListener
            }

            status.text = "Status: Gemini waa la tijaabinayaa..."

            GeminiClient.ask(
                key,
                "Salaan AlphaAgent. Si kooban oo dabiici ah Af-Soomaali iigu jawaab."
            ) { answer, error ->

                runOnUiThread {
                    if (answer != null) {
                        status.text = "Gemini ✓\n\n$answer"
                    } else {
                        status.text = "Gemini Error:\n$error"
                    }
                }
            }
        }

        accessButton.setOnClickListener {
            startActivity(
                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            )
        }

        root.addView(title)
        root.addView(subtitle)
        root.addView(keyTitle)
        root.addView(keyInput)
        root.addView(saveButton)
        root.addView(testButton)
        root.addView(accessButton)
        root.addView(status)

        setContentView(root)
    }
}
