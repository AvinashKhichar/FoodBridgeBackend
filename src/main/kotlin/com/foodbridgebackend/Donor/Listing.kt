package com.foodbridgebackend.Donor

import com.foodbridgebackend.Models.CreateRequest
import com.foodbridgebackend.Models.Donation
import com.foodbridgebackend.Repositories.CreateDoantionRepository
import com.foodbridgebackend.Repositories.CreateRequestRepository
import com.foodbridgebackend.Repositories.UserRepository
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import kotlin.math.abs

@Service
class Listing(
    private val userRepo : UserRepository,
    private val donationRepo : CreateDoantionRepository,
    private val requestRepo : CreateRequestRepository
) {

    fun createdonation(
        title : String,
        description : String,
        quantity : Double,
        latitude : Double,
        longitude : Double,
        donorId : String,
        contactDetails : String
    ): Donation {
        val user = userRepo.findById(donorId)   ?: throw Exception("User not found")
        checkDuplicateDonation(title, quantity, latitude, longitude, user.id)
        return donationRepo.save(

            Donation(
                title = title.trim(),
                description = description.trim(),
                quatity = quantity.toDouble(),
                latitude = latitude.toDouble(),
                longitude = longitude.toDouble(),
                donorId = user.id,
                contactDetails = contactDetails.trim()
            )
        )

    }

    fun updateDonation(
        donationIdStr: String,
        title: String?,
        description: String?,
        quantity: Double?,
        latitude: Double?,
        longitude: Double?,
        contactDetails: String?
    ): Donation {
        val donationId = ObjectId(donationIdStr)
        val existing = donationRepo.findByDonationId(donationId) ?: throw Exception("Donation not found")
        val updated = existing.copy(
            title = title?.trim() ?: existing.title,
            description = description?.trim() ?: existing.description,
            quatity = quantity ?: existing.quatity,
            latitude = latitude ?: existing.latitude,
            longitude = longitude ?: existing.longitude,
            contactDetails = contactDetails?.trim() ?: existing.contactDetails
        )
        return donationRepo.save(updated)
    }

    fun deleteDonation(donationIdStr: String): Boolean {
        val donationId = ObjectId(donationIdStr)
        val existing = donationRepo.findByDonationId(donationId) ?: throw Exception("Donation not found")
        if (!existing.listed) return false // already unlisted
        val updated = existing.copy(listed = false)
        donationRepo.save(updated)
        return true
    }

    private fun checkDuplicateDonation(
        title: String,
        quantity: Double,
        latitude: Double,
        longitude: Double,
        donorObjectId: ObjectId
    ) {

        val exact = donationRepo.findByTitleIgnoreCaseAndDonorId(title.trim(), donorObjectId)
        if (exact != null && exact.listed) {
            throw Exception("Duplicate donation: same title already listed by this donor")
        }


        val candidates = donationRepo.findByDonorId(donorObjectId)
        val latThreshold = 0.02
        val lonThreshold = 0.02
        val quantityTolerance = 0.2 * quantity

        for (c in candidates) {
            if (!c.listed) continue
            if (abs(c.latitude - latitude) <= latThreshold &&
                abs(c.longitude - longitude) <= lonThreshold &&
                kotlin.math.abs(c.quatity - quantity) <= quantityTolerance) {
                throw Exception("Potential duplicate donation: similar donation already listed nearby")
            }
        }
    }


    fun request(
        donationId : ObjectId,
        name : String,
        contact : String,
        reason : String
    ): CreateRequest{
        val user = donationRepo.findByDonationId(donationId) ?: throw Exception("User not found")

        val request = CreateRequest(
            donationId = user.donationId,
            createdBy = name.trim(),
            contactDetails = contact.trim(),
            reason = reason.trim()
        )
        return requestRepo.save(request)

    }
}