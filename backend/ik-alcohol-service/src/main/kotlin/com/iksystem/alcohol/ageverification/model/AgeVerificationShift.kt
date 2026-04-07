package com.iksystem.alcohol.ageverification.model

import com.iksystem.common.user.model.User
import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(name = "age_verification_shifts")
data class AgeVerificationShift(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "organization_id", nullable = false)
    val organizationId: Long,

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

    @Column(name = "signed_off", nullable = false)
    val signedOff: Boolean = false,

    @Column(name = "signed_off_at")
    val signedOffAt: Instant? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: ShiftStatus = ShiftStatus.ACTIVE,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant = Instant.now(),
)
