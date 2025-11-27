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



    @GetMapping
    fun getAll(): ResponseEntity<Any> {
        return try {
            val list = listing.getAllDonations()
            ResponseEntity.ok(list)
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf("error" to (e.message ?: "internal error")))
        }
    }

    @GetMapping("/donor/{donorId}")
    fun getByDonor(@PathVariable donorId: String): ResponseEntity<Any> {
        return try {
            val list = listing.getDonationsByDonor(donorId)
            ResponseEntity.ok(list)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf("error" to (e.message ?: "internal error")))
        }
    }

    // --- get single donation by id ---
    @GetMapping("/single/{id}")
    fun getById(@PathVariable id: String): ResponseEntity<Any> {
        return try {
            val donation = listing.getDonationById(id)
            ResponseEntity.ok(donation)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(404).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf("error" to (e.message ?: "internal error")))
        }
    }

    // --- proximity search ---
    // Example: GET /donations/near?lat=18.52&lon=73.85&radiusKm=15
    @GetMapping("/near")
    fun getNear(
        @RequestParam lat: Double,
        @RequestParam lon: Double,
        @RequestParam(required = false, defaultValue = "15.0") radiusKm: Double
    ): ResponseEntity<Any> {
        return try {
            val list = listing.getDonationsNear(lat, lon, radiusKm)
            ResponseEntity.ok(list)
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf("error" to (e.message ?: "internal error")))
        }
    }
}
