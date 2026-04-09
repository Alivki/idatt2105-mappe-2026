package com.iksystem.food.haccpsetup.controller

import com.iksystem.common.security.AuthenticatedUser
import com.iksystem.food.haccpsetup.dto.HaccpSetupRequest
import com.iksystem.food.haccpsetup.dto.HaccpSetupResponse
import com.iksystem.food.haccpsetup.service.HaccpSetupService
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
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for generating HACCP checklists.
 */
@Tag(name = "HACCP Setup", description = "Generate HACCP checklists based on business profile")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/haccp-setup")
class HaccpSetupController(
    private val haccpSetupService: HaccpSetupService,
) {

    /**
     * Generates HACCP checklists based on input data.
     */
    @Operation(
        summary = "Generate HACCP checklists",
        description = "Generates checklists based on the business profile answers. Replaces any previously generated wizard checklists.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Checklists generated"),
        ApiResponse(responseCode = "400", description = "Validation error"),
    )
    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun generate(
        @Valid @RequestBody request: HaccpSetupRequest,
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<HaccpSetupResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(haccpSetupService.generateChecklists(request, auth))
}