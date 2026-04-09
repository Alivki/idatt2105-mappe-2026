package com.iksystem.common.checklist.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

/**
 * Represents a checklist template scoped to a single organization.
 *
 * A checklist defines a reusable set of tasks that can be completed
 * based on a given frequency (e.g., daily, weekly).
 *
 * @property id Unique identifier for the checklist
 * @property organizationId ID of the organization this checklist belongs to
 * @property name Name of the checklist
 * @property description Optional description of the checklist
 * @property frequency How often the checklist should be completed
 * @property active Whether the checklist is currently active
 * @property source Origin of the checklist (e.g., MANUAL, SYSTEM)
 * @property createdAt Timestamp when the checklist was created
 * @property updatedAt Timestamp when the checklist was last updated
 */
@Entity
@Table(name = "checklists")
data class Checklist(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "organization_id", nullable = false)
    val organizationId: Long,

    @Column(nullable = false)
    val name: String,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val frequency: ChecklistFrequency,

    @Column(nullable = false)
    val active: Boolean = true,

    @Column(nullable = false)
    val source: String = "MANUAL",

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant = Instant.now(),
)