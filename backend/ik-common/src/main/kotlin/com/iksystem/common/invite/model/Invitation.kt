package com.iksystem.common.invite.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

/**
 * Entity representing an invitation to join an organization.
 *
 * Invitations are token-based and include:
 * - Target email and assigned role
 * - The user who sent the invitation
 * - Expiration timestamp
 * - Optional acceptance timestamp
 *
 * Used in the onboarding flow to allow new or existing users
 * to join an organization securely.
 */
@Entity
@Table(name = "invitations")
data class Invitation (
    /**
     * Unique identifier for the invitation.
     */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    /**
     * ID of the organization the user is invited to.
     */
    @Column(name = "organization_id", nullable = false)
    val organizationId: Long,

    /**
     * Email address of the invited user.
     */
    @Column(nullable = false)
    val email: String,

    /**
     * Role assigned to the user upon acceptance.
     */
    @Column(nullable = false)
    val role: String,

    /**
     * Unique token used to identify and validate the invitation.
     */
    @Column(nullable = false)
    val token: String,

    /**
     * ID of the user who sent the invitation.
     */
    @Column(name = "invited_by_id", nullable = false)
    val invitedById: Long,

    /**
     * Timestamp when the invitation expires.
     */
    @Column(name = "expires_at", nullable = false)
    val expiresAt: Instant,

    /**
     * Timestamp when the invitation was accepted.
     * Null if the invitation has not yet been accepted.
     */
    @Column(name = "accepted_at")
    var acceptedAt: Instant? = null,
)