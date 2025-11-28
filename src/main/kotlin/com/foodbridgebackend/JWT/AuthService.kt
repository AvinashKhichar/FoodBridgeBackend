package com.foodbridgebackend.JWT

import com.foodbridgebackend.Models.PasswordResetToken
import com.foodbridgebackend.Models.RefreshToken
import com.foodbridgebackend.Models.User
import com.foodbridgebackend.Repositories.PasswordResetTokenRepository
import com.foodbridgebackend.Repositories.RefereshTokenRepository
import com.foodbridgebackend.Repositories.UserRepository
import org.bson.types.ObjectId
import org.springframework.http.HttpStatusCode
import org.springframework.mail.SimpleMailMessage
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.security.MessageDigest
import java.time.Instant
import java.util.Base64
import java.util.UUID

@Service
class AuthService(
    private val jwtService: JwtService,
    private val repository: UserRepository,
    private val hashEncoder: HashEncoder,
    private val refreshTokenRepo: RefereshTokenRepository,
    private val userRepository: UserRepository,
    private val passwordResetTokenRepo: PasswordResetTokenRepository,
    private val emailService: SendGridEmailService
) {

    data class TokenPair(
        val accessToken : String,
        val refreshToken : String
    )
    fun register(name: String, username: String, email: String, password: String): User {
        return repository.save(
            User(
                name = name,
                username = username,
                email = email,
                hashedPassword = hashEncoder.encode(password)
            )
        )
    }

    fun login(email: String, password: String): TokenPair {
        val user = repository.findByEmail(email)
            ?: throw BadCredentialsException("User doesn't exist with this mail")


        if(!hashEncoder.matches(password, user.hashedPassword)){
            throw BadCredentialsException("Invalid crecdential")
        }

        val newAccess = jwtService.generateAccessToken(user.id.toHexString())
        val newRefresh = jwtService.generateRefreshToken(user.id.toHexString())

        storeRefreshToken(user.id, newRefresh)

        return TokenPair(
            accessToken = newAccess,
            refreshToken = newRefresh
        )
    }
        @Transactional
    fun refreshToken(refreshToken: String): TokenPair {
        if(!jwtService.validateAccessToken(refreshToken)){
            throw IllegalArgumentException("Invalid refresh token")
        }

        val userId = jwtService.getUserIdFromToken(refreshToken)
        val user = userRepository.findById(ObjectId(userId)).orElseThrow{
           IllegalArgumentException("invalide refresh token")
        }

        val hashed = hashToken(refreshToken)
        refreshTokenRepo.findByUserIdAndHashedToken(user.id, hashed)
            ?: throw ResponseStatusException(HttpStatusCode.valueOf(401), "Refresh token not recognised")

        refreshTokenRepo.deleteByUserIdAndHashedToken(user.id, hashed)

        val newAccessToken = jwtService.generateAccessToken(userId)
        val newRefreshToken = jwtService.generateRefreshToken(userId)

        storeRefreshToken(user.id, newRefreshToken)

        return TokenPair(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken
        )
    }

    private fun storeRefreshToken(userId: ObjectId, rawRefreshToken: String){
        val hashed = hashToken(rawRefreshToken)
        val expiryMs = jwtService.refreshTokenValidity
        val expiresAt = Instant.now().plusMillis(expiryMs)

        refreshTokenRepo.save(
            RefreshToken(
                userId = userId,
                expiresAt = expiresAt,
                hashedToken = hashed
            )
        )
    }

    private fun hashToken(token: String): String{
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.encodeToByteArray())
        return Base64.getEncoder().encodeToString(hashBytes)
    }


    fun requestPasswordReset(email: String) {
        val user = repository.findByEmail(email) ?: return  // don't reveal existence

        val rawToken = UUID.randomUUID().toString()
        val hashed = hashToken(rawToken)

        val token = PasswordResetToken(
            userId = user.id,
            hashedToken = hashed,
            expiresAt = Instant.now().plusSeconds(15 * 60) // 15 minutes
        )

        passwordResetTokenRepo.save(token)

        val resetLink = "https://foodbridgebackend-1.onrender.com/auth/reset-password?token=$rawToken"

        emailService.sendResetEmail(user.email, resetLink)
    }

    fun resetPassword(rawToken: String, newPassword: String) {
        val hashed = hashToken(rawToken)

        val tokenEntity = passwordResetTokenRepo.findByHashedToken(hashed)
            ?: throw IllegalArgumentException("Invalid reset token")

        if (tokenEntity.used || tokenEntity.expiresAt.isBefore(Instant.now())) {
            throw IllegalArgumentException("Reset token expired or already used")
        }

        val user = userRepository.findById(tokenEntity.userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        val updatedUser = user.copy(hashedPassword = hashEncoder.encode(newPassword))
        userRepository.save(updatedUser)

        passwordResetTokenRepo.save(tokenEntity.copy(used = true))
    }


}