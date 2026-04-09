package com.iksystem.food.haccpsetup.service

import com.iksystem.common.checklist.dto.ChecklistItemResponse
import com.iksystem.common.checklist.dto.ChecklistResponse
import com.iksystem.common.checklist.dto.ChecklistStatus
import com.iksystem.common.checklist.model.Checklist
import com.iksystem.common.checklist.model.ChecklistItem
import com.iksystem.common.checklist.repository.ChecklistItemRepository
import com.iksystem.common.checklist.repository.ChecklistRepository
import com.iksystem.common.security.AuthenticatedUser
import com.iksystem.food.haccpsetup.dto.GenerationSummary
import com.iksystem.food.haccpsetup.dto.HaccpSetupRequest
import com.iksystem.food.haccpsetup.dto.HaccpSetupResponse
import com.iksystem.food.haccpsetup.templates.ChecklistTemplates
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service for generating HACCP checklists based on setup input.
 */
@Service
class HaccpSetupService(
    private val checklistRepository: ChecklistRepository,
    private val checklistItemRepository: ChecklistItemRepository,
) {

    /**
     * Generates checklists from matching templates and replaces previous wizard data.
     */
    @Transactional
    fun generateChecklists(request: HaccpSetupRequest, auth: AuthenticatedUser): HaccpSetupResponse {
        val orgId = auth.requireOrganizationId()

        checklistRepository.deleteAllByOrganizationIdAndSource(orgId, HACCP_WIZARD_SOURCE)

        val matchingTemplates = ChecklistTemplates.all.filter { it.condition(request) }

        val generatedChecklists = matchingTemplates.map { template ->
            val checklist = checklistRepository.save(
                Checklist(
                    organizationId = orgId,
                    name = template.name,
                    description = template.description,
                    frequency = template.frequency,
                    source = HACCP_WIZARD_SOURCE,
                )
            )

            val items = template.items.mapIndexed { index, templateItem ->
                checklistItemRepository.save(
                    ChecklistItem(
                        checklist = checklist,
                        title = templateItem.title,
                        description = templateItem.description,
                        sortOrder = index,
                    )
                )
            }

            checklist.toResponse(items)
        }

        val totalItems = generatedChecklists.sumOf { it.itemCount }

        return HaccpSetupResponse(
            generatedChecklists = generatedChecklists,
            summary = GenerationSummary(
                totalChecklists = generatedChecklists.size,
                totalItems = totalItems,
            ),
        )
    }

    companion object {
        /**
         * Source identifier for HACCP wizard-generated checklists.
         */
        const val HACCP_WIZARD_SOURCE = "HACCP_WIZARD"
    }
}

/**
 * Maps a Checklist entity and its items to a response DTO.
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
    status = when {
        items.isEmpty() -> ChecklistStatus.NOT_STARTED
        items.all { it.completed } -> ChecklistStatus.COMPLETED
        items.any { it.completed } -> ChecklistStatus.IN_PROGRESS
        else -> ChecklistStatus.NOT_STARTED
    },
    items = items.map { item ->
        ChecklistItemResponse(
            id = item.id,
            title = item.title,
            description = item.description,
            completed = item.completed,
            completedAt = item.completedAt,
        )
    },
)