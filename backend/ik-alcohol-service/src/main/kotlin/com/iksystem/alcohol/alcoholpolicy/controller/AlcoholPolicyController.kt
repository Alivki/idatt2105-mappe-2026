package com.iksystem.alcohol.alcoholpolicy.controller

import com.iksystem.alcohol.alcoholpolicy.dto.AlcoholPolicyResponse
import com.iksystem.alcohol.alcoholpolicy.dto.CreateAlcoholPolicyRequest
import com.iksystem.alcohol.alcoholpolicy.dto.UpdateAlcoholPolicyRequest
import com.iksystem.alcohol.alcoholpolicy.service.AlcoholPolicyService
import com.iksystem.common.security.AuthenticatedUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

/**
 * REST controller for managing alcohol policies within an organization.
 *
 * Provides endpoints to:
 * - Check if a policy exists
 * - Retrieve the current policy
 * - Create or update a policy
 * - Partially update an existing policy
 *
 * All operations are scoped to the authenticated user's organization.
 * Write operations require ADMIN or MANAGER roles.
 *
 * @property alcoholPolicyService Service layer handling business logic
 */
@Tag(name = "Alcohol Policy", description = "Management of organization alcohol serving policy")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("api/v1/alcohol-policy")
class AlcoholPolicyController(
    private val alcoholPolicyService: AlcoholPolicyService
) {

    /**
     * Checks whether an alcohol policy exists for the authenticated user's organization.
     *
     * @param auth The authenticated user
     * @return HTTP 200 with a boolean flag indicating existence
     */
    @GetMapping("/exists")
    fun exists(
        @AuthenticationPrincipal auth: AuthenticatedUser
    ): ResponseEntity<Map<String, Boolean>> =
        ResponseEntity.ok(mapOf("exists" to alcoholPolicyService.existsForOrg(auth)))

    /**
     * Retrieves the alcohol policy for the authenticated user's organization.
     *
     * @param auth The authenticated user
     * @return HTTP 200 with the policy
     * @throws NotFoundException if no policy exists for the organization
     */
    @GetMapping
    fun get(
        @AuthenticationPrincipal auth: AuthenticatedUser
    ): ResponseEntity<AlcoholPolicyResponse> =
        ResponseEntity.ok(alcoholPolicyService.getByOrg(auth))

    /**
     * Creates a new alcohol policy or updates an existing one.
     *
     * This endpoint performs an "upsert" operation:
     * - Creates a policy if none exists
     * - Updates the existing policy otherwise
     *
     * Access restricted to ADMIN and MANAGER roles.
     *
     * @param request Request containing policy data
     * @param auth The authenticated user
     * @return HTTP 200 with the created or updated policy
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun upsert(
        @Valid @RequestBody request: CreateAlcoholPolicyRequest,
        @AuthenticationPrincipal auth: AuthenticatedUser
    ): ResponseEntity<AlcoholPolicyResponse> =
        ResponseEntity.ok(alcoholPolicyService.upsert(request, auth))

    /**
     * Partially updates the alcohol policy for the organization.
     *
     * Only the provided fields will be updated.
     *
     * Access restricted to ADMIN and MANAGER roles.
     *
     * @param request Request containing fields to update
     * @param auth The authenticated user
     * @return HTTP 200 with the updated policy
     * @throws NotFoundException if no policy exists
     */
    @PatchMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun patch(
        @Valid @RequestBody request: UpdateAlcoholPolicyRequest,
        @AuthenticationPrincipal auth: AuthenticatedUser
    ): ResponseEntity<AlcoholPolicyResponse> =
        ResponseEntity.ok(alcoholPolicyService.upsert(request, auth))
}