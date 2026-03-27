package com.iksystem.`ik-common`.organization.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateOrganizationRequest(
    @field:NotBlank @field:Size(max = 200)
    val name: String,
    val orgNumber: String? = null,
)

data class OrganizationResponse(
    val id: Long,
    val name: String,
    val orgNumber: String?,
)