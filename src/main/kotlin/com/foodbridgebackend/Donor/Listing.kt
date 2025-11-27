package com.foodbridgebackend.Donor

import com.foodbridgebackend.Models.CreateRequest
import com.foodbridgebackend.Models.Donation
import com.foodbridgebackend.Repositories.CreateDoantionRepository
import com.foodbridgebackend.Repositories.CreateRequestRepository
import com.foodbridgebackend.Repositories.UserRepository
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

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



    //get part


    fun getAllDonations(): List<Donation> {
        return donationRepo.findAllByListedTrue()
    }

    fun getDonationsByDonor(donorIdStr: String): List<Donation> {
        val donorObjectId = try {
            ObjectId(donorIdStr)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid donorId format: must be 24-hex characters")
        }
        return donationRepo.findByDonorId(donorObjectId).filter { it.listed }
    }

    fun getDonationById(donationIdStr: String): Donation {
        val donationObjectId = try {
            ObjectId(donationIdStr)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid donationId format: must be 24-hex characters")
        }
        return donationRepo.findByDonationId(donationObjectId)
            ?: throw NoSuchElementException("Donation not found for id $donationIdStr")
    }

    fun getDonationsNear(latitude: Double, longitude: Double, radiusKm: Double = 15.0): List<Donation> {
        val all = donationRepo.findAllByListedTrue()
        return all.filter { donation ->
            val d = haversineKm(latitude, longitude, donation.latitude, donation.longitude)
            d <= radiusKm
        }
    }


    private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }
}