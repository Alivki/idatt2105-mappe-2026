package com.iksystem.alcohol.dashboard

import com.iksystem.common.security.AuthenticatedUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Alcohol Dashboard", description = "Aggregated dashboard statistics for IK-Alkohol")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/dashboard/alcohol")
class AlcoholDashboardController(
    private val dashboardService: AlcoholDashboardService,
) {

    @Operation(
        summary = "Get alcohol dashboard statistics",
        description = "Returns aggregated statistics for alcohol deviations, penalty points, age verification, and policy status."
    )
    @ApiResponse(responseCode = "200", description = "Dashboard statistics returned")
    @GetMapping
    fun getStats(@AuthenticationPrincipal auth: AuthenticatedUser): ResponseEntity<AlcoholDashboardStatsResponse> =
        ResponseEntity.ok(dashboardService.getStats(auth))
}
