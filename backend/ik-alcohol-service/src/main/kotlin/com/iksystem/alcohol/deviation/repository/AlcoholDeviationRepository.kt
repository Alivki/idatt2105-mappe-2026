package com.iksystem.alcohol.deviation.repository

import com.iksystem.alcohol.deviation.model.AlcoholDeviation
import com.iksystem.alcohol.deviation.model.AlcoholDeviationStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant

/**
 * Repository for accessing and querying [AlcoholDeviation] entities.
 *
 * Provides methods for:
 * - Retrieving deviations by organization and shift
 * - Filtering by time ranges
 * - Counting deviations for reporting and statistics
 */
@Repository
interface AlcoholDeviationRepository : JpaRepository<AlcoholDeviation, Long> {

    /**
     * Retrieves all deviations for an organization, ordered by report time descending.
     *
     * @param organizationId Organization ID
     * @return List of deviations
     */
    fun findAllByOrganizationIdOrderByReportedAtDesc(organizationId: Long): List<AlcoholDeviation>

    /**
     * Finds a deviation by ID within a specific organization.
     *
     * @param id Deviation ID
     * @param organizationId Organization ID
     * @return The deviation, or null if not found
     */
    fun findByIdAndOrganizationId(id: Long, organizationId: Long): AlcoholDeviation?

    /**
     * Retrieves all deviations linked to a specific age verification shift.
     *
     * @param shiftId Shift ID
     * @return List of deviations
     */
    fun findAllByAgeVerificationShiftId(shiftId: Long): List<AlcoholDeviation>

    /**
     * Retrieves all deviations linked to multiple age verification shifts.
     *
     * @param shiftIds List of shift IDs
     * @return List of deviations
     */
    fun findAllByAgeVerificationShiftIdIn(shiftIds: List<Long>): List<AlcoholDeviation>

    /**
     * Counts deviations linked to a list of shift IDs.
     *
     * @param shiftIds List of shift IDs
     * @return Number of deviations
     */
    fun countByAgeVerificationShiftIdIn(shiftIds: List<Long>): Long

    /**
     * Counts deviations for an organization with a given status.
     *
     * @param organizationId Organization ID
     * @param status Deviation status
     * @return Number of matching deviations
     */
    fun countByOrganizationIdAndStatus(organizationId: Long, status: AlcoholDeviationStatus): Long

    /**
     * Retrieves recent deviations for an organization since a given timestamp.
     *
     * Results are ordered by report time ascending.
     *
     * @param orgId Organization ID
     * @param since Lower bound timestamp (inclusive)
     * @return List of deviations
     */
    @Query("SELECT d FROM AlcoholDeviation d WHERE d.organizationId = :orgId AND d.reportedAt >= :since ORDER BY d.reportedAt ASC")
    fun findRecentByOrganizationId(orgId: Long, since: Instant): List<AlcoholDeviation>

    /**
     * Retrieves deviations for an organization within a time range.
     *
     * @param organizationId Organization ID
     * @param from Start timestamp (inclusive)
     * @param to End timestamp (inclusive)
     * @return List of deviations
     */
    fun findAllByOrganizationIdAndReportedAtBetween(
        organizationId: Long,
        from: Instant,
        to: Instant
    ): List<AlcoholDeviation>
}