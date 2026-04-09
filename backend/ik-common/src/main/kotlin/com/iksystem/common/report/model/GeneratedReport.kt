package com.iksystem.common.report.model

import com.iksystem.common.documents.model.Document
import com.iksystem.common.user.model.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(name = "generated_reports")
data class GeneratedReport(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "organization_id", nullable = false)
    val organizationId: Long,

    @Column(nullable = false)
    val title: String,

    @Column(name = "period_from", nullable = false)
    val periodFrom: LocalDate,

    @Column(name = "period_to", nullable = false)
    val periodTo: LocalDate,

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_by_user_id", nullable = false)
    val generatedByUser: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    var document: Document? = null,

    @Column(columnDefinition = "JSON", nullable = false)
    val config: String,

    @Column(name = "generated_at", nullable = false, updatable = false)
    val generatedAt: Instant = Instant.now(),
)
