package com.iksystem.common.checklist.service

import com.iksystem.common.exception.BadRequestException
import com.iksystem.common.exception.ForbiddenException
import com.iksystem.common.exception.NotFoundException
import com.iksystem.common.checklist.dto.ChecklistItemResponse
import com.iksystem.common.checklist.dto.ChecklistResponse
import com.iksystem.common.checklist.dto.ChecklistStatus
import com.iksystem.common.checklist.dto.ChecklistStatsResponse
import com.iksystem.common.checklist.dto.CompletionHistoryEntry
import com.iksystem.common.checklist.dto.CreateChecklistItemRequest
import com.iksystem.common.checklist.dto.CreateChecklistRequest
import com.iksystem.common.checklist.dto.SetChecklistCompletionRequest
import com.iksystem.common.checklist.dto.UpdateChecklistItemRequest
import com.iksystem.common.checklist.dto.UpdateChecklistRequest
import com.iksystem.common.checklist.model.Checklist
import com.iksystem.common.checklist.model.ChecklistCompletion
import com.iksystem.common.checklist.model.ChecklistItem
import com.iksystem.common.checklist.repository.ChecklistCompletionRepository
import com.iksystem.common.checklist.repository.ChecklistItemRepository
import com.iksystem.common.checklist.repository.ChecklistRepository
import com.iksystem.common.security.AuthenticatedUser
import com.iksystem.common.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * Service responsible for managing checklists and checklist items.
 *
 * Handles:
 * - Retrieval of checklists and statistics
 * - Creation, updating, and deletion of checklists
 * - Creation, updating, and deletion of checklist items
 * - Completion tracking and history
 *
 * All operations are scoped to the authenticated user's organization.
 */
@Service
class ChecklistService(
    private val checklistRepository: ChecklistRepository,
    private val checklistItemRepository: ChecklistItemRepository,
    private val checklistCompletionRepository: ChecklistCompletionRepository,
    private val userRepository: UserRepository,
) {

    /**
     * Retrieves all checklists for the authenticated user's organization.
     *
     * @param auth The authenticated user
     * @return List of checklist responses
     */
    @Transactional(readOnly = true)
    fun list(auth: AuthenticatedUser): List<ChecklistResponse> {
        val orgId = auth.requireOrganizationId()
        val checklists = checklistRepository.findAllByOrganizationIdOrderByCreatedAtDesc(orgId)

        return checklists.map { checklist ->
            val items = checklistItemRepository.findAllByChecklistIdOrderByIdAsc(checklist.id)
            checklist.toResponse(items)
        }
    }

    /**
     * Retrieves checklist statistics for the organization.
     *
     * @param auth The authenticated user
     * @return Checklist statistics
     */
    @Transactional(readOnly = true)
    fun stats(auth: AuthenticatedUser): ChecklistStatsResponse {
        val orgId = auth.requireOrganizationId()
        return ChecklistStatsResponse(
            activeChecklists = checklistRepository.countActiveOpenByOrganizationId(orgId),
            totalChecklistItems = checklistItemRepository.countAllByOrganizationId(orgId),
        )
    }

    /**
     * Creates a new checklist.
     *
     * @param request Request containing checklist data
     * @param auth The authenticated user
     * @return Created checklist
     */
    @Transactional
    fun createChecklist(request: CreateChecklistRequest, auth: AuthenticatedUser): ChecklistResponse {
        val orgId = auth.requireOrganizationId()
        val checklist = checklistRepository.save(
            Checklist(
                organizationId = orgId,
                name = request.name.trim(),
                description = request.description?.trim()?.takeIf { it.isNotEmpty() },
                frequency = request.frequency,
            )
        )
        return checklist.toResponse(emptyList())
    }

    /**
     * Updates an existing checklist.
     *
     * @param checklistId The checklist ID
     * @param request Request containing updated fields
     * @param auth The authenticated user
     * @return Updated checklist
     */
    @Transactional
    fun updateChecklist(checklistId: Long, request: UpdateChecklistRequest, auth: AuthenticatedUser): ChecklistResponse {
        val orgId = auth.requireOrganizationId()
        val checklist = requireChecklist(checklistId, orgId)

        if (
            request.name == null &&
            request.description == null &&
            request.frequency == null &&
            request.active == null
        ) {
            throw BadRequestException("No fields provided to update")
        }

        val updated = checklistRepository.save(
            checklist.copy(
                name = request.name?.trim()?.takeIf { it.isNotEmpty() } ?: checklist.name,
                description = request.description?.trim()?.takeIf { it.isNotEmpty() } ?: checklist.description,
                frequency = request.frequency ?: checklist.frequency,
                active = request.active ?: checklist.active,
                updatedAt = Instant.now(),
            )
        )

        val items = checklistItemRepository.findAllByChecklistIdOrderByIdAsc(updated.id)
        return updated.toResponse(items)
    }

    /**
     * Deletes a checklist.
     *
     * @param checklistId The checklist ID
     * @param auth The authenticated user
     */
    @Transactional
    fun deleteChecklist(checklistId: Long, auth: AuthenticatedUser) {
        val orgId = auth.requireOrganizationId()
        val checklist = requireChecklist(checklistId, orgId)
        checklistRepository.delete(checklist)
    }

    /**
     * Creates a new checklist item.
     *
     * @param checklistId The checklist ID
     * @param request Request containing item data
     * @param auth The authenticated user
     * @return Created checklist item
     */
    @Transactional
    fun createItem(checklistId: Long, request: CreateChecklistItemRequest, auth: AuthenticatedUser): ChecklistItemResponse {
        val orgId = auth.requireOrganizationId()
        val checklist = requireChecklist(checklistId, orgId)

        val item = checklistItemRepository.save(
            ChecklistItem(
                checklist = checklist,
                title = request.title.trim(),
                description = request.description?.trim()?.takeIf { it.isNotEmpty() },
                completed = request.completed,
                completedAt = if (request.completed) Instant.now() else null,
            )
        )

        return item.toResponse()
    }

    /**
     * Updates a checklist item.
     *
     * @param checklistId The checklist ID
     * @param itemId The item ID
     * @param request Request containing updated fields
     * @param auth The authenticated user
     * @return Updated checklist item
     */
    @Transactional
    fun updateItem(
        checklistId: Long,
        itemId: Long,
        request: UpdateChecklistItemRequest,
        auth: AuthenticatedUser,
    ): ChecklistItemResponse {
        val orgId = auth.requireOrganizationId()
        requireChecklist(checklistId, orgId)

        if (request.title == null && request.description == null && request.completed == null) {
            throw BadRequestException("No fields provided to update")
        }

        val role = auth.requireRole()
        val isManager = role == "ADMIN" || role == "MANAGER"
        if (!isManager && (request.title != null || request.description != null)) {
            throw ForbiddenException("Only managers can edit item title or description")
        }

        val item = checklistItemRepository.findByIdAndChecklistId(itemId, checklistId)
            ?: throw NotFoundException("Checklist item not found")

        val updated = checklistItemRepository.save(
            item.copy(
                title = request.title?.trim()?.takeIf { it.isNotEmpty() } ?: item.title,
                description = request.description?.trim()?.takeIf { it.isNotEmpty() } ?: item.description,
                completed = request.completed ?: item.completed,
                completedAt = when (request.completed) {
                    true -> item.completedAt ?: Instant.now()
                    false -> null
                    null -> item.completedAt
                },
                updatedAt = Instant.now(),
            )
        )

        // Record completion if this was the last item to be checked off
        if (request.completed == true) {
            val checklist = requireChecklist(checklistId, orgId)
            val allItems = checklistItemRepository.findAllByChecklistIdOrderByIdAsc(checklistId)
            if (allItems.all { it.completed }) {
                recordCompletion(checklist, auth)
            }
        }

        return updated.toResponse()
    }

    /**
     * Deletes a checklist item.
     *
     * @param checklistId The checklist ID
     * @param itemId The item ID
     * @param auth The authenticated user
     */
    @Transactional
    fun deleteItem(checklistId: Long, itemId: Long, auth: AuthenticatedUser) {
        val orgId = auth.requireOrganizationId()
        requireChecklist(checklistId, orgId)

        val item = checklistItemRepository.findByIdAndChecklistId(itemId, checklistId)
            ?: throw NotFoundException("Checklist item not found")

        checklistItemRepository.delete(item)
    }

    /**
     * Sets completion status for all items in a checklist.
     *
     * @param checklistId The checklist ID
     * @param request Completion request
     * @param auth The authenticated user
     * @return Updated checklist
     */
    @Transactional
    fun setChecklistCompletion(
        checklistId: Long,
        request: SetChecklistCompletionRequest,
        auth: AuthenticatedUser,
    ): ChecklistResponse {
        val orgId = auth.requireOrganizationId()
        val checklist = requireChecklist(checklistId, orgId)

        val now = Instant.now()
        checklistItemRepository.setChecklistCompletion(
            checklistId = checklist.id,
            completed = request.completed,
            completedAt = if (request.completed) now else null,
            updatedAt = now,
        )

        // Record completion when all items are marked complete
        if (request.completed) {
            recordCompletion(checklist, auth)
        }

        val items = checklistItemRepository.findAllByChecklistIdOrderByIdAsc(checklist.id)
        return checklist.toResponse(items)
    }

    /**
     * Retrieves checklist completion history for a given time period.
     *
     * @param days Number of days to look back
     * @param auth The authenticated user
     * @return List of completion history entries
     */
    @Transactional(readOnly = true)
    fun completionHistory(days: Int, auth: AuthenticatedUser): List<CompletionHistoryEntry> {
        val orgId = auth.requireOrganizationId()
        val from = Instant.now().minus(java.time.Duration.ofDays(days.toLong()))
        return checklistCompletionRepository
            .findAllByOrganizationIdAndCompletedAtGreaterThanEqualOrderByCompletedAtAsc(orgId, from)
            .map { CompletionHistoryEntry(checklistId = it.checklist.id, completedAt = it.completedAt) }
    }

    /**
     * Records a checklist completion event.
     *
     * @param checklist The checklist
     * @param auth The authenticated user
     */
    private fun recordCompletion(checklist: Checklist, auth: AuthenticatedUser) {
        val user = userRepository.findById(auth.userId).orElse(null) ?: return
        checklistCompletionRepository.save(
            ChecklistCompletion(
                checklist = checklist,
                organizationId = checklist.organizationId,
                completedByUser = user,
            )
        )
    }

    /**
     * Retrieves a checklist by ID within the organization.
     *
     * @param checklistId The checklist ID
     * @param organizationId The organization ID
     * @return The checklist
     * @throws NotFoundException If not found
     */
    private fun requireChecklist(checklistId: Long, organizationId: Long): Checklist {
        return checklistRepository.findByIdAndOrganizationId(checklistId, organizationId)
            ?: throw NotFoundException("Checklist not found")
    }
}

/**
 * Maps a [Checklist] and its items to a [ChecklistResponse].
 */
private fun Checklist.toResponse(items: List<ChecklistItem>): ChecklistResponse = ChecklistResponse(
    id = id,
    name = name,
    description = description,
    frequency = frequency,
    active = active,
    source = source,
    itemCount = items.size,
    completedItemCount = items.count { it.completed },
    status = items.toChecklistStatus(),
    items = items.map { it.toResponse() },
)

/**
 * Maps a [ChecklistItem] to a [ChecklistItemResponse].
 */
private fun ChecklistItem.toResponse(): ChecklistItemResponse = ChecklistItemResponse(
    id = id,
    title = title,
    description = description,
    completed = completed,
    completedAt = completedAt,
)

/**
 * Determines the overall status of a checklist based on its items.
 *
 * @return Checklist status
 */
private fun List<ChecklistItem>.toChecklistStatus(): ChecklistStatus {
    if (isEmpty()) {
        return ChecklistStatus.NOT_STARTED
    }

    val completedCount = count { it.completed }
    return when {
        completedCount == 0 -> ChecklistStatus.NOT_STARTED
        completedCount == size -> ChecklistStatus.COMPLETED
        else -> ChecklistStatus.IN_PROGRESS
    }
}