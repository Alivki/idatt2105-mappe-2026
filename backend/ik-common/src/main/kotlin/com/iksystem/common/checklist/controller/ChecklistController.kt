package com.iksystem.common.checklist.controller

import com.iksystem.common.checklist.dto.ChecklistItemResponse
import com.iksystem.common.checklist.dto.ChecklistResponse
import com.iksystem.common.checklist.dto.ChecklistStatsResponse
import com.iksystem.common.checklist.dto.CompletionHistoryEntry
import com.iksystem.common.checklist.dto.CreateChecklistItemRequest
import com.iksystem.common.checklist.dto.CreateChecklistRequest
import com.iksystem.common.checklist.dto.SetChecklistCompletionRequest
import com.iksystem.common.checklist.dto.UpdateChecklistItemRequest
import com.iksystem.common.checklist.dto.UpdateChecklistRequest
import com.iksystem.common.checklist.service.ChecklistService
import com.iksystem.common.security.AuthenticatedUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for managing checklists and checklist items.
 *
 * Provides endpoints for:
 * - Listing checklists and checklist statistics
 * - Retrieving completion history
 * - Creating, updating, and deleting checklists
 * - Marking checklist completion
 * - Creating, updating, and deleting checklist items
 *
 * All operations are scoped to the authenticated user's active organization.
 * Write operations are restricted where specified.
 *
 * @property checklistService Service layer handling checklist business logic
 */
@Tag(name = "Checklists", description = "Checklist templates and checklist items")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/checklists")
class ChecklistController(
    private val checklistService: ChecklistService,
) {

    /**
     * Retrieves all checklists for the authenticated user's active organization.
     *
     * @param auth The authenticated user
     * @return HTTP 200 with the list of checklists
     */
    @Operation(summary = "List checklists", description = "Returns all checklists for the active organization.")
    @ApiResponse(responseCode = "200", description = "Checklist list returned")
    @GetMapping
    fun list(@AuthenticationPrincipal auth: AuthenticatedUser): ResponseEntity<List<ChecklistResponse>> =
        ResponseEntity.ok(checklistService.list(auth))

    /**
     * Retrieves aggregated checklist statistics for the authenticated user's organization.
     *
     * Typically used for dashboard counters and summary views.
     *
     * @param auth The authenticated user
     * @return HTTP 200 with checklist statistics
     */
    @Operation(summary = "Checklist stats", description = "Returns summary stats for dashboard counters.")
    @ApiResponse(responseCode = "200", description = "Checklist stats returned")
    @GetMapping("/stats")
    fun stats(@AuthenticationPrincipal auth: AuthenticatedUser): ResponseEntity<ChecklistStatsResponse> =
        ResponseEntity.ok(checklistService.stats(auth))

    /**
     * Retrieves checklist completion events for a given number of past days.
     *
     * @param days Number of days to include in the history
     * @param auth The authenticated user
     * @return HTTP 200 with checklist completion history entries
     */
    @Operation(summary = "Completion history", description = "Returns checklist completion events for the given period.")
    @ApiResponse(responseCode = "200", description = "Completion history returned")
    @GetMapping("/completion-history")
    fun completionHistory(
        @RequestParam(defaultValue = "30") days: Int,
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<List<CompletionHistoryEntry>> =
        ResponseEntity.ok(checklistService.completionHistory(days, auth))

    /**
     * Creates a new checklist template.
     *
     * Access is restricted to users with ADMIN or MANAGER roles.
     *
     * @param request Request containing checklist details
     * @param auth The authenticated user
     * @return HTTP 201 with the created checklist
     */
    @Operation(summary = "Create checklist", description = "Creates a new checklist template.")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Checklist created"),
        ApiResponse(responseCode = "400", description = "Validation error"),
    )
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun createChecklist(
        @Valid @RequestBody request: CreateChecklistRequest,
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<ChecklistResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(checklistService.createChecklist(request, auth))

    /**
     * Updates checklist metadata and status.
     *
     * Access is restricted to users with ADMIN or MANAGER roles.
     *
     * @param checklistId The checklist ID
     * @param request Request containing updated checklist values
     * @param auth The authenticated user
     * @return HTTP 200 with the updated checklist
     */
    @Operation(summary = "Update checklist", description = "Updates checklist metadata and status.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Checklist updated"),
        ApiResponse(responseCode = "400", description = "No update fields or validation error"),
        ApiResponse(responseCode = "404", description = "Checklist not found"),
    )
    @PatchMapping("/{checklistId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun updateChecklist(
        @PathVariable checklistId: Long,
        @Valid @RequestBody request: UpdateChecklistRequest,
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<ChecklistResponse> =
        ResponseEntity.ok(checklistService.updateChecklist(checklistId, request, auth))

    /**
     * Deletes a checklist and all of its items.
     *
     * Access is restricted to users with ADMIN or MANAGER roles.
     *
     * @param checklistId The checklist ID
     * @param auth The authenticated user
     * @return HTTP 204 if the checklist was deleted
     */
    @Operation(summary = "Delete checklist", description = "Deletes a checklist and all of its items.")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Checklist deleted"),
        ApiResponse(responseCode = "404", description = "Checklist not found"),
    )
    @DeleteMapping("/{checklistId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun deleteChecklist(
        @PathVariable checklistId: Long,
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<Void> {
        checklistService.deleteChecklist(checklistId, auth)
        return ResponseEntity.noContent().build()
    }

    /**
     * Marks all items in a checklist as completed or not completed.
     *
     * @param checklistId The checklist ID
     * @param request Request containing the completion state
     * @param auth The authenticated user
     * @return HTTP 200 with the updated checklist
     */
    @Operation(summary = "Set checklist completion", description = "Marks all items in a checklist as completed or not completed.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Checklist completion updated"),
        ApiResponse(responseCode = "404", description = "Checklist not found"),
    )
    @PatchMapping("/{checklistId}/completion")
    fun setChecklistCompletion(
        @PathVariable checklistId: Long,
        @Valid @RequestBody request: SetChecklistCompletionRequest,
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<ChecklistResponse> =
        ResponseEntity.ok(checklistService.setChecklistCompletion(checklistId, request, auth))

    /**
     * Creates a new item in a checklist.
     *
     * Access is restricted to users with ADMIN or MANAGER roles.
     *
     * @param checklistId The checklist ID
     * @param request Request containing item details
     * @param auth The authenticated user
     * @return HTTP 201 with the created checklist item
     */
    @Operation(summary = "Create checklist item", description = "Adds an item to a checklist.")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Checklist item created"),
        ApiResponse(responseCode = "404", description = "Checklist not found"),
    )
    @PostMapping("/{checklistId}/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun createItem(
        @PathVariable checklistId: Long,
        @Valid @RequestBody request: CreateChecklistItemRequest,
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<ChecklistItemResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(checklistService.createItem(checklistId, request, auth))

    /**
     * Updates an existing checklist item.
     *
     * @param checklistId The checklist ID
     * @param itemId The checklist item ID
     * @param request Request containing updated item values
     * @param auth The authenticated user
     * @return HTTP 200 with the updated checklist item
     */
    @Operation(summary = "Update checklist item", description = "Updates a checklist item.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Checklist item updated"),
        ApiResponse(responseCode = "404", description = "Checklist or checklist item not found"),
    )
    @PatchMapping("/{checklistId}/items/{itemId}")
    fun updateItem(
        @PathVariable checklistId: Long,
        @PathVariable itemId: Long,
        @Valid @RequestBody request: UpdateChecklistItemRequest,
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<ChecklistItemResponse> =
        ResponseEntity.ok(checklistService.updateItem(checklistId, itemId, request, auth))

    /**
     * Deletes an item from a checklist.
     *
     * Access is restricted to users with ADMIN or MANAGER roles.
     *
     * @param checklistId The checklist ID
     * @param itemId The checklist item ID
     * @param auth The authenticated user
     * @return HTTP 204 if the item was deleted
     */
    @Operation(summary = "Delete checklist item", description = "Removes an item from a checklist.")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Checklist item deleted"),
        ApiResponse(responseCode = "404", description = "Checklist or checklist item not found"),
    )
    @DeleteMapping("/{checklistId}/items/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun deleteItem(
        @PathVariable checklistId: Long,
        @PathVariable itemId: Long,
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<Void> {
        checklistService.deleteItem(checklistId, itemId, auth)
        return ResponseEntity.noContent().build()
    }
}