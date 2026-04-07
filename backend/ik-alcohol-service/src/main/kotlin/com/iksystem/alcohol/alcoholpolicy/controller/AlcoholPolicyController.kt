package com.iksystem.alcohol.alcoholpolicy.controller

import com.iksystem.alcohol.alcoholpolicy.dto.AlcoholPolicyResponse
import com.iksystem.alcohol.alcoholpolicy.dto.CreateAlcoholPolicyRequest
import com.iksystem.alcohol.alcoholpolicy.dto.UpdateAlcoholPolicyRequest
import com.iksystem.alcohol.alcoholpolicy.service.AlcoholPolicyService
import com.iksystem.common.security.AuthenticatedUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Alcohol Policy", description = "Management of organization alcohol serving policy")
@RestController
@RequestMapping("api/v1/alcohol-policy")
class AlcoholPolicyController(private val alcoholPolicyService: AlcoholPolicyService) {
    @Operation(summary = "Check if an alcohol policy exists for the organization")
    @GetMapping("/exists")
    fun exists(@AuthenticationPrincipal auth: AuthenticatedUser): ResponseEntity<Map<String, Boolean>> =
        ResponseEntity.ok(mapOf("exists" to alcoholPolicyService.existsForOrg(auth)))

    @Operation(summary = "Get the organization's alcohol policy")
    @GetMapping
    fun get(@AuthenticationPrincipal auth: AuthenticatedUser): ResponseEntity<AlcoholPolicyResponse> =
        ResponseEntity.ok(alcoholPolicyService.getByOrg(auth))

    @Operation(summary = "Create or Update the alcohol policy")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun upsert(
        @Valid @RequestBody request: CreateAlcoholPolicyRequest,
        @AuthenticationPrincipal auth: AuthenticatedUser
    ): ResponseEntity<AlcoholPolicyResponse> =
        ResponseEntity.ok(alcoholPolicyService.upsert(request, auth))

    @Operation(summary = "Partial update of the alcohol policy")
    @PatchMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun patch(
        @Valid @RequestBody request: UpdateAlcoholPolicyRequest,
        @AuthenticationPrincipal auth: AuthenticatedUser
    ): ResponseEntity<AlcoholPolicyResponse> =
        ResponseEntity.ok(alcoholPolicyService.upsert(request, auth))
}