package com.iksystem.alcohol.ageverification.repository

import com.iksystem.alcohol.ageverification.model.AgeVerificationShift
import com.iksystem.alcohol.ageverification.model.ShiftStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate

/**
 * Repository for accessing and querying [AgeVerificationShift] entities.
 *
 * Provides methods for:
 * - Retrieving shifts by user, organization, and status
 * - Fetching shifts within date ranges
 * - Aggregating shift data for reporting purposes
 *
 * Custom queries are used for performance-efficient aggregation,
 * especially for reporting endpoints such as daily summaries and statistics.
 */
@Repository
interface AgeVerificationShiftRepository : JpaRepository<AgeVerificationShift, Long> {

    /**
     * Finds a shift by its ID and organization.
     *
     * Ensures that the shift belongs to the specified organization.
     *
     * @param id Shift ID
     * @param organizationId Organization ID
     * @return The matching shift, or null if not found
     */
    fun findByIdAndOrganizationId(id: Long, organizationId: Long): AgeVerificationShift?

    /**
     * Finds a shift for a specific user and organization with a given status.
     *
     * Commonly used to check if a user already has an active shift.
     *
     * @param userId User ID
     * @param organizationId Organization ID
     * @param status Shift status (e.g., ACTIVE)
     * @return The matching shift, or null if none exists
     */
    fun findByUserIdAndOrganizationIdAndStatus(
        userId: Long,
        organizationId: Long,
        status: ShiftStatus,
    ): AgeVerificationShift?

    /**
     * Finds the most recent shift for a user on a given date.
     *
     * Results are ordered by start time in descending order,
     * returning the latest shift first.
     *
     * @param userId User ID
     * @param organizationId Organization ID
     * @param shiftDate The date of the shift
     * @return The most recent shift, or null if none exists
     */
    fun findFirstByUserIdAndOrganizationIdAndShiftDateOrderByStartedAtDesc(
        userId: Long,
        organizationId: Long,
        shiftDate: LocalDate,
    ): AgeVerificationShift?

    /**
     * Retrieves all shifts for an organization within a date range.
     *
     * Results are ordered by shift date in descending order.
     *
     * @param organizationId Organization ID
     * @param from Start date (inclusive)
     * @param to End date (inclusive)
     * @return List of matching shifts
     */
    fun findByOrganizationIdAndShiftDateBetweenOrderByShiftDateDesc(
        organizationId: Long,
        from: LocalDate,
        to: LocalDate,
    ): List<AgeVerificationShift>

    /**
     * Retrieves all shifts for a specific organization on a given date.
     *
     * Results are ordered by start time in ascending order.
     *
     * @param organizationId Organization ID
     * @param shiftDate The date of interest
     * @return List of shifts for that day
     */
    fun findByOrganizationIdAndShiftDateOrderByStartedAt(
        organizationId: Long,
        shiftDate: LocalDate,
    ): List<AgeVerificationShift>

    /**
     * Retrieves aggregated daily summaries for completed shifts within a date range.
     *
     * The result includes:
     * - Number of shifts per day
     * - Total number of IDs checked per day
     *
     * Only shifts with status COMPLETED are included.
     *
     * @param orgId Organization ID
     * @param from Start date (inclusive)
     * @param to End date (inclusive)
     * @return List of daily summary projections
     */
    @Query(
        """
        SELECT s.shiftDate as shiftDate,
               COUNT(s) as shiftCount,
               SUM(s.idsCheckedCount) as totalIdsChecked
        FROM AgeVerificationShift s
        WHERE s.organizationId = :orgId
          AND s.shiftDate BETWEEN :from AND :to
          AND s.status = 'COMPLETED'
        GROUP BY s.shiftDate
        ORDER BY s.shiftDate DESC
        """
    )
    fun findDailySummaries(
        orgId: Long,
        from: LocalDate,
        to: LocalDate,
    ): List<DailySummaryProjection>

    /**
     * Counts the number of shifts for an organization with a given status.
     *
     * @param organizationId Organization ID
     * @param status Shift status
     * @return Number of matching shifts
     */
    fun countByOrganizationIdAndStatus(organizationId: Long, status: ShiftStatus): Long

    /**
     * Calculates the total number of IDs checked across all completed shifts
     * for a given organization.
     *
     * Returns 0 if no matching records are found.
     *
     * @param orgId Organization ID
     * @return Sum of IDs checked
     */
    @Query("SELECT COALESCE(SUM(s.idsCheckedCount), 0) FROM AgeVerificationShift s WHERE s.organizationId = :orgId AND s.status = 'COMPLETED'")
    fun sumIdsCheckedByOrganizationId(orgId: Long): Long
}

/**
 * Projection interface for daily summary aggregation.
 *
 * Used in custom queries to efficiently fetch summarized data
 * without loading full entity objects.
 *
 * @property shiftDate The date of the shifts
 * @property shiftCount Number of shifts on that date
 * @property totalIdsChecked Total number of IDs checked on that date
 */
interface DailySummaryProjection {
    val shiftDate: LocalDate
    val shiftCount: Long
    val totalIdsChecked: Long
}