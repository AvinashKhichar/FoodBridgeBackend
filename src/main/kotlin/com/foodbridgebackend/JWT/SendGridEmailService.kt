package com.foodbridgebackend.JWT

import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

@Service
class SendGridEmailService {
    private val apiKey: String = System.getenv("SENDGRID_API_KEY")
        ?: throw IllegalStateException("SENDGRID_API_KEY not set")
    private val sender: String = System.getenv("SENDER_EMAIL")
        ?: throw IllegalStateException("SENDER_EMAIL not set")

    private val client: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()

    @Async("taskExecutor")
    fun sendResetEmail(to: String, resetLink: String) {
        val json = """
        {
          "personalizations": [
            {
              "to": [
                { "email": "$to" }
              ]
            }
          ],
          "from": { "email": "$sender" },
          "subject": "Reset your FoodBridge password",
          "content": [
            {
              "type": "text/plain",
              "value": "Click this link to reset your password:\n\n$resetLink\n\nThe link expires in 15 minutes."
            }
          ]
        }
        """.trimIndent()

        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.sendgrid.com/v3/mail/send"))
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .timeout(Duration.ofSeconds(10))
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build()

        client.sendAsync(request, HttpResponse.BodyHandlers.discarding())
            .whenComplete { resp, err ->
                if (err != null) {
                    // Log the error (use your logger)
                    println("SendGrid send failed: ${err.message}")
                } else if (resp.statusCode() !in 200..299) {
                    println("SendGrid responded ${resp.statusCode()}")
                }
            }
    }
}
