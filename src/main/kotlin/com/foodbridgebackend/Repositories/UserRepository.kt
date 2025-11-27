package com.foodbridgebackend.Repositories

import com.foodbridgebackend.Models.User
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface UserRepository: MongoRepository<User, ObjectId> {
    fun findByEmail(email: String): User?

    fun findById(id: String): User?
}