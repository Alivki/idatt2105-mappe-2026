package com.iksystem.common.checklist.model

import com.iksystem.common.user.model.User
import jakarta.persistence.*
import java.time.Instant

/**
 * Represents a checklist completion event.
 *
 * Each record indicates that a checklist has been completed by a specific user
 * at a specific point in time within an organization.
 *
 * @property id Unique identifier for the completion record
 * @property checklist The checklist that was completed
 * @property organizationId ID of the organization the checklist belongs to
 * @property completedByUser The user who completed the checklist
 * @property completedAt Timestamp when the checklist was completed
 */
@Entity
@Table(name = "checklist_completions")
data class ChecklistCompletion(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checklist_id", nullable = false)
    val checklist: Checklist,

    @Column(name = "organization_id", nullable = false)
    val organizationId: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completed_by_user_id", nullable = false)
    val completedByUser: User,

    @Column(name = "completed_at", nullable = false)
    val completedAt: Instant = Instant.now(),
)