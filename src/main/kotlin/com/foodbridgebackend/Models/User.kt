package com.foodbridgebackend.Models

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("users")
data class User(
    val name : String,
    val username : String,
    val email : String,
    val hashedPassword :String,
    @Id val id : ObjectId = ObjectId.get()

)
