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

/**
 * Entity representing a generated report within the system.
 *
 * Stores metadata about the report such as:
 * - Organization ownership
 * - Reporting period
 * - User who generated the report
 * - Associated document (e.g., PDF stored externally)
 * - Configuration used to generate the report
 *
 * The actual report file is referenced via [document], while the structure
 * and selected sections are stored as JSON in [config].
 */
@Entity
@Table(name = "generated_reports")
data class GeneratedReport(
    /** Unique identifier for the report. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    /** ID of the organization that owns this report. */
    @Column(name = "organization_id", nullable = false)
    val organizationId: Long,

    /** Title of the report. */
    @Column(nullable = false)
    val title: String,

    /** Start date of the reporting period. */
    @Column(name = "period_from", nullable = false)
    val periodFrom: LocalDate,

    /** End date of the reporting period. */
    @Column(name = "period_to", nullable = false)
    val periodTo: LocalDate,

    /**
     * User who generated the report.
     *
     * Loaded lazily to avoid unnecessary joins unless explicitly accessed.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_by_user_id", nullable = false)
    val generatedByUser: User,

    /**
     * Reference to the generated document (e.g., PDF stored in S3).
     *
     * Nullable because a report may exist before the document is generated
     * or if generation fails.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    var document: Document? = null,

    /**
     * JSON string representing the configuration used to generate the report.
     *
     * Includes selected sections and filters applied at generation time.
     */
    @Column(columnDefinition = "JSON", nullable = false)
    val config: String,

    /** Timestamp indicating when the report was generated. */
    @Column(name = "generated_at", nullable = false, updatable = false)
    val generatedAt: Instant = Instant.now(),
)