package com.foodbridgebackend.Models

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("donations")
data class Donation(
    @Id val donationId: ObjectId = ObjectId.get(),
    val title: String,
    val description: String,
    val quatity: Double,
    val latitude: Double,
    val longitude: Double,
    val donorId: ObjectId,
    val contactDetails: String,
    val listed: Boolean = true
)