package com.alpha.agent

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.util.Log

class AlphaAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "AlphaAgent"
        private const val WHATSAPP_PACKAGE = "com.whatsapp"
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val packageName = event.packageName?.toString() ?: return

        if (packageName == WHATSAPP_PACKAGE) {
            Log.d(TAG, "WhatsApp detected: ${event.eventType}")
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Alpha Agent Accessibility Service interrupted")
    }
}
