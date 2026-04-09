package com.iksystem.common.checklist.dto

import com.iksystem.common.checklist.model.ChecklistFrequency
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

/**
 * Request DTO for creating a new checklist.
 *
 * @property name The name of the checklist
 * @property description Optional description of the checklist
 * @property frequency The frequency at which the checklist should be completed
 */
data class CreateChecklistRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 255, message = "Name cannot exceed 255 characters")
    val name: String,

    @field:Size(max = 5000, message = "Description cannot exceed 5000 characters")
    val description: String? = null,

    @field:NotNull(message = "Frequency is required")
    val frequency: ChecklistFrequency,
)

/**
 * Request DTO for updating an existing checklist.
 *
 * All fields are optional and will only update provided values.
 *
 * @property name Updated checklist name
 * @property description Updated checklist description
 * @property frequency Updated checklist frequency
 * @property active Whether the checklist is active
 */
data class UpdateChecklistRequest(
    @field:Size(max = 255, message = "Name cannot exceed 255 characters")
    val name: String? = null,

    @field:Size(max = 5000, message = "Description cannot exceed 5000 characters")
    val description: String? = null,

    val frequency: ChecklistFrequency? = null,
    val active: Boolean? = null,
)

/**
 * Request DTO for creating a checklist item.
 *
 * @property title The title of the item
 * @property description Optional description of the item
 * @property completed Initial completion status
 */
data class CreateChecklistItemRequest(
    @field:NotBlank(message = "Title is required")
    @field:Size(max = 255, message = "Title cannot exceed 255 characters")
    val title: String,

    @field:Size(max = 5000, message = "Description cannot exceed 5000 characters")
    val description: String? = null,

    val completed: Boolean = false,
)

/**
 * Request DTO for updating a checklist item.
 *
 * All fields are optional and will only update provided values.
 *
 * @property title Updated title
 * @property description Updated description
 * @property completed Updated completion status
 */
data class UpdateChecklistItemRequest(
    @field:Size(max = 255, message = "Title cannot exceed 255 characters")
    val title: String? = null,

    @field:Size(max = 5000, message = "Description cannot exceed 5000 characters")
    val description: String? = null,

    val completed: Boolean? = null,
)

/**
 * Request DTO for setting completion status for an entire checklist.
 *
 * @property completed Whether all items should be marked as completed
 */
data class SetChecklistCompletionRequest(
    @field:NotNull(message = "Completed is required")
    val completed: Boolean,
)

/**
 * Response DTO representing a checklist item.
 *
 * @property id The item ID
 * @property title The item title
 * @property description Optional description
 * @property completed Whether the item is completed
 * @property completedAt Timestamp when the item was completed
 */
data class ChecklistItemResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val completed: Boolean,
    val completedAt: java.time.Instant?,
)

/**
 * Represents the completion status of a checklist.
 */
enum class ChecklistStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
}

/**
 * Response DTO representing a checklist with its items and status.
 *
 * @property id The checklist ID
 * @property name The checklist name
 * @property description Optional description
 * @property frequency The checklist frequency
 * @property active Whether the checklist is active
 * @property source The origin of the checklist (e.g., template/system)
 * @property itemCount Total number of items
 * @property completedItemCount Number of completed items
 * @property status Current status of the checklist
 * @property items List of checklist items
 */
data class ChecklistResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val frequency: ChecklistFrequency,
    val active: Boolean,
    val source: String,
    val itemCount: Int,
    val completedItemCount: Int,
    val status: ChecklistStatus,
    val items: List<ChecklistItemResponse>,
)

/**
 * Response DTO for checklist statistics.
 *
 * @property activeChecklists Number of active checklists
 * @property totalChecklistItems Total number of checklist items
 */
data class ChecklistStatsResponse(
    val activeChecklists: Long,
    val totalChecklistItems: Long,
)

/**
 * Represents a checklist completion event.
 *
 * @property checklistId The checklist ID
 * @property completedAt Timestamp when the checklist was completed
 */
data class CompletionHistoryEntry(
    val checklistId: Long,
    val completedAt: java.time.Instant,
)