package com.alpha.agent

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var apiKeyInput: EditText
    private lateinit var statusText: TextView

    private val prefs by lazy {
        getSharedPreferences("alpha_agent", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 50, 32, 32)
            setBackgroundResource(R.drawable.alpha_background)
        }

        val title = TextView(this).apply {
            text = "ALPHA AGENT"
            textSize = 30f
            setTextColor(0xFFFFFFFF.toInt())
        }

        val subtitle = TextView(this).apply {
            text = "Gemini AI • Somali • WhatsApp"
            textSize = 17f
            setTextColor(0xFFE0E0E0.toInt())
            setPadding(0, 10, 0, 30)
        }

        apiKeyInput = EditText(this).apply {
            hint = "Geli Gemini API Key"
            setSingleLine(true)
            textSize = 16f
            inputType =
                android.text.InputType.TYPE_CLASS_TEXT or
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD

            setTextColor(0xFFFFFFFF.toInt())
            setHintTextColor(0xFFBBBBBB.toInt())

            val savedKey = prefs.getString("gemini_api_key", "")
            setText(savedKey)
        }

        val saveButton = Button(this).apply {
            text = "Kaydi Gemini Key"

            setOnClickListener {
                val key = apiKeyInput.text.toString().trim()

                if (key.isEmpty()) {
                    Toast.makeText(
                        this@MainActivity,
                        "Geli Gemini API Key-gaaga",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                prefs.edit()
                    .putString("gemini_api_key", key)
                    .apply()

                Toast.makeText(
                    this@MainActivity,
                    "Gemini Key waa la kaydiyey ✅",
                    Toast.LENGTH_SHORT
                ).show()

                statusText.text = "Gemini: Key diyaar ah ✅"
            }
        }

        val testButton = Button(this).apply {
            text = "Tijaabi Gemini"

            setOnClickListener {
                val key = prefs.getString("gemini_api_key", "")

                if (key.isNullOrBlank()) {
                    Toast.makeText(
                        this@MainActivity,
                        "Marka hore geli API Key",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                statusText.text = "Gemini: Tijaabinaya..."

                GeminiClient.ask(
                    key,
                    "Salaan AlphaAgent. Si kooban ii dheh: Gemini wuu shaqaynayaa."
                ) { answer, error ->

                    runOnUiThread {
                        if (answer != null) {
                            statusText.text =
                                "Gemini: Wuu shaqaynayaa ✅\n\n$answer"
                        } else {
                            statusText.text =
                                "Gemini: Khalad ❌\n$error"
                        }
                    }
                }
            }
        }

        val accessibilityButton = Button(this).apply {
            text = "Fur Accessibility Settings"

            setOnClickListener {
                startActivity(
                    Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                )
            }
        }

        statusText = TextView(this).apply {
            text = "Agent: Diyaar"
            textSize = 17f
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(0, 25, 0, 20)
        }

        root.addView(title)
        root.addView(subtitle)
        root.addView(apiKeyInput)
        root.addView(saveButton)
        root.addView(testButton)
        root.addView(accessibilityButton)
        root.addView(statusText)

        setContentView(root)
    }
}
