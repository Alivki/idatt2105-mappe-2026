package com.iksystem.food.dashboard

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

/**
 * REST controller for retrieving food dashboard statistics.
 */
@Tag(name = "Food Dashboard", description = "Aggregated dashboard statistics for IK-Mat")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/dashboard/food")
class FoodDashboardController(
    private val dashboardService: FoodDashboardService,
) {

    /**
     * Returns aggregated dashboard statistics for the authenticated user's organization.
     */
    @Operation(
        summary = "Get food dashboard statistics",
        description = "Returns aggregated statistics for food deviations, temperature monitoring, checklists, and training compliance."
    )
    @ApiResponse(responseCode = "200", description = "Dashboard statistics returned")
    @GetMapping
    fun getStats(@AuthenticationPrincipal auth: AuthenticatedUser): ResponseEntity<FoodDashboardStatsResponse> =
        ResponseEntity.ok(dashboardService.getStats(auth))
}