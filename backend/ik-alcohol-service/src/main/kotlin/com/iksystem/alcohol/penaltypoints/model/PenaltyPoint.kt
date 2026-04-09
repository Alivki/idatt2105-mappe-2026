package com.iksystem.alcohol.penaltypoints.model

import com.iksystem.alcohol.deviation.model.AlcoholDeviation
import com.iksystem.alcohol.deviation.model.AlcoholDeviationType
import jakarta.persistence.*
import java.time.Instant

/**
 * Entity representing a penalty point entry for alcohol-related violations.
 *
 * Penalty points can either be:
 * - Automatically generated from an [AlcoholDeviation]
 * - Manually added by administrators
 *
 * Each entry contributes to the organization's total penalty score.
 */
@Entity
@Table(name = "penalty_points")
data class PenaltyPoint(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "organization_id", nullable = false)
    val organizationId: Long,

    /**
     * Optional reference to the deviation that caused this penalty.
     */
    @ManyToOne
    @JoinColumn(name = "alcohol_deviation_id")
    val alcoholDeviation: AlcoholDeviation? = null,

    @Column(nullable = false)
    val points: Int,

    /**
     * Type of violation that determines the penalty points.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "violation_type", nullable = false)
    val violationType: AlcoholDeviationType,

    /**
     * Optional description providing additional context.
     */
    @Column(length = 500)
    val description: String? = null,

    /**
     * Timestamp when the penalty point entry was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),
)