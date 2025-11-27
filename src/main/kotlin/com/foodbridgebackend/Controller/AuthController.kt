package com.foodbridgebackend.Controller

import com.foodbridgebackend.JWT.AuthService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
) {
    data class AuthRegRequest(
        val name : String,
        val username: String,
        val email: String,
        val password: String
    )

    data class AuthRequest(
        val email: String,
        val password: String
    )

    data class RefreshRequest(
        val refreshToken: String
    )

    data class ForgotPasswordRequest(
        val email: String
    )

    data class ResetPasswordRequest(
        val token: String, val newPassword: String
    )

    @PostMapping("/register")
    fun register(
        @RequestBody body : AuthRegRequest
    ){
        authService.register(body.name, body.username,body.email, body.password)
    }

    @PostMapping("/login")
    fun login(
        @RequestBody body : AuthRequest
    ): AuthService.TokenPair{
        return authService.login(body.email, body.password)
    }

    @PostMapping("/refresh")
    fun refresh(
        @RequestBody body : RefreshRequest
    ): AuthService.TokenPair{
        return authService.refreshToken(body.refreshToken)
    }

    @PostMapping("/forgot-password")
    fun forgotPassword(@RequestBody body: ForgotPasswordRequest) {
        authService.requestPasswordReset(body.email)
    }

    @PostMapping("/reset-password")
    fun resetPassword(@RequestBody body: ResetPasswordRequest) {
        authService.resetPassword(body.token, body.newPassword)
    }
}