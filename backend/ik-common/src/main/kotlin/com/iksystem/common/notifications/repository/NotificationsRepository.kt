package com.iksystem.common.notifications.repository

import com.iksystem.common.notifications.model.Notification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

/**
 * Repository for managing [Notification] entities.
 *
 * Provides methods for:
 * - Fetching paginated notifications for a user within an organization
 * - Counting unread notifications
 * - Bulk updating notifications as read
 */
@Repository
interface NotificationsRepository : JpaRepository<Notification, Long> {

    /**
     * Retrieves a paginated list of notifications for a specific user
     * within an organization, ordered by creation time (newest first).
     *
     * @param recipientUserId ID of the recipient user
     * @param organizationId ID of the organization
     * @param pageable Pagination information
     * @return Page of notifications
     */
    fun findAllByRecipientUserIdAndOrganizationIdOrderByCreatedAtDesc(
        recipientUserId: Long,
        organizationId: Long,
        pageable: Pageable
    ): Page<Notification>

    /**
     * Counts the number of unread notifications for a user
     * within a specific organization.
     *
     * @param recipientUserId ID of the recipient user
     * @param organizationId ID of the organization
     * @return Number of unread notifications
     */
    fun countByRecipientUserIdAndOrganizationIdAndIsReadFalse(
        recipientUserId: Long,
        organizationId: Long
    ): Long

    /**
     * Marks all unread notifications as read for a given user
     * within an organization.
     *
     * Sets:
     * - isRead = true
     * - readAt = current timestamp
     *
     * @param userId ID of the recipient user
     * @param organizationId ID of the organization
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.recipientUserId = :userId AND n.organizationId = :organizationId AND n.isRead = false")
    fun markAllAsRead(userId: Long, organizationId: Long)
}