package com.iksystem.alcohol.deviation.model

import com.iksystem.common.user.model.User
import jakarta.persistence.*
import java.time.Instant

/**
 * Entity representing an alcohol-related deviation.
 *
 * A deviation captures incidents such as improper alcohol service,
 * missing ID checks, or other policy violations.
 *
 * It includes:
 * - Reporting details (who, when, source)
 * - Description of the incident
 * - Optional immediate actions taken
 * - Root cause analysis
 * - Preventive measures and follow-up responsibility
 * - Current status of the deviation
 *
 * Deviations may optionally be linked to an age verification shift.
 */
@Entity
@Table(name = "alcohol_deviations")
data class AlcoholDeviation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "organization_id", nullable = false)
    val organizationId: Long,

    @Column(name = "reported_at", nullable = false)
    val reportedAt: Instant = Instant.now(),

    /**
     * The user who reported the deviation.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "reported_by_user_id", nullable = false)
    val reportedByUser: User,

    @Enumerated(EnumType.STRING)
    @Column(name = "report_source", nullable = false)
    val reportSource: AlcoholReportSource,

    @Enumerated(EnumType.STRING)
    @Column(name = "deviation_type", nullable = false)
    val deviationType: AlcoholDeviationType,

    /**
     * Detailed description of the deviation.
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    val description: String,

    /**
     * Immediate corrective actions taken at the time of the incident.
     */
    @Column(name = "immediate_action", columnDefinition = "TEXT")
    val immediateAction: String? = null,

    /**
     * Categorized root cause of the deviation.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "causal_analysis")
    val causalAnalysis: AlcoholCausalAnalysis? = null,

    /**
     * Free-text explanation of the root cause.
     */
    @Column(name = "causal_explanation", columnDefinition = "TEXT")
    val causalExplanation: String? = null,

    /**
     * Planned measures to prevent similar deviations in the future.
     */
    @Column(name = "preventive_measures", columnDefinition = "TEXT")
    val preventiveMeasures: String? = null,

    /**
     * Deadline for implementing preventive measures.
     */
    @Column(name = "preventive_deadline")
    val preventiveDeadline: Instant? = null,

    /**
     * User responsible for implementing preventive measures.
     */
    @ManyToOne
    @JoinColumn(name = "preventive_responsible_user_id")
    val preventiveResponsibleUser: User? = null,

    /**
     * Optional reference to the related age verification shift.
     */
    @Column(name = "age_verification_shift_id")
    val ageVerificationShiftId: Long? = null,

    /**
     * Current status of the deviation (e.g., OPEN, CLOSED).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: AlcoholDeviationStatus = AlcoholDeviationStatus.OPEN,

    /**
     * Timestamp when the deviation was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    /**
     * Timestamp when the deviation was last updated.
     */
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant = Instant.now(),
)