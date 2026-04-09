package com.iksystem.common.invite.controller

import com.iksystem.common.auth.dto.AuthResponse
import com.iksystem.common.invite.dto.AcceptInviteRequest
import com.iksystem.common.invite.dto.InviteDetailsResponse
import com.iksystem.common.invite.dto.InviteUserRequest
import com.iksystem.common.invite.service.InvitationService
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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for managing organization invitations.
 *
 * Provides endpoints for:
 * - Sending invitations to new users
 * - Retrieving invitation details (public)
 * - Accepting invitations and onboarding users
 *
 * Invitations are token-based and can be accessed without authentication
 * for onboarding purposes.
 */
@Tag(name = "Invitations", description = "Invite users to an organization")
@RestController
@RequestMapping("/api/v1/invitations")
class InvitationController(private val invitationService: InvitationService) {

    /**
     * Sends an invitation to a user via email.
     *
     * Only users with ADMIN or MANAGER roles are allowed to send invitations.
     *
     * @param request The invitation request containing user details
     * @param auth The authenticated user sending the invite
     * @return HTTP 201 if the invitation was successfully created
     */
    @Operation(summary = "Send invitation", description = "Sends an invite email to the given address. Requires ADMIN or MANAGER role.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Invitation sent"),
        ApiResponse(responseCode = "400", description = "Validation error"),
        ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        ApiResponse(responseCode = "409", description = "User already invited or already a member"),
    )
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun invite(
        @Valid @RequestBody request: InviteUserRequest,
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<Void> {
        invitationService.createInvite(request, auth)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    /**
     * Retrieves public details for a valid invitation token.
     *
     * This endpoint does not require authentication and is used
     * during the onboarding process.
     *
     * @param token The invitation token
     * @return Invitation details if valid
     */
    @Operation(summary = "Get invitation details", description = "Returns public details for a pending invite (no auth required).")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Invitation details returned"),
        ApiResponse(responseCode = "404", description = "Invalid or expired invitation token"),
    )
    @GetMapping("/{token}")
    fun getInviteDetails(@PathVariable token: String): ResponseEntity<InviteDetailsResponse> {
        return ResponseEntity.ok(invitationService.getValidInvite(token))
    }

    /**
     * Accepts an invitation and creates a new user account.
     *
     * Returns authentication tokens upon successful registration.
     * This endpoint does not require authentication.
     *
     * @param token The invitation token
     * @param request The request containing user credentials and details
     * @return Authentication response with tokens
     */
    @Operation(summary = "Accept invitation", description = "Accepts an invite and returns auth tokens (no auth required).")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Invitation accepted, auth tokens returned"),
        ApiResponse(responseCode = "400", description = "Validation error"),
        ApiResponse(responseCode = "404", description = "Invalid or expired invitation token"),
    )
    @PostMapping("/{token}/accept")
    fun accept(
        @PathVariable token: String,
        @Valid @RequestBody request: AcceptInviteRequest,
    ): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(
            invitationService.acceptInvitation(token, request.password, request.fullName, request.phoneNumber)
        )
    }
}