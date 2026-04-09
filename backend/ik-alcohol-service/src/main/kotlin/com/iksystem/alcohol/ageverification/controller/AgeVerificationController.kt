package com.iksystem.alcohol.ageverification.controller

import com.iksystem.alcohol.ageverification.dto.*
import com.iksystem.alcohol.ageverification.service.AgeVerificationService
import com.iksystem.common.security.AuthenticatedUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

/**
 * REST controller for managing age verification shifts and reporting.
 *
 * This controller provides endpoints for:
 * - Managing shifts (start, update, end, reopen)
 * - Tracking ID checks during shifts
 * - Registering deviations linked to shifts
 * - Retrieving shift details
 * - Fetching summaries, day-level insights, and statistics
 *
 * All endpoints operate in the context of an authenticated user via [AuthenticatedUser].
 * Some endpoints are restricted to users with ADMIN or MANAGER roles.
 *
 * @property service Service layer handling business logic for age verification
 */
@Tag(name = "Age Verification", description = "Age verification shift tracking and reporting")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/age-verification")
class AgeVerificationController(
    private val service: AgeVerificationService,
) {

    /**
     * Retrieves the currently active shift for the authenticated user.
     *
     * This is typically used by the client to determine whether the user
     * already has an ongoing shift.
     *
     * @param auth The authenticated user
     * @return HTTP 200 with the active shift, or `null` if no active shift exists
     */
    @GetMapping("/shifts/active")
    fun getActiveShift(
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<ShiftDetailResponse?> =
        ResponseEntity.ok(service.getActiveShift(auth))

    /**
     * Starts a new age verification shift for the authenticated user.
     *
     * A user can only have one active shift at a time.
     *
     * @param auth The authenticated user
     * @return HTTP 201 with the created shift
     * @throws Conflict if the user already has an active shift
     */
    @PostMapping("/shifts/start")
    fun startShift(
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<ShiftResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(service.startShift(auth))

    /**
     * Updates the number of ID checks performed in a shift.
     *
     * This endpoint allows incrementing or setting the ID check count
     * for a given shift.
     *
     * @param id The ID of the shift
     * @param request Request containing the updated ID check count
     * @param auth The authenticated user
     * @return HTTP 200 with the updated shift
     * @throws NotFound if the shift does not exist or is not accessible
     */
    @PatchMapping("/shifts/{id}/count")
    fun updateIdCheckCount(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateIdCheckCountRequest,
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<ShiftResponse> =
        ResponseEntity.ok(service.updateIdCheckCount(id, request, auth))

    /**
     * Creates a deviation associated with a specific shift.
     *
     * Deviations represent incidents or irregularities that occur during a shift.
     *
     * @param id The ID of the shift
     * @param request Request containing deviation details
     * @param auth The authenticated user
     * @return HTTP 201 with the created deviation
     * @throws NotFound if the shift does not exist or is not accessible
     */
    @PostMapping("/shifts/{id}/deviations")
    fun createShiftDeviation(
        @PathVariable id: Long,
        @Valid @RequestBody request: CreateShiftDeviationRequest,
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<ShiftDeviationResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(service.createShiftDeviation(id, request, auth))

    /**
     * Ends and signs off a shift.
     *
     * Once ended, the shift is considered completed and cannot be modified
     * unless explicitly reopened.
     *
     * @param id The ID of the shift
     * @param auth The authenticated user
     * @return HTTP 200 with the updated shift
     * @throws BadRequest if the shift is already ended
     */
    @PostMapping("/shifts/{id}/end")
    fun endShift(
        @PathVariable id: Long,
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<ShiftResponse> =
        ResponseEntity.ok(service.endShift(id, auth))

    /**
     * Reopens a previously completed shift from the same day.
     *
     * This allows corrections to be made after a shift has been ended.
     *
     * @param id The ID of the shift
     * @param auth The authenticated user
     * @return HTTP 200 with the reopened shift
     * @throws BadRequest if the shift cannot be reopened
     */
    @PostMapping("/shifts/{id}/reopen")
    fun reopenShift(
        @PathVariable id: Long,
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<ShiftResponse> =
        ResponseEntity.ok(service.reopenShift(id, auth))

    /**
     * Retrieves detailed information about a specific shift.
     *
     * Access is restricted to:
     * - The owner of the shift
     * - Users with sufficient privileges (e.g., managers)
     *
     * @param id The ID of the shift
     * @param auth The authenticated user
     * @return HTTP 200 with shift details
     */
    @GetMapping("/shifts/{id}")
    fun getShift(
        @PathVariable id: Long,
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<ShiftDetailResponse> =
        ResponseEntity.ok(service.getShiftById(id, auth))

    /**
     * Retrieves aggregated daily summaries within a date range.
     *
     * Only accessible to users with ADMIN or MANAGER roles.
     *
     * @param from Start date (inclusive)
     * @param to End date (inclusive)
     * @param auth The authenticated user
     * @return HTTP 200 with a list of daily summaries
     */
    @GetMapping("/daily-summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun getDailySummaries(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate,
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<List<DailySummaryResponse>> =
        ResponseEntity.ok(service.getDailySummaries(from, to, auth))

    /**
     * Retrieves detailed information for a specific day,
     * including all shifts performed that day.
     *
     * Accessible to all authenticated users within the organization,
     * so employees can view today's summary on the dashboard.
     *
     * @param date The date to retrieve details for
     * @param auth The authenticated user
     * @return HTTP 200 with detailed day data
     */
    @GetMapping("/daily-summary/{date}")
    fun getDayDetail(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<DayDetailResponse> =
        ResponseEntity.ok(service.getDayDetail(date, auth))

    /**
     * Retrieves statistics and trends for a given date range.
     *
     * This may include aggregated metrics such as total shifts,
     * ID checks, and deviation trends.
     *
     * Only accessible to users with ADMIN or MANAGER roles.
     *
     * @param from Start date (inclusive)
     * @param to End date (inclusive)
     * @param auth The authenticated user
     * @return HTTP 200 with statistics data
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun getStats(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate,
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<StatsResponse> =
        ResponseEntity.ok(service.getStats(from, to, auth))
}