package com.iksystem.common.checklist.repository

import com.iksystem.common.checklist.model.ChecklistItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant

/**
 * Repository for managing [ChecklistItem] entities.
 *
 * Provides methods for:
 * - Retrieving checklist items
 * - Counting items across checklists and organizations
 * - Bulk updating completion status for checklist items
 */
@Repository
interface ChecklistItemRepository : JpaRepository<ChecklistItem, Long> {

    /**
     * Retrieves all items for a specific checklist ordered by ID.
     *
     * @param checklistId The checklist ID
     * @return List of checklist items
     */
    fun findAllByChecklistIdOrderByIdAsc(checklistId: Long): List<ChecklistItem>

    /**
     * Retrieves a checklist item by its ID and checklist ID.
     *
     * @param id The item ID
     * @param checklistId The checklist ID
     * @return The matching checklist item or null if not found
     */
    fun findByIdAndChecklistId(id: Long, checklistId: Long): ChecklistItem?

    /**
     * Counts all checklist items within an organization.
     *
     * @param organizationId The organization ID
     * @return Total number of checklist items
     */
    @Query(
        """
        SELECT COUNT(ci.id)
        FROM ChecklistItem ci
        WHERE ci.checklist.organizationId = :organizationId
        """
    )
    fun countAllByOrganizationId(@Param("organizationId") organizationId: Long): Long

    /**
     * Counts how many items in a checklist are not completed.
     *
     * @param checklistId The checklist ID
     * @return Number of incomplete items
     */
    fun countByChecklistIdAndCompletedFalse(checklistId: Long): Long

    /**
     * Counts how many checklist items are completed within an organization.
     *
     * @param organizationId The organization ID
     * @return Number of completed items
     */
    @Query(
        """
        SELECT COUNT(ci.id)
        FROM ChecklistItem ci
        WHERE ci.checklist.organizationId = :organizationId
          AND ci.completed = true
        """
    )
    fun countCompletedByOrganizationId(@Param("organizationId") organizationId: Long): Long

    /**
     * Bulk updates completion status for all items in a checklist.
     *
     * Sets:
     * - completed flag
     * - completedAt timestamp
     * - updatedAt timestamp
     *
     * @param checklistId The checklist ID
     * @param completed New completion state
     * @param completedAt Timestamp for completion (nullable if unsetting)
     * @param updatedAt Timestamp for update
     * @return Number of affected rows
     */
    @Modifying
    @Query(
        """
        UPDATE ChecklistItem ci
        SET ci.completed = :completed,
            ci.completedAt = :completedAt,
            ci.updatedAt = :updatedAt
        WHERE ci.checklist.id = :checklistId
        """
    )
    fun setChecklistCompletion(
        @Param("checklistId") checklistId: Long,
        @Param("completed") completed: Boolean,
        @Param("completedAt") completedAt: Instant?,
        @Param("updatedAt") updatedAt: Instant,
    ): Int
}