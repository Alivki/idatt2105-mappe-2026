package com.iksystem.`ik-common`.user.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserResponse(
    val id: Long,
    val email: String,
    val fullName: String,
    val phoneNumber: String,
    val role: String,
    val organizationId: Long,
    val active: Boolean,
)

data class CreateUserRequest(
    @field:NotBlank @field:Email
    val email: String,
    @field:NotBlank @field:Size(min = 8, max = 128)
    val password: String,
    @field:NotBlank @field:Size(max = 100)
    val fullName: String,
    @field:NotBlank @field:Size(max = 30)
    val phoneNumber: String,
    @field:NotBlank
    val role: String,
)

data class UpdateUserRoleRequest(
    @field:NotBlank
    val role: String,
)