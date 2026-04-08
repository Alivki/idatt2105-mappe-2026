package com.iksystem.common.checklist.model

import com.iksystem.common.user.model.User
import jakarta.persistence.*
import java.time.Instant

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
