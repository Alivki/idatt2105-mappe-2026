package com.iksystem.common.checklist.repository

import com.iksystem.common.checklist.model.Checklist
import com.iksystem.common.checklist.model.ChecklistFrequency
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Repository for managing [Checklist] entities.
 *
 * Provides methods for:
 * - Retrieving checklists by organization and attributes
 * - Counting checklists based on status, frequency, and activity
 * - Managing system-generated checklists
 * - Identifying incomplete checklists for scheduled processing
 */
@Repository
interface ChecklistRepository : JpaRepository<Checklist, Long> {

    /**
     * Retrieves all checklists for an organization ordered by creation time (descending).
     *
     * @param organizationId The organization ID
     * @return List of checklists
     */
    fun findAllByOrganizationIdOrderByCreatedAtDesc(organizationId: Long): List<Checklist>

    /**
     * Retrieves a checklist by ID within a specific organization.
     *
     * @param id The checklist ID
     * @param organizationId The organization ID
     * @return The checklist if found, otherwise null
     */
    fun findByIdAndOrganizationId(id: Long, organizationId: Long): Checklist?

    /**
     * Counts active checklists that are considered "open".
     *
     * A checklist is open if:
     * - It has no items, OR
     * - It has at least one incomplete item
     *
     * @param organizationId The organization ID
     * @return Number of active open checklists
     */
    @Query(
        """
        SELECT COUNT(c.id)
        FROM Checklist c
        WHERE c.organizationId = :organizationId
          AND c.active = true
          AND (
                NOT EXISTS (
                    SELECT ciAll.id
                    FROM ChecklistItem ciAll
                    WHERE ciAll.checklist.id = c.id
                )
                OR EXISTS (
                    SELECT ciOpen.id
                    FROM ChecklistItem ciOpen
                    WHERE ciOpen.checklist.id = c.id
                      AND ciOpen.completed = false
                )
          )
        """
    )
    fun countActiveOpenByOrganizationId(@Param("organizationId") organizationId: Long): Long

    /**
     * Counts checklists by organization and active status.
     *
     * @param organizationId The organization ID
     * @param active Whether the checklist is active
     * @return Number of matching checklists
     */
    fun countByOrganizationIdAndActive(organizationId: Long, active: Boolean): Long

    /**
     * Counts checklists by organization, frequency, and active status.
     *
     * @param organizationId The organization ID
     * @param frequency The checklist frequency
     * @param active Whether the checklist is active
     * @return Number of matching checklists
     */
    fun countByOrganizationIdAndFrequencyAndActive(organizationId: Long, frequency: ChecklistFrequency, active: Boolean): Long

    /**
     * Retrieves checklists by organization, frequency, and active status.
     *
     * @param organizationId The organization ID
     * @param frequency The checklist frequency
     * @param active Whether the checklist is active
     * @return List of matching checklists
     */
    fun findAllByOrganizationIdAndFrequencyAndActive(organizationId: Long, frequency: ChecklistFrequency, active: Boolean): List<Checklist>

    /**
     * Retrieves all checklists by organization and source.
     *
     * @param organizationId The organization ID
     * @param source The checklist source (e.g., SYSTEM, MANUAL)
     * @return List of matching checklists
     */
    fun findAllByOrganizationIdAndSource(organizationId: Long, source: String): List<Checklist>

    /**
     * Deletes all checklists for an organization with a given source.
     *
     * Typically used for cleaning up system-generated checklists.
     *
     * @param organizationId The organization ID
     * @param source The checklist source
     */
    fun deleteAllByOrganizationIdAndSource(organizationId: Long, source: String)

    /**
     * Retrieves active checklists with a given frequency that have incomplete items.
     *
     * Used for identifying checklists that still require completion.
     *
     * @param frequency The checklist frequency
     * @return List of incomplete checklists
     */
    @Query(
        """
        SELECT c FROM Checklist c
        WHERE c.frequency = :frequency
          AND c.active = true
          AND EXISTS (
              SELECT ci.id FROM ChecklistItem ci
              WHERE ci.checklist.id = c.id
                AND ci.completed = false
          )
        """
    )
    fun findIncompleteByFrequency(@Param("frequency") frequency: ChecklistFrequency): List<Checklist>
}