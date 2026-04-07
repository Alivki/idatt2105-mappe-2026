package com.iksystem.alcohol.deviation.repository

import com.iksystem.alcohol.deviation.model.AlcoholDeviation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AlcoholDeviationRepository : JpaRepository<AlcoholDeviation, Long> {
    fun findAllByOrganizationIdOrderByReportedAtDesc(organizationId: Long): List<AlcoholDeviation>
    fun findByIdAndOrganizationId(id: Long, organizationId: Long): AlcoholDeviation?
    fun findAllByAgeVerificationShiftId(shiftId: Long): List<AlcoholDeviation>
    fun findAllByAgeVerificationShiftIdIn(shiftIds: List<Long>): List<AlcoholDeviation>
    fun countByAgeVerificationShiftIdIn(shiftIds: List<Long>): Long
}
