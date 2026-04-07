package com.iksystem.food.deviation.repository

import com.iksystem.food.deviation.model.FoodDeviation
import com.iksystem.food.deviation.model.FoodDeviationStatus
import com.iksystem.common.deviation.model.DeviationSeverity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface FoodDeviationRepository : JpaRepository<FoodDeviation, Long> {
    fun findAllByOrganizationIdOrderByReportedAtDesc(organizationId: Long): List<FoodDeviation>
    fun findByIdAndOrganizationId(id: Long, organizationId: Long): FoodDeviation?
    fun countByOrganizationIdAndStatus(organizationId: Long, status: FoodDeviationStatus): Long
    fun countByOrganizationIdAndSeverity(organizationId: Long, severity: DeviationSeverity): Long

    @Query("SELECT d FROM FoodDeviation d WHERE d.organizationId = :orgId AND d.reportedAt >= :since ORDER BY d.reportedAt ASC")
    fun findRecentByOrganizationId(orgId: Long, since: Instant): List<FoodDeviation>
}
