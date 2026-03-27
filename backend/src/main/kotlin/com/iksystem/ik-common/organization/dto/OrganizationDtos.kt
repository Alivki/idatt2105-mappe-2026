package com.iksystem.`ik-common`.organization.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * Request payload for creating a new organization.
 *
 * @property name Display name of the organization (required, max 200 chars).
 * @property orgNumber Optional external organization/business registry number.
 */
data class CreateOrganizationRequest(
    @field:NotBlank @field:Size(max = 200)
    val name: String,
    val orgNumber: String? = null,
)

/**
 * Response DTO returned when reading organization data.
 *
 * @property id The organization's primary key.
 * @property name The organization's display name.
 * @property orgNumber The external organization number, or `null` if not set.
 */
data class OrganizationResponse(
    val id: Long,
    val name: String,
    val orgNumber: String?,
)