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

@Tag(name = "Age Verification", description = "Age verification shift tracking and reporting")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/age-verification")
class AgeVerificationController(
    private val service: AgeVerificationService,
) {


    @Operation(summary = "Get current active shift for logged-in user")
    @ApiResponse(responseCode = "200", description = "Active shift or null")
    @GetMapping("/shifts/active")
    fun getActiveShift(
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<ShiftDetailResponse?> =
        ResponseEntity.ok(service.getActiveShift(auth))

    @Operation(summary = "Start a new age verification shift")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Shift started"),
        ApiResponse(responseCode = "409", description = "Already have an active shift"),
    )
    @PostMapping("/shifts/start")
    fun startShift(
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<ShiftResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(service.startShift(auth))

    @Operation(summary = "Update ID check count for active shift")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Count updated"),
        ApiResponse(responseCode = "404", description = "Shift not found"),
    )
    @PatchMapping("/shifts/{id}/count")
    fun updateIdCheckCount(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateIdCheckCountRequest,
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<ShiftResponse> =
        ResponseEntity.ok(service.updateIdCheckCount(id, request, auth))

    @Operation(summary = "Create a deviation linked to this shift")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Deviation created"),
        ApiResponse(responseCode = "404", description = "Shift not found"),
    )
    @PostMapping("/shifts/{id}/deviations")
    fun createShiftDeviation(
        @PathVariable id: Long,
        @Valid @RequestBody request: CreateShiftDeviationRequest,
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<ShiftDeviationResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(service.createShiftDeviation(id, request, auth))

    @Operation(summary = "End and sign off a shift")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Shift ended and signed off"),
        ApiResponse(responseCode = "400", description = "Shift already ended"),
    )
    @PostMapping("/shifts/{id}/end")
    fun endShift(
        @PathVariable id: Long,
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<ShiftResponse> =
        ResponseEntity.ok(service.endShift(id, auth))

    @Operation(summary = "Get shift by ID (own shift or manager access)")
    @ApiResponse(responseCode = "200", description = "Shift details")
    @GetMapping("/shifts/{id}")
    fun getShift(
        @PathVariable id: Long,
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<ShiftDetailResponse> =
        ResponseEntity.ok(service.getShiftById(id, auth))


    @Operation(summary = "Get daily summaries for a date range")
    @ApiResponse(responseCode = "200", description = "Daily summaries")
    @GetMapping("/daily-summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun getDailySummaries(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate,
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<List<DailySummaryResponse>> =
        ResponseEntity.ok(service.getDailySummaries(from, to, auth))

    @Operation(summary = "Get detailed view for a specific day")
    @ApiResponse(responseCode = "200", description = "Day detail with individual shifts")
    @GetMapping("/daily-summary/{date}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun getDayDetail(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<DayDetailResponse> =
        ResponseEntity.ok(service.getDayDetail(date, auth))

    @Operation(summary = "Get statistics and trends for a date range")
    @ApiResponse(responseCode = "200", description = "Statistics returned")
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun getStats(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate,
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<StatsResponse> =
        ResponseEntity.ok(service.getStats(from, to, auth))
}
