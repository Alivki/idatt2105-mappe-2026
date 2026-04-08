package com.iksystem.common.report.repository

import com.iksystem.common.report.model.GeneratedReport
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface GeneratedReportRepository : JpaRepository<GeneratedReport, Long> {
    fun findAllByOrganizationIdOrderByGeneratedAtDesc(organizationId: Long): List<GeneratedReport>
    fun findByIdAndOrganizationId(id: Long, organizationId: Long): GeneratedReport?
}
