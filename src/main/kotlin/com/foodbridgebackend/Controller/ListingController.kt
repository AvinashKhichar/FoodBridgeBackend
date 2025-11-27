package com.foodbridgebackend.Controller

import com.foodbridgebackend.Donor.Listing
import org.bson.types.ObjectId
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity

@RestController
@RequestMapping("/donations")
class ListingController (
    private val listing : Listing
){

    data class AddDoantion(
        val title : String,
        val description : String,
        val quantity : Double,
        val latitude : Double,
        val longitude : Double,
        val donorId : String,
        val contactDetails : String
    )

    data class AddRequest(
        val donationId : ObjectId,
        val name : String,
        val contact : String,
        val reason : String
    )

    data class UpdateDonation(
        val title : String?,
        val description : String?,
        val quantity : Double?,
        val latitude : Double?,
        val longitude : Double?,
        val contactDetails : String?
    )

    @PostMapping("/add")
    fun createDoantion(
        @RequestBody donation : AddDoantion
    ){
        listing.createdonation(donation.title, donation.description, donation.quantity, donation.latitude, donation.longitude, donation.donorId,donation.contactDetails )
    }

    @PostMapping("/requests")
    fun createRequest(
        @RequestBody request : AddRequest){
        listing.request(request.donationId, request.name, request.contact, request.reason)
    }

    @PutMapping("/{id}")
    fun updateDonation(
        @PathVariable id: String,
        @RequestBody body: UpdateDonation
    ): ResponseEntity<Any> {
        return try {
            val updated = listing.updateDonation(
                donationIdStr = id,
                title = body.title,
                description = body.description,
                quantity = body.quantity,
                latitude = body.latitude,
                longitude = body.longitude,
                contactDetails = body.contactDetails
            )
            ResponseEntity.ok(updated)
        } catch (e: Exception) {
            ResponseEntity.status(404).body(mapOf("error" to (e.message ?: "error")))
        }
    }

    @DeleteMapping("/{id}")
    fun deleteDonation(
        @PathVariable id: String
    ): ResponseEntity<Any> {
        return try {
            val removed = listing.deleteDonation(id)
            if (removed) ResponseEntity.ok(mapOf("deleted" to true))
            else ResponseEntity.ok(mapOf("deleted" to false, "message" to "already unlisted"))
        } catch (e: Exception) {
            ResponseEntity.status(404).body(mapOf("error" to (e.message ?: "error")))
        }
    }
}
