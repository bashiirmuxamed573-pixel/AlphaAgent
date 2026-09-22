package com.alpha.agent

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo

class AlphaAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "AlphaAgent"
        private const val WHATSAPP = "com.whatsapp"
        private const val WHATSAPP_BUSINESS = "com.whatsapp.w4b"

        private const val PREFS = "alpha_agent"
        private const val API_KEY = "gemini_api_key"
    }

    private val handler = Handler(Looper.getMainLooper())

    private var lastIncomingMessage = ""
    private var lastSentMessage = ""
    private var processing = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "AlphaAgent Accessibility connected")
    }

    override fun onAccessibilityEvent(event: android.view.accessibility.AccessibilityEvent?) {
        if (event == null) return

        val packageName = event.packageName?.toString() ?: return

        if (
            packageName != WHATSAPP &&
            packageName != WHATSAPP_BUSINESS
        ) {
            return
        }

        if (processing) return

        handler.removeCallbacksAndMessages(null)

        handler.postDelayed({
            processWhatsApp()
        }, 700)
    }

    private fun processWhatsApp() {

        val root = rootInActiveWindow ?: return

        val messages = ArrayList<String>()

        collectText(root, messages)

        if (messages.isEmpty()) return

        val incoming = findLatestMessage(messages) ?: return

        if (incoming.isBlank()) return

        if (incoming == lastIncomingMessage) return

        if (incoming == lastSentMessage) return

        val apiKey = getSharedPreferences(
            PREFS,
            MODE_PRIVATE
        ).getString(API_KEY, null)

        if (apiKey.isNullOrBlank()) {
            Log.d(TAG, "Gemini API key lama helin")
            return
        }

        lastIncomingMessage = incoming
        processing = true

        Log.d(TAG, "Incoming WhatsApp: $incoming")

        GeminiClient.ask(
            apiKey,
            incoming
        ) { answer, error ->

            handler.post {

                if (answer != null && answer.isNotBlank()) {

                    Log.d(TAG, "Gemini answer: $answer")

                    sendWhatsAppMessage(answer)

                } else {

                    Log.e(
                        TAG,
                        "Gemini error: $error"
                    )

                    processing = false
                }
            }
        }
    }

    private fun collectText(
        node: AccessibilityNodeInfo,
        output: MutableList<String>
    ) {

        node.text?.toString()
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let {
                output.add(it)
            }

        for (i in 0 until node.childCount) {

            val child = node.getChild(i)

            if (child != null) {

                collectText(child, output)

                child.recycle()
            }
        }
    }

    private fun findLatestMessage(
        texts: List<String>
    ): String? {

        val ignored = setOf(
            "Chats",
            "Calls",
            "Updates",
            "Communities",
            "Search",
            "Type a message",
            "Message",
            "Send",
            "Attach",
            "Camera",
            "Emoji"
        )

        return texts
            .asReversed()
            .firstOrNull { text ->

                val clean = text.trim()

                clean.isNotEmpty() &&
                clean !in ignored &&
                clean != lastSentMessage &&
                clean.length <= 1000 &&
                !clean.matches(
                    Regex("\\d{1,2}:\\d{2}")
                )
            }
    }

    private fun sendWhatsAppMessage(
        message: String
    ) {

        val root = rootInActiveWindow

        if (root == null) {
            processing = false
            return
        }

        val editTexts = root.findAccessibilityNodeInfosByViewId(
            "com.whatsapp:id/entry"
        )

        var input: AccessibilityNodeInfo? =
            editTexts.firstOrNull()

        if (input == null) {

            input = findEditableNode(root)
        }

        if (input == null) {

            Log.e(TAG, "WhatsApp message input lama helin")

            processing = false
            return
        }

        val arguments = Bundle()

        arguments.putCharSequence(
            AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
            message
        )

        val changed = input.performAction(
            AccessibilityNodeInfo.ACTION_SET_TEXT,
            arguments
        )

        input.recycle()

        if (!changed) {

            Log.e(TAG, "Fariinta lama gelin")

            processing = false
            return
        }

        handler.postDelayed({

            val currentRoot = rootInActiveWindow

            if (currentRoot != null) {

                val sendButton =
                    findSendButton(currentRoot)

                if (sendButton != null) {

                    lastSentMessage = message

                    sendButton.performAction(
                        AccessibilityNodeInfo.ACTION_CLICK
                    )

                    sendButton.recycle()

                    Log.d(
                        TAG,
                        "WhatsApp message sent"
                    )
                } else {

                    Log.e(
                        TAG,
                        "Send button lama helin"
                    )
                }
            }

            processing = false

        }, 300)
    }

    private fun findEditableNode(
        node: AccessibilityNodeInfo
    ): AccessibilityNodeInfo? {

        if (
            node.className?.toString() ==
            "android.widget.EditText" &&
            node.isEditable
        ) {
            return node
        }

        for (i in 0 until node.childCount) {

            val child = node.getChild(i)
                ?: continue

            val result = findEditableNode(child)

            if (result != null) {
                child.recycle()
                return result
            }

            child.recycle()
        }

        return null
    }

    private fun findSendButton(
        node: AccessibilityNodeInfo
    ): AccessibilityNodeInfo? {

        val description =
            node.contentDescription
                ?.toString()
                ?.lowercase()
                ?: ""

        if (
            node.isClickable &&
            (
                description.contains("send") ||
                description.contains("dir") ||
                description.contains("enviar")
            )
        ) {
            return node
        }

        for (i in 0 until node.childCount) {

            val child = node.getChild(i)
                ?: continue

            val result = findSendButton(child)

            if (result != null) {
                child.recycle()
                return result
            }

            child.recycle()
        }

        return null
    }

    override fun onInterrupt() {

        Log.d(
            TAG,
            "AlphaAgent Accessibility interrupted"
        )

        processing = false
    }
}
