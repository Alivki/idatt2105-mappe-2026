package com.iksystem.common.invite.controller

import com.iksystem.common.auth.dto.AuthResponse
import com.iksystem.common.invite.dto.AcceptInviteRequest
import com.iksystem.common.invite.dto.InviteDetailsResponse
import com.iksystem.common.invite.dto.InviteUserRequest
import com.iksystem.common.invite.service.InvitationService
import com.iksystem.common.security.AuthenticatedUser
import com.iksystem.common.user.dto.UserResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class InvitationControllerTest {

    private lateinit var invitationService: InvitationService
    private lateinit var controller: InvitationController

    @BeforeEach
    fun setUp() {
        invitationService = mock(InvitationService::class.java)
        controller = InvitationController(invitationService)
    }

    @Test
    fun `invite returns 201 and empty body`() {
        val auth = managerAuth()
        val request = InviteUserRequest(
            email = "invitee@example.com",
            role = "EMPLOYEE"
        )

        val result = controller.invite(request, auth)

        assertEquals(201, result.statusCode.value())
        assertNull(result.body)

        verify(invitationService).createInvite(request, auth)
    }

    @Test
    fun `getInviteDetails returns 200 with public details`() {
        val response = InviteDetailsResponse(
            email = "invitee@example.com",
            organizationName = "Test Org",
            existingUser = true
        )

        `when`(invitationService.getValidInvite("token-123")).thenReturn(response)

        val result = controller.getInviteDetails("token-123")

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals("invitee@example.com", result.body!!.email)
        assertEquals("Test Org", result.body!!.organizationName)
        assertTrue(result.body!!.existingUser)

        verify(invitationService).getValidInvite("token-123")
    }

    @Test
    fun `accept returns 200 with auth response`() {
        val request = AcceptInviteRequest(
            password = "secret123",
            fullName = "New User",
            phoneNumber = "+4712345678"
        )
        val response = AuthResponse(
            accessToken = "access-token",
            refreshToken = "refresh-token",
            expiresIn = 900L,
            user = userResponse(),
            organizationId = 1L,
            role = "EMPLOYEE"
        )

        `when`(
            invitationService.acceptInvitation(
                "token-abc",
                "secret123",
                "New User",
                "+4712345678"
            )
        ).thenReturn(response)

        val result = controller.accept("token-abc", request)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals("access-token", result.body!!.accessToken)
        assertEquals("refresh-token", result.body!!.refreshToken)
        assertEquals(1L, result.body!!.organizationId)
        assertEquals("EMPLOYEE", result.body!!.role)

        verify(invitationService).acceptInvitation(
            "token-abc",
            "secret123",
            "New User",
            "+4712345678"
        )
    }

    private fun managerAuth() = AuthenticatedUser(
        10L,
        1L,
        "MANAGER"
    )

    private fun userResponse() = UserResponse(
        id = 10L,
        email = "user@example.com",
        fullName = "Test User",
        phoneNumber = "+4712345678",
        active = true
    )
}