package com.iksystem.common.auth.controller

import com.iksystem.common.auth.dto.AuthResponse
import com.iksystem.common.auth.dto.LoginRequest
import com.iksystem.common.auth.dto.LoginResponse
import com.iksystem.common.auth.dto.RefreshRequest
import com.iksystem.common.auth.dto.RegisterRequest
import com.iksystem.common.auth.dto.SelectOrgRequest
import com.iksystem.common.auth.service.AuthService
import com.iksystem.common.membership.dto.MembershipSummary
import com.iksystem.common.security.AuthenticatedUser
import com.iksystem.common.user.dto.UserResponse
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class AuthControllerTest {

    private lateinit var authService: AuthService
    private lateinit var controller: AuthController

    @BeforeEach
    fun setUp() {
        authService = mock(AuthService::class.java)
        controller = AuthController(authService)
    }

    @Test
    fun `register returns 201 with login response`() {
        val request = RegisterRequest(
            email = "newuser@example.com",
            password = "securepass123",
            fullName = "New User",
            phoneNumber = "+4712345678"
        )
        val response = LoginResponse(
            preAuthToken = "pre-auth-token",
            user = userResponse(email = "newuser@example.com"),
            memberships = emptyList()
        )

        `when`(authService.register(request)).thenReturn(response)

        val result = controller.register(request)

        assertEquals(201, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals("pre-auth-token", result.body!!.preAuthToken)
        assertEquals("newuser@example.com", result.body!!.user.email)
        assertEquals(0, result.body!!.memberships.size)

        verify(authService).register(request)
    }

    @Test
    fun `login returns 200 with memberships`() {
        val request = LoginRequest(
            email = "user@example.com",
            password = "password"
        )
        val response = loginResponse(
            preAuthToken = "pre-auth-login",
            memberships = listOf(
                membershipSummary(1L, 100L, "Org One", "ADMIN"),
                membershipSummary(2L, 200L, "Org Two", "MANAGER")
            )
        )

        `when`(authService.login(request)).thenReturn(response)

        val result = controller.login(request)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals("pre-auth-login", result.body!!.preAuthToken)
        assertEquals(2, result.body!!.memberships.size)
        assertEquals("Org One", result.body!!.memberships[0].organizationName)
        assertEquals("MANAGER", result.body!!.memberships[1].role)

        verify(authService).login(request)
    }

    @Test
    fun `selectOrg returns 200 with auth response and forwards request metadata`() {
        val request = SelectOrgRequest(organizationId = 200L)
        val auth = AuthenticatedUser(
            userId = 10L,
            organizationId = null,
            role = null
        )
        val httpRequest = mock(HttpServletRequest::class.java)
        val response = authResponse(
            accessToken = "access-token",
            refreshToken = "refresh-token",
            organizationId = 200L,
            role = "MANAGER"
        )

        `when`(httpRequest.remoteAddr).thenReturn("127.0.0.1")
        `when`(httpRequest.getHeader("User-Agent")).thenReturn("JUnit")
        `when`(authService.selectOrg(10L, request, "127.0.0.1", "JUnit")).thenReturn(response)

        val result = controller.selectOrg(request, auth, httpRequest)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals("access-token", result.body!!.accessToken)
        assertEquals("refresh-token", result.body!!.refreshToken)
        assertEquals(200L, result.body!!.organizationId)
        assertEquals("MANAGER", result.body!!.role)

        verify(authService).selectOrg(10L, request, "127.0.0.1", "JUnit")
    }

    @Test
    fun `refresh returns 200 with rotated tokens`() {
        val request = RefreshRequest(refreshToken = "old-refresh-token")
        val response = authResponse(
            accessToken = "new-access-token",
            refreshToken = "new-refresh-token",
            organizationId = 100L,
            role = "ADMIN"
        )

        `when`(authService.refresh(request)).thenReturn(response)

        val result = controller.refresh(request)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals("new-access-token", result.body!!.accessToken)
        assertEquals("new-refresh-token", result.body!!.refreshToken)
        assertEquals(100L, result.body!!.organizationId)

        verify(authService).refresh(request)
    }

    @Test
    fun `listMemberships returns 200 with memberships`() {
        val auth = AuthenticatedUser(
            userId = 10L,
            organizationId = 100L,
            role = "ADMIN"
        )
        val memberships = listOf(
            membershipSummary(1L, 100L, "Org One", "ADMIN"),
            membershipSummary(2L, 200L, "Org Two", "MANAGER")
        )

        `when`(authService.listMemberships(10L)).thenReturn(memberships)

        val result = controller.listMemberships(auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(2, result.body!!.size)
        assertEquals(100L, result.body!![0].organizationId)
        assertEquals("Org Two", result.body!![1].organizationName)

        verify(authService).listMemberships(10L)
    }

    @Test
    fun `switchOrg returns 200 with new auth response and forwards current org and metadata`() {
        val request = SelectOrgRequest(organizationId = 300L)
        val auth = AuthenticatedUser(
            userId = 10L,
            organizationId = 100L,
            role = "ADMIN"
        )
        val httpRequest = mock(HttpServletRequest::class.java)
        val response = authResponse(
            accessToken = "switched-access-token",
            refreshToken = "switched-refresh-token",
            organizationId = 300L,
            role = "MANAGER"
        )

        `when`(httpRequest.remoteAddr).thenReturn("10.0.0.5")
        `when`(httpRequest.getHeader("User-Agent")).thenReturn("Postman")
        `when`(authService.switchOrg(10L, 100L, request, "10.0.0.5", "Postman")).thenReturn(response)

        val result = controller.switchOrg(request, auth, httpRequest)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals("switched-access-token", result.body!!.accessToken)
        assertEquals("switched-refresh-token", result.body!!.refreshToken)
        assertEquals(300L, result.body!!.organizationId)
        assertEquals("MANAGER", result.body!!.role)

        verify(authService).switchOrg(10L, 100L, request, "10.0.0.5", "Postman")
    }

    @Test
    fun `logout returns 204 with empty body`() {
        val auth = AuthenticatedUser(
            userId = 10L,
            organizationId = 100L,
            role = "ADMIN"
        )

        val result = controller.logout(auth)

        assertEquals(204, result.statusCode.value())
        assertNull(result.body)

        verify(authService).logout(10L)
    }

    private fun loginResponse(
        preAuthToken: String,
        memberships: List<MembershipSummary>
    ) = LoginResponse(
        preAuthToken = preAuthToken,
        user = userResponse(),
        memberships = memberships
    )

    private fun authResponse(
        accessToken: String,
        refreshToken: String,
        organizationId: Long,
        role: String
    ) = AuthResponse(
        accessToken = accessToken,
        refreshToken = refreshToken,
        expiresIn = 900,
        user = userResponse(),
        organizationId = organizationId,
        role = role
    )

    private fun membershipSummary(
        membershipId: Long,
        organizationId: Long,
        organizationName: String,
        role: String
    ) = MembershipSummary(
        membershipId = membershipId,
        organizationId = organizationId,
        organizationName = organizationName,
        role = role
    )

    private fun userResponse(
        email: String = "user@example.com"
    ) = UserResponse(
        id = 10L,
        email = email,
        fullName = "Test User",
        phoneNumber = "+4712345678",
        active = true
    )
}