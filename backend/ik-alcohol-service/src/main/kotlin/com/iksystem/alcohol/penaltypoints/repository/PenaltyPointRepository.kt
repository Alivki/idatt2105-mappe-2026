package com.iksystem.alcohol.penaltypoints.repository

import com.iksystem.alcohol.penaltypoints.model.PenaltyPoint
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

/**
 * Repository for accessing and querying [PenaltyPoint] entities.
 *
 * Provides methods for retrieving penalty point entries
 * and calculating aggregated penalty scores for an organization.
 */
@Repository
interface PenaltyPointRepository : JpaRepository<PenaltyPoint, Long> {

    /**
     * Retrieves all penalty point entries for an organization,
     * ordered by creation time descending.
     *
     * @param organizationId Organization ID
     * @return List of penalty point entries
     */
    fun findAllByOrganizationIdOrderByCreatedAtDesc(organizationId: Long): List<PenaltyPoint>

    /**
     * Finds a penalty point entry by ID within a specific organization.
     *
     * @param id Penalty point ID
     * @param organizationId Organization ID
     * @return The entry, or null if not found
     */
    fun findByIdAndOrganizationId(id: Long, organizationId: Long): PenaltyPoint?

    /**
     * Calculates the total penalty points for an organization.
     *
     * Returns 0 if no entries exist.
     *
     * @param organizationId Organization ID
     * @return Sum of penalty points
     */
    @Query("SELECT COALESCE(SUM(p.points), 0) FROM PenaltyPoint p WHERE p.organizationId = :organizationId")
    fun sumPointsByOrganizationId(organizationId: Long): Int
}