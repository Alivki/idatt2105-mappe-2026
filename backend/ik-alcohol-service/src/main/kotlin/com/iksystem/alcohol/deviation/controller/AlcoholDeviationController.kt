package com.iksystem.alcohol.deviation.controller

import com.iksystem.alcohol.deviation.dto.AlcoholDeviationResponse
import com.iksystem.alcohol.deviation.dto.CreateAlcoholDeviationRequest
import com.iksystem.alcohol.deviation.dto.UpdateAlcoholDeviationRequest
import com.iksystem.alcohol.deviation.service.AlcoholDeviationService
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
 * REST controller for managing alcohol deviation registrations.
 *
 * Provides endpoints to list, retrieve, create, update, and delete alcohol deviations
 * scoped to the authenticated user's organization. Mutating operations (update, delete)
 * are restricted to users with the `ADMIN` or `MANAGER` role.
 *
 * Base path: `/api/v1/deviations/alcohol`
 *
 * @property service Service handling the business logic for alcohol deviations.
 */
@Tag(name = "Alcohol Deviations", description = "Deviation management for IK-Alkohol")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/deviations/alcohol")
class AlcoholDeviationController(
    private val service: AlcoholDeviationService,
) {

    /**
     * Returns all alcohol deviations for the authenticated user's organization.
     *
     * @param auth The authenticated user, used to scope results to the correct organization.
     * @return A `200 OK` response containing the list of [AlcoholDeviationResponse] items.
     */
    @Operation(summary = "List alcohol deviations")
    @ApiResponse(responseCode = "200", description = "Alcohol deviation list returned")
    @GetMapping
    fun list(@AuthenticationPrincipal auth: AuthenticatedUser): ResponseEntity<List<AlcoholDeviationResponse>> =
        ResponseEntity.ok(service.list(auth))

    /**
     * Returns a single alcohol deviation by its ID.
     *
     * @param id The ID of the deviation to retrieve.
     * @param auth The authenticated user, used to verify organization ownership.
     * @return A `200 OK` response with the [AlcoholDeviationResponse], or `404 Not Found`
     * if no deviation with the given ID exists for the organization.
     */
    @Operation(summary = "Get alcohol deviation by ID")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Alcohol deviation returned"),
        ApiResponse(responseCode = "404", description = "Not found"),
    )
    @GetMapping("/{id}")
    fun getById(
        @PathVariable id: Long,
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<AlcoholDeviationResponse> =
        ResponseEntity.ok(service.getById(id, auth))

    /**
     * Creates a new alcohol deviation for the authenticated user's organization.
     *
     * @param request The validated request body containing the deviation details.
     * @param auth The authenticated user, used to associate the deviation with the correct organization.
     * @return A `201 Created` response containing the created [AlcoholDeviationResponse],
     * or `400 Bad Request` if validation fails.
     */
    @Operation(summary = "Create alcohol deviation")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Alcohol deviation created"),
        ApiResponse(responseCode = "400", description = "Validation error"),
    )
    @PostMapping
    fun create(
        @Valid @RequestBody request: CreateAlcoholDeviationRequest,
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<AlcoholDeviationResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(service.create(request, auth))

    /**
     * Partially updates an existing alcohol deviation by its ID.
     *
     * Restricted to users with the `ADMIN` or `MANAGER` role.
     *
     * @param id The ID of the deviation to update.
     * @param request The validated request body containing the fields to update.
     * @param auth The authenticated user, used to verify organization ownership.
     * @return A `200 OK` response with the updated [AlcoholDeviationResponse], or `404 Not Found`
     * if no deviation with the given ID exists for the organization.
     */
    @Operation(summary = "Update alcohol deviation")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Alcohol deviation updated"),
        ApiResponse(responseCode = "404", description = "Not found"),
    )
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateAlcoholDeviationRequest,
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<AlcoholDeviationResponse> =
        ResponseEntity.ok(service.update(id, request, auth))

    /**
     * Deletes an alcohol deviation by its ID.
     *
     * Restricted to users with the `ADMIN` or `MANAGER` role.
     *
     * @param id The ID of the deviation to delete.
     * @param auth The authenticated user, used to verify organization ownership.
     * @return A `204 No Content` response on success, or `404 Not Found` if no deviation
     * with the given ID exists for the organization.
     */
    @Operation(summary = "Delete alcohol deviation")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Alcohol deviation deleted"),
        ApiResponse(responseCode = "404", description = "Not found"),
    )
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