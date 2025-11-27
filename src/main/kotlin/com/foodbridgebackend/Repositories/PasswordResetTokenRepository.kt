package com.foodbridgebackend.Repositories

import com.foodbridgebackend.Models.PasswordResetToken
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface PasswordResetTokenRepository :
    MongoRepository<PasswordResetToken, ObjectId> {

    fun findByHashedToken(hashedToken: String): PasswordResetToken?
}
