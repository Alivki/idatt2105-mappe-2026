package com.iksystem.alcohol.ageverification.model

import com.iksystem.common.user.model.User
import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate

/**
 * Entity representing an age verification shift.
 *
 * A shift tracks a user's activity related to age verification within a specific organization
 * on a given date. It includes lifecycle timestamps, ID check counts, and sign-off status.
 *
 * A shift typically follows this lifecycle:
 * ACTIVE → (optionally updated) → ENDED → SIGNED_OFF
 *
 * @property id Unique identifier of the shift
 * @property organizationId ID of the organization the shift belongs to
 * @property user The user performing the shift
 * @property shiftDate The calendar date the shift is associated with
 * @property startedAt Timestamp when the shift started
 * @property endedAt Timestamp when the shift ended (null if still active)
 * @property idsCheckedCount Number of IDs checked during the shift
 * @property signedOff Indicates whether the shift has been signed off
 * @property signedOffAt Timestamp when the shift was signed off (if applicable)
 * @property status Current lifecycle status of the shift
 * @property createdAt Timestamp when the entity was created
 * @property updatedAt Timestamp when the entity was last updated
 */
@Entity
@Table(name = "age_verification_shifts")
data class AgeVerificationShift(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "organization_id", nullable = false)
    val organizationId: Long,

    /**
     * The user assigned to this shift.
     *
     * This is a many-to-one relationship, as a user can have multiple shifts.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(name = "shift_date", nullable = false)
    val shiftDate: LocalDate,

    @Column(name = "started_at", nullable = false)
    val startedAt: Instant = Instant.now(),

    @Column(name = "ended_at")
    val endedAt: Instant? = null,

    @Column(name = "ids_checked_count", nullable = false)
    val idsCheckedCount: Int = 0,

    /**
     * Indicates whether the shift has been formally signed off.
     *
     * A signed-off shift is considered finalized and should typically not be modified.
     */
    @Column(name = "signed_off", nullable = false)
    val signedOff: Boolean = false,

    @Column(name = "signed_off_at")
    val signedOffAt: Instant? = null,

    /**
     * Current status of the shift.
     *
     * Stored as a string representation of [ShiftStatus].
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: ShiftStatus = ShiftStatus.ACTIVE,

    /**
     * Timestamp when the shift entity was created.
     *
     * This value is immutable after creation.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    /**
     * Timestamp when the shift entity was last updated.
     *
     * Should be updated whenever the shift is modified.
     */
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant = Instant.now(),
)