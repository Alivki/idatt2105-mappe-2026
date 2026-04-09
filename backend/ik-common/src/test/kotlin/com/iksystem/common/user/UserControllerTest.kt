package com.iksystem.common.user.controller

import com.iksystem.common.membership.dto.MemberNameResponse
import com.iksystem.common.membership.dto.MembershipResponse
import com.iksystem.common.security.AuthenticatedUser
import com.iksystem.common.user.dto.CreateUserRequest
import com.iksystem.common.user.dto.UpdateUserRoleRequest
import com.iksystem.common.user.dto.UserResponse
import com.iksystem.common.user.service.UserService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class UserControllerTest {

    private lateinit var userService: UserService
    private lateinit var controller: UserController

    @BeforeEach
    fun setUp() {
        userService = mock(UserService::class.java)
        controller = UserController(userService)
    }

    @Test
    fun `listUsers returns 200 with memberships`() {
        val auth = managerAuth()
        val response = listOf(
            membershipResponse(id = 1L, userId = 101L, userFullName = "Alice", role = "ADMIN"),
            membershipResponse(id = 2L, userId = 102L, userFullName = "Bob", role = "EMPLOYEE")
        )

        `when`(userService.listUsers(auth)).thenReturn(response)

        val result = controller.listUsers(auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(2, result.body!!.size)
        assertEquals(101L, result.body!![0].userId)
        assertEquals("Alice", result.body!![0].userFullName)
        assertEquals("EMPLOYEE", result.body!![1].role)

        verify(userService).listUsers(auth)
    }

    @Test
    fun `listMemberNames returns 200 with member names`() {
        val auth = employeeAuth()
        val response = listOf(
            MemberNameResponse(userId = 101L, fullName = "Alice"),
            MemberNameResponse(userId = 102L, fullName = "Bob")
        )

        `when`(userService.listMemberNames(auth)).thenReturn(response)

        val result = controller.listMemberNames(auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(2, result.body!!.size)
        assertEquals(101L, result.body!![0].userId)
        assertEquals("Bob", result.body!![1].fullName)

        verify(userService).listMemberNames(auth)
    }

    @Test
    fun `getUser returns 200 with membership`() {
        val auth = managerAuth()
        val response = membershipResponse(
            id = 5L,
            userId = 150L,
            userEmail = "member@example.com",
            userFullName = "Member User",
            role = "MANAGER"
        )

        `when`(userService.getUser(150L, auth)).thenReturn(response)

        val result = controller.getUser(150L, auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(150L, result.body!!.userId)
        assertEquals("member@example.com", result.body!!.userEmail)
        assertEquals("MANAGER", result.body!!.role)

        verify(userService).getUser(150L, auth)
    }

    @Test
    fun `getCurrentUser returns 200 with current user`() {
        val auth = employeeAuth()
        val response = userResponse(
            id = 20L,
            email = "me@example.com",
            fullName = "Current User",
            active = true
        )

        `when`(userService.getCurrentUser(auth)).thenReturn(response)

        val result = controller.getCurrentUser(auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(20L, result.body!!.id)
        assertEquals("me@example.com", result.body!!.email)
        assertEquals(true, result.body!!.active)

        verify(userService).getCurrentUser(auth)
    }

    @Test
    fun `createUser returns 201 with created membership`() {
        val auth = managerAuth()
        val request = CreateUserRequest(
            email = "newuser@example.com",
            password = "securepass123",
            fullName = "New User",
            phoneNumber = "+4712345678",
            role = "EMPLOYEE"
        )
        val response = membershipResponse(
            id = 9L,
            userId = 300L,
            userEmail = "newuser@example.com",
            userFullName = "New User",
            role = "EMPLOYEE"
        )

        `when`(userService.createUser(request, auth)).thenReturn(response)

        val result = controller.createUser(request, auth)

        assertEquals(201, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(300L, result.body!!.userId)
        assertEquals("newuser@example.com", result.body!!.userEmail)
        assertEquals("EMPLOYEE", result.body!!.role)

        verify(userService).createUser(request, auth)
    }

    @Test
    fun `updateUserRole returns 200 with updated membership`() {
        val auth = managerAuth()
        val request = UpdateUserRoleRequest(role = "ADMIN")
        val response = membershipResponse(
            id = 10L,
            userId = 301L,
            userFullName = "Promoted User",
            role = "ADMIN"
        )

        `when`(userService.updateUserRole(301L, request, auth)).thenReturn(response)

        val result = controller.updateUserRole(301L, request, auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(301L, result.body!!.userId)
        assertEquals("ADMIN", result.body!!.role)

        verify(userService).updateUserRole(301L, request, auth)
    }

    @Test
    fun `deactivateUser returns 200 with inactive user`() {
        val auth = managerAuth()
        val response = userResponse(
            id = 400L,
            email = "inactive@example.com",
            fullName = "Inactive User",
            active = false
        )

        `when`(userService.deactivateUser(400L, auth)).thenReturn(response)

        val result = controller.deactivateUser(400L, auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(400L, result.body!!.id)
        assertEquals(false, result.body!!.active)

        verify(userService).deactivateUser(400L, auth)
    }

    @Test
    fun `activateUser returns 200 with active user`() {
        val auth = managerAuth()
        val response = userResponse(
            id = 401L,
            email = "active@example.com",
            fullName = "Active User",
            active = true
        )

        `when`(userService.activateUser(401L, auth)).thenReturn(response)

        val result = controller.activateUser(401L, auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(401L, result.body!!.id)
        assertEquals(true, result.body!!.active)

        verify(userService).activateUser(401L, auth)
    }

    @Test
    fun `kickUser returns 204 with empty body`() {
        val auth = managerAuth()

        val result = controller.kickUser(500L, auth)

        assertEquals(204, result.statusCode.value())
        assertNull(result.body)

        verify(userService).kickUser(500L, auth)
    }

    @Test
    fun `removeMember returns 204 with empty body`() {
        val auth = adminAuth()

        val result = controller.removeMember(600L, auth)

        assertEquals(204, result.statusCode.value())
        assertNull(result.body)

        verify(userService).removeMember(600L, auth)
    }

    private fun adminAuth() = AuthenticatedUser(
        1L,
        1L,
        "ADMIN"
    )

    private fun managerAuth() = AuthenticatedUser(
        10L,
        1L,
        "MANAGER"
    )

    private fun employeeAuth() = AuthenticatedUser(
        20L,
        1L,
        "EMPLOYEE"
    )

    private fun userResponse(
        id: Long,
        email: String,
        fullName: String,
        phoneNumber: String = "+4712345678",
        active: Boolean
    ) = UserResponse(
        id = id,
        email = email,
        fullName = fullName,
        phoneNumber = phoneNumber,
        active = active,
        emailNotifications = true
    )

    private fun membershipResponse(
        id: Long,
        userId: Long,
        userEmail: String = "user@example.com",
        userFullName: String,
        organizationId: Long = 1L,
        role: String
    ) = MembershipResponse(
        id = id,
        userId = userId,
        userEmail = userEmail,
        userFullName = userFullName,
        organizationId = organizationId,
        role = role
    )
}