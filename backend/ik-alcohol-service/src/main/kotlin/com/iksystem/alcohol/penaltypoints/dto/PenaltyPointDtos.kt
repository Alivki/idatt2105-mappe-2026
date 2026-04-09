package com.iksystem.alcohol.penaltypoints.dto

import com.iksystem.alcohol.deviation.model.AlcoholDeviationType
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

/**
 * Request DTO for creating a penalty point entry.
 *
 * The violation type determines the associated penalty points.
 */
data class CreatePenaltyPointRequest(
    @field:NotNull(message = "Violation type is required")
    val violationType: AlcoholDeviationType,

    @field:Size(max = 500)
    val description: String? = null,
)

/**
 * Response DTO representing a penalty point entry.
 */
data class PenaltyPointResponse(
    val id: Long,
    val organizationId: Long,
    val alcoholDeviationId: Long?,
    val points: Int,
    val violationType: AlcoholDeviationType,
    val description: String?,
    val createdAt: String,
)

/**
 * Response DTO representing a summary of penalty points for an organization.
 *
 * Includes the total accumulated points and all individual entries.
 */
data class PenaltyPointSummaryResponse(
    val organizationId: Long,
    val totalPoints: Int,
    val entries: List<PenaltyPointResponse>,
)