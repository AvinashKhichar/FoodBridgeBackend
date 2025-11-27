package com.foodbridgebackend.Models

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("password_reset_tokens")
data class PasswordResetToken(
    @Id
    val id: ObjectId = ObjectId.get(),
    val userId: ObjectId,
    val hashedToken: String,
    val expiresAt: Instant,
    val used: Boolean = false
)
