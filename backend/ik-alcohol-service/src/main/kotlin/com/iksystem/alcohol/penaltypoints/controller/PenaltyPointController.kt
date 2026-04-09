package com.iksystem.alcohol.penaltypoints.controller

import com.iksystem.alcohol.penaltypoints.dto.CreatePenaltyPointRequest
import com.iksystem.alcohol.penaltypoints.dto.PenaltyPointResponse
import com.iksystem.alcohol.penaltypoints.dto.PenaltyPointSummaryResponse
import com.iksystem.alcohol.penaltypoints.service.PenaltyPointService
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
import org.springframework.web.bind.annotation.*

/**
 * REST controller for managing penalty points related to alcohol deviations.
 *
 * Provides endpoints to:
 * - Retrieve penalty point summaries
 * - Manually add penalty points
 * - Delete penalty point entries
 *
 * All operations are scoped to the authenticated user's organization.
 * Write operations require ADMIN or MANAGER roles.
 *
 * @property service Service layer handling penalty point logic
 */
@Tag(name = "Penalty Points", description = "Alcohol deviation penalty point management")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/penalty-points")
class PenaltyPointController(
    private val service: PenaltyPointService,
) {

    /**
     * Retrieves the penalty point summary for the organization.
     *
     * Includes total points and all individual entries.
     *
     * @param auth The authenticated user
     * @return HTTP 200 with penalty point summary
     */
    @GetMapping
    fun getSummary(
        @AuthenticationPrincipal auth: AuthenticatedUser
    ): ResponseEntity<PenaltyPointSummaryResponse> =
        ResponseEntity.ok(service.getSummary(auth))

    /**
     * Adds a new penalty point entry manually.
     *
     * Typically used for administrative adjustments.
     *
     * Access restricted to ADMIN and MANAGER roles.
     *
     * @param request Request containing penalty point details
     * @param auth The authenticated user
     * @return HTTP 201 with the created penalty point entry
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun add(
        @Valid @RequestBody request: CreatePenaltyPointRequest,
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<PenaltyPointResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(service.add(request, auth))

    /**
     * Deletes a penalty point entry.
     *
     * Access restricted to ADMIN and MANAGER roles.
     *
     * @param id ID of the penalty point entry
     * @param auth The authenticated user
     * @return HTTP 204 if deletion is successful
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun delete(
        @PathVariable id: Long,
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<Void> {
        service.delete(id, auth)
        return ResponseEntity.noContent().build()
    }
}