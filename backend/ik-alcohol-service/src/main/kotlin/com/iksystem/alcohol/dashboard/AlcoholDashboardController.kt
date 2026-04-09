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

/**
 * REST controller exposing dashboard statistics for the IK-Alkohol module.
 *
 * All endpoints are scoped to the authenticated user's organization and require
 * a valid Bearer token.
 *
 * Base path: `/api/v1/dashboard/alcohol`
 *
 * @property dashboardService Service used to compute and retrieve aggregated dashboard statistics.
 */
@Tag(name = "Alcohol Dashboard", description = "Aggregated dashboard statistics for IK-Alkohol")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/dashboard/alcohol")
class AlcoholDashboardController(
    private val dashboardService: AlcoholDashboardService,
) {

    /**
     * Returns aggregated dashboard statistics for the authenticated user's organization.
     *
     * Statistics include data on alcohol deviations, penalty points, age verification
     * checks, and the current alcohol policy status.
     *
     * @param auth The authenticated user, used to scope statistics to the correct organization.
     * @return A [ResponseEntity] containing the [AlcoholDashboardStatsResponse] with a `200 OK` status.
     */
    @Operation(
        summary = "Get alcohol dashboard statistics",
        description = "Returns aggregated statistics for alcohol deviations, penalty points, age verification, and policy status."
    )
    @ApiResponse(responseCode = "200", description = "Dashboard statistics returned")
    @GetMapping
    fun getStats(@AuthenticationPrincipal auth: AuthenticatedUser): ResponseEntity<AlcoholDashboardStatsResponse> =
        ResponseEntity.ok(dashboardService.getStats(auth))
}