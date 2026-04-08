package com.iksystem.alcohol.deviation.repository

import com.iksystem.alcohol.deviation.model.AlcoholDeviation
import com.iksystem.alcohol.deviation.model.AlcoholDeviationStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface AlcoholDeviationRepository : JpaRepository<AlcoholDeviation, Long> {
    fun findAllByOrganizationIdOrderByReportedAtDesc(organizationId: Long): List<AlcoholDeviation>
    fun findByIdAndOrganizationId(id: Long, organizationId: Long): AlcoholDeviation?
    fun findAllByAgeVerificationShiftId(shiftId: Long): List<AlcoholDeviation>
    fun findAllByAgeVerificationShiftIdIn(shiftIds: List<Long>): List<AlcoholDeviation>
    fun countByAgeVerificationShiftIdIn(shiftIds: List<Long>): Long
    fun countByOrganizationIdAndStatus(organizationId: Long, status: AlcoholDeviationStatus): Long

    @Query("SELECT d FROM AlcoholDeviation d WHERE d.organizationId = :orgId AND d.reportedAt >= :since ORDER BY d.reportedAt ASC")
    fun findRecentByOrganizationId(orgId: Long, since: Instant): List<AlcoholDeviation>
}
