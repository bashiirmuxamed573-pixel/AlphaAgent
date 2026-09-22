package com.alpha.agent

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val text = TextView(this).apply {
            text = "AlphaAgent\n\nApp-ku wuu furmay."
            textSize = 24f
            setPadding(40, 60, 40, 40)
        }

        setContentView(text)
    }
}
