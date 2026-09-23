package com.alpha.agent

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat

class WhatsAppNotificationService : NotificationListenerService() {

    companion object {
        private const val TAG = "AlphaAgent"
        private const val WHATSAPP = "com.whatsapp"
        private const val PREFS = "alpha_agent"
        private const val API_KEY = "gemini_api_key"

        private var lastMessage = ""
        private var working = false
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "WhatsApp notification listener connected")
    }

    override fun onNotificationPosted(
        sbn: StatusBarNotification
    ) {
        if (sbn.packageName != WHATSAPP) return
        if (working) return

        val notification = sbn.notification ?: return

        val extras = notification.extras ?: return

        val title =
            extras.getString(Notification.EXTRA_TITLE)
                ?: return

        val text =
            extras.getCharSequence(
                Notification.EXTRA_TEXT
            )?.toString()?.trim()
                ?: return

        if (text.isBlank()) return

        if (title.equals("WhatsApp", true)) return

        if (text == lastMessage) return

        val apiKey =
            getSharedPreferences(
                PREFS,
                MODE_PRIVATE
            ).getString(API_KEY, null)

        if (apiKey.isNullOrBlank()) {
            Log.e(TAG, "Gemini API key lama helin")
            return
        }

        lastMessage = text
        working = true

        Log.d(
            TAG,
            "WhatsApp message received: $text"
        )

        GeminiClient.ask(
            apiKey,
            """
            Fariintan WhatsApp ayaa qof kuu soo diray:

            "$text"

            U jawaab sida qof Soomaali ah oo caadi ah.
            Jawaab dabiici ah oo kooban samee.
            Ha sharxin inaad AI tahay.
            Ha ku darin wax hordhac ah.
            Kaliya qor jawaabta qofka loo dirayo.
            """.trimIndent()
        ) { answer, error ->

            if (
                answer != null &&
                answer.isNotBlank()
            ) {
                sendReply(
                    sbn,
                    answer.trim()
                )
            } else {
                Log.e(
                    TAG,
                    "Gemini error: $error"
                )
                working = false
            }
        }
    }

    private fun sendReply(
        sbn: StatusBarNotification,
        message: String
    ) {
        try {
            val actions =
                sbn.notification.actions
                    ?: run {
                        Log.e(
                            TAG,
                            "WhatsApp notification reply action lama helin"
                        )
                        working = false
                        return
                    }

            var replyAction:
                    Notification.Action? = null

            for (action in actions) {

                if (
                    action.remoteInputs != null &&
                    action.remoteInputs.isNotEmpty()
                ) {
                    replyAction = action
                    break
                }
            }

            if (replyAction == null) {
                Log.e(
                    TAG,
                    "WhatsApp RemoteInput lama helin"
                )
                working = false
                return
            }

            val remoteInput =
                replyAction.remoteInputs[0]

            val intent =
                Intent()

            val results =
                Bundle()

            results.putCharSequence(
                remoteInput.resultKey,
                message
            )

            android.app.RemoteInput
                .addResultsToIntent(
                    arrayOf(remoteInput),
                    intent,
                    results
                )

            replyAction.actionIntent.send(
                this,
                0,
                intent
            )

            Log.d(
                TAG,
                "WhatsApp reply sent: $message"
            )

        } catch (e: PendingIntent.CanceledException) {

            Log.e(
                TAG,
                "Reply PendingIntent canceled",
                e
            )

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Reply failed",
                e

            )
        } finally {
            working = false
        }
    }

    override fun onNotificationRemoved(
        sbn: StatusBarNotification
    ) {
        // Nothing
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()

        Log.d(
            TAG,
            "Notification listener disconnected"
        )
    }
}
