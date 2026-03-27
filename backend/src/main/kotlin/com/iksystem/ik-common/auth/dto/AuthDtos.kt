package com.iksystem.`ik-common`.auth.dto

import com.iksystem.`ik-common`.user.dto.UserResponse
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class LoginRequest (
    @field:NotBlank @field:Email
    val email: String,
    @field:NotBlank
    val password: String,
)

data class RegisterRequest(
    @field:NotBlank @field:Email
    val email: String,
    @field:NotBlank @field:Size(min = 8, max = 128)
    val password: String,
    @field:NotBlank @field:Size(max = 100)
    val fullName: String,
    @field:NotBlank @field:Size(max = 30)
    val phoneNumber: String,
    val organizationId: Long,
)

data class RefreshRequest(
    @field:NotBlank
    val refreshToken: String,
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val user: UserResponse,
)