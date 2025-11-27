package com.foodbridgebackend.Repositories

import com.foodbridgebackend.Models.Donation
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface CreateDoantionRepository : MongoRepository<Donation, ObjectId> {
    fun findByDonationId(donationId: ObjectId) : Donation?

    fun findByTitleIgnoreCaseAndDonorId(title: String, donorId: ObjectId): Donation?

    fun findByDonorId(donorId: ObjectId): List<Donation>

    fun deleteByDonationId(donationId: ObjectId)

    fun findAllByListedTrue(): List<Donation>
}