package com.iksystem.food.deviation.repository

import com.iksystem.food.deviation.model.FoodDeviation
import com.iksystem.food.deviation.model.FoodDeviationStatus
import com.iksystem.common.deviation.model.DeviationSeverity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant

/**
 * Repository for accessing food deviation data.
 */
@Repository
interface FoodDeviationRepository : JpaRepository<FoodDeviation, Long> {

    /**
     * Finds all deviations for an organization ordered by reported date.
     */
    fun findAllByOrganizationIdOrderByReportedAtDesc(organizationId: Long): List<FoodDeviation>

    /**
     * Finds a deviation by ID and organization.
     */
    fun findByIdAndOrganizationId(id: Long, organizationId: Long): FoodDeviation?

    /**
     * Counts deviations by status.
     */
    fun countByOrganizationIdAndStatus(organizationId: Long, status: FoodDeviationStatus): Long

    /**
     * Counts deviations by severity.
     */
    fun countByOrganizationIdAndSeverity(organizationId: Long, severity: DeviationSeverity): Long

    /**
     * Finds recent deviations since a given timestamp.
     */
    @Query("SELECT d FROM FoodDeviation d WHERE d.organizationId = :orgId AND d.reportedAt >= :since ORDER BY d.reportedAt ASC")
    fun findRecentByOrganizationId(orgId: Long, since: Instant): List<FoodDeviation>

    /**
     * Finds deviations within a time range.
     */
    fun findAllByOrganizationIdAndReportedAtBetween(organizationId: Long, from: Instant, to: Instant): List<FoodDeviation>
}