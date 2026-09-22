package com.alpha.agent

import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object GeminiClient {

    private const val MODEL = "gemini-3.8-flash"

    private const val ENDPOINT =
        "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    fun ask(
        apiKey: String,
        message: String,
        callback: (String?, String?) -> Unit
    ) {
        Thread {
            var connection: HttpURLConnection? = null

            try {
                connection = URL(ENDPOINT)
                    .openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.connectTimeout = 20000
                connection.readTimeout = 60000
                connection.doOutput = true

                connection.setRequestProperty(
                    "x-goog-api-key",
                    apiKey
                )

                connection.setRequestProperty(
                    "Content-Type",
                    "application/json"
                )

                val prompt = """
                    Waxaad tahay AlphaAgent.

                    Had iyo jeer ugu jawaab Af-Soomaali dabiici ah.
                    Jawaabta ka dhig mid kooban oo ku habboon WhatsApp.
                    Si edeb leh oo caqli leh uga jawaab.

                    Fariinta qofka:
                    $message
                """.trimIndent()

                val body = JSONObject().apply {
                    put(
                        "contents",
                        JSONArray().put(
                            JSONObject().apply {
                                put(
                                    "parts",
                                    JSONArray().put(
                                        JSONObject().apply {
                                            put("text", prompt)
                                        }
                                    )
                                )
                            }
                        )
                    )
                }

                connection.outputStream.use { output ->
                    output.write(
                        body.toString().toByteArray(Charsets.UTF_8)
                    )
                }

                val code = connection.responseCode

                val stream =
                    if (code in 200..299) {
                        connection.inputStream
                    } else {
                        connection.errorStream
                    }

                val response = BufferedReader(
                    InputStreamReader(stream, Charsets.UTF_8)
                ).use { it.readText() }

                if (code !in 200..299) {
                    callback(
                        null,
                        "Gemini API error $code"
                    )
                    return@Thread
                }

                val json = JSONObject(response)

                val text = json
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                    .trim()

                callback(text, null)

            } catch (e: Exception) {
                callback(
                    null,
                    e.message ?: "Gemini connection error"
                )
            } finally {
                connection?.disconnect()
            }
        }.start()
    }
}
