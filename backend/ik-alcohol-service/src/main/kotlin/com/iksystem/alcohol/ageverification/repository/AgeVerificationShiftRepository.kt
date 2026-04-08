package com.iksystem.alcohol.ageverification.repository

import com.iksystem.alcohol.ageverification.model.AgeVerificationShift
import com.iksystem.alcohol.ageverification.model.ShiftStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface AgeVerificationShiftRepository : JpaRepository<AgeVerificationShift, Long> {

    fun findByIdAndOrganizationId(id: Long, organizationId: Long): AgeVerificationShift?

    fun findByUserIdAndOrganizationIdAndStatus(
        userId: Long,
        organizationId: Long,
        status: ShiftStatus,
    ): AgeVerificationShift?

    fun findByOrganizationIdAndShiftDateBetweenOrderByShiftDateDesc(
        organizationId: Long,
        from: LocalDate,
        to: LocalDate,
    ): List<AgeVerificationShift>

    fun findByOrganizationIdAndShiftDateOrderByStartedAt(
        organizationId: Long,
        shiftDate: LocalDate,
    ): List<AgeVerificationShift>

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

    fun countByOrganizationIdAndStatus(organizationId: Long, status: ShiftStatus): Long

    @Query("SELECT COALESCE(SUM(s.idsCheckedCount), 0) FROM AgeVerificationShift s WHERE s.organizationId = :orgId AND s.status = 'COMPLETED'")
    fun sumIdsCheckedByOrganizationId(orgId: Long): Long
}

interface DailySummaryProjection {
    val shiftDate: LocalDate
    val shiftCount: Long
    val totalIdsChecked: Long
}
