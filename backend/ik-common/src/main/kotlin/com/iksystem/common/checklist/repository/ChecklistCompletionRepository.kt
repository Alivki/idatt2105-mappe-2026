package com.iksystem.common.checklist.repository

import com.iksystem.common.checklist.model.ChecklistCompletion
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant

/**
 * Repository for managing [ChecklistCompletion] entities.
 *
 * Provides methods for querying completion events, counting completions
 * within time ranges, and deleting completions associated with a checklist.
 */
interface ChecklistCompletionRepository : JpaRepository<ChecklistCompletion, Long> {

    /**
     * Counts how many times a checklist has been completed within a given time range.
     *
     * @param checklistId The checklist ID
     * @param from Start of the time range (inclusive)
     * @param to End of the time range (exclusive)
     * @return Number of completion records in the specified range
     */
    @Query("SELECT COUNT(cc) FROM ChecklistCompletion cc WHERE cc.checklist.id = :checklistId AND cc.completedAt >= :from AND cc.completedAt < :to")
    fun countByChecklistIdAndCompletedAtBetween(checklistId: Long, from: Instant, to: Instant): Long

    /**
     * Retrieves all checklist completion events for an organization
     * from a given timestamp, ordered chronologically.
     *
     * @param organizationId The organization ID
     * @param completedAt The lower bound timestamp (inclusive)
     * @return List of checklist completions
     */
    fun findAllByOrganizationIdAndCompletedAtGreaterThanEqualOrderByCompletedAtAsc(
        organizationId: Long,
        completedAt: Instant,
    ): List<ChecklistCompletion>

    /**
     * Deletes all completion records associated with a specific checklist.
     *
     * @param checklistId The checklist ID
     */
    fun deleteAllByChecklistId(checklistId: Long)
}