package com.iksystem.common.notifications.model

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
 * Represents different types of notifications that can be sent to users.
 *
 * These types categorize notifications based on system events,
 * such as compliance alerts, deviations, and reminders.
 */
enum class NotificationType {
    BEVILLING_EXPIRY,
    TRAINING_EXPIRY,
    DEVIATION_CREATED,
    DEVIATION_ASSIGNED,
    CHECKLIST_OVERDUE,
    LOG_REMINDER,
    SYSTEM_ALERT
}

/**
 * Represents the type of domain entity a notification is related to.
 *
 * Used to provide contextual linking between a notification
 * and the resource it references.
 */
enum class ReferenceType {
    ALCOHOL_POLICY,
    TRAINING_LOG,
    DEVIATION,
    CHECKLIST,
    AGE_VERIFICATION_LOG
}

/**
 * Entity representing a notification sent to a user.
 *
 * Notifications are used to inform users about important events,
 * tasks, or system updates. Each notification belongs to an organization
 * and is delivered to a specific recipient.
 *
 * Includes metadata such as:
 * - Type and optional reference to a domain entity
 * - Read status and timestamp
 * - Whether an email notification has been sent
 * - Creation timestamp
 */
@Entity
@Table(name = "notifications")
data class Notification (
    /**
     * Unique identifier for the notification.
     */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    /**
     * ID of the organization the notification belongs to.
     */
    @Column(name = "organization_id", nullable = false)
    val organizationId: Long,

    /**
     * ID of the user who will receive the notification.
     */
    @Column(name = "recipient_id", nullable = false)
    val recipientUserId: Long,

    /**
     * Short title summarizing the notification.
     */
    @Column(nullable = false)
    val title: String,

    /**
     * Detailed message describing the notification.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    val message: String,

    /**
     * Category/type of the notification.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: NotificationType,

    /**
     * Optional reference type indicating what domain entity this notification relates to.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type")
    val referenceType: ReferenceType? = null,

    /**
     * Optional ID of the referenced domain entity.
     */
    @Column(name = "reference_id")
    val referenceId: Long? = null,

    /**
     * Indicates whether the notification has been read.
     */
    @Column(name = "is_read", nullable = false)
    var isRead: Boolean = false,

    /**
     * Timestamp when the notification was read.
     */
    @Column(name = "read_at")
    var readAt: Instant? = null,

    /**
     * Indicates whether an email notification has been sent.
     */
    @Column(name = "email_sent", nullable = false)
    var emailSent: Boolean = false,

    /**
     * Timestamp when the notification was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now())