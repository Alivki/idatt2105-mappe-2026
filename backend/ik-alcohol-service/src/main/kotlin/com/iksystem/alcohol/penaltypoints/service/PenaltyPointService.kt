package com.iksystem.alcohol.penaltypoints.service

import com.iksystem.alcohol.deviation.model.AlcoholDeviation
import com.iksystem.alcohol.penaltypoints.dto.CreatePenaltyPointRequest
import com.iksystem.alcohol.penaltypoints.dto.PenaltyPointResponse
import com.iksystem.alcohol.penaltypoints.dto.PenaltyPointSummaryResponse
import com.iksystem.alcohol.penaltypoints.model.PenaltyPoint
import com.iksystem.alcohol.penaltypoints.repository.PenaltyPointRepository
import com.iksystem.common.exception.NotFoundException
import com.iksystem.common.security.AuthenticatedUser
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service responsible for managing penalty points related to alcohol deviations.
 *
 * Provides functionality for:
 * - Retrieving penalty point summaries
 * - Creating penalty point entries manually
 * - Deleting entries
 * - Automatically generating penalty points from deviations
 *
 * All operations are scoped to the authenticated user's organization.
 */
@Service
class PenaltyPointService(
    private val repository: PenaltyPointRepository,
) {

    /**
     * Retrieves a summary of penalty points for the authenticated user's organization.
     *
     * Includes:
     * - Total accumulated points
     * - All penalty point entries
     *
     * @param auth The authenticated user
     * @return A summary containing total points and all entries
     */
    @Transactional(readOnly = true)
    fun getSummary(auth: AuthenticatedUser): PenaltyPointSummaryResponse {
        val orgId = auth.requireOrganizationId()
        val entries = repository.findAllByOrganizationIdOrderByCreatedAtDesc(orgId)
        val total = repository.sumPointsByOrganizationId(orgId)
        return PenaltyPointSummaryResponse(
            organizationId = orgId,
            totalPoints = total,
            entries = entries.map { it.toResponse() },
        )
    }

    /**
     * Creates a new penalty point entry manually.
     *
     * The number of points is derived from the selected violation type.
     *
     * @param request Request containing violation details
     * @param auth The authenticated user
     * @return The created penalty point entry
     */
    @Transactional
    fun add(request: CreatePenaltyPointRequest, auth: AuthenticatedUser): PenaltyPointResponse {
        val orgId = auth.requireOrganizationId()
        val entry = repository.save(
            PenaltyPoint(
                organizationId = orgId,
                points = request.violationType.penaltyPoints,
                violationType = request.violationType,
                description = request.description?.trim()?.takeIf { it.isNotEmpty() },
            )
        )
        return entry.toResponse()
    }

    /**
     * Deletes a penalty point entry within the authenticated user's organization.
     *
     * @param id The ID of the penalty point entry
     * @param auth The authenticated user
     * @throws NotFoundException If the entry does not exist
     */
    @Transactional
    fun delete(id: Long, auth: AuthenticatedUser) {
        val orgId = auth.requireOrganizationId()
        val entry = repository.findByIdAndOrganizationId(id, orgId)
            ?: throw NotFoundException("Penalty point entry not found")
        repository.delete(entry)
    }

    /**
     * Auto-creates a penalty point entry linked to an alcohol deviation.
     * Called internally when a deviation with source SJENKEKONTROLL or POLITIRAPPORT is created.
     *
     * @param deviation The deviation that triggered the penalty point
     */
    @Transactional
    fun addForDeviation(deviation: AlcoholDeviation) {
        repository.save(
            PenaltyPoint(
                organizationId = deviation.organizationId,
                alcoholDeviation = deviation,
                points = deviation.deviationType.penaltyPoints,
                violationType = deviation.deviationType,
                description = "Auto: ${deviation.reportSource.name.lowercase()} – ${deviation.deviationType.name}",
            )
        )
    }
}

/**
 * Maps a [PenaltyPoint] entity to a [PenaltyPointResponse].
 *
 * @return The mapped response DTO
 */
private fun PenaltyPoint.toResponse() = PenaltyPointResponse(
    id = id,
    organizationId = organizationId,
    alcoholDeviationId = alcoholDeviation?.id,
    points = points,
    violationType = violationType,
    description = description,
    createdAt = createdAt.toString(),
)