package com.foodbridgebackend.Repositories

import com.foodbridgebackend.Models.CreateRequest
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface CreateRequestRepository: MongoRepository<CreateRequest, ObjectId> {

    fun findRequestById(id: ObjectId): CreateRequest?
}