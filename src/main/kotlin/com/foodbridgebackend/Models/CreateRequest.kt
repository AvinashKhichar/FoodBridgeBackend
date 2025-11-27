package com.foodbridgebackend.Models

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("requests")
data class CreateRequest(
    @Id val id: ObjectId = ObjectId.get(),
    val createdBy: String,
    val contactDetails: String,
    val reason: String,
    val donationId: ObjectId,
    val accepted: Boolean = false
)
