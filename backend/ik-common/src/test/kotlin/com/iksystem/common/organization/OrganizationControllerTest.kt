package com.iksystem.common.organization.controller

import com.iksystem.common.organization.dto.CreateOrganizationRequest
import com.iksystem.common.organization.dto.OrganizationResponse
import com.iksystem.common.organization.service.OrganizationService
import com.iksystem.common.security.AuthenticatedUser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class OrganizationControllerTest {

    private lateinit var organizationService: OrganizationService
    private lateinit var controller: OrganizationController

    @BeforeEach
    fun setUp() {
        organizationService = mock(OrganizationService::class.java)
        controller = OrganizationController(organizationService)
    }

    @Test
    fun `create returns 201 with organization response`() {
        val auth = AuthenticatedUser(
            userId = 10L,
            organizationId = null,
            role = null
        )
        val request = CreateOrganizationRequest(
            name = "Test Org",
            orgNumber = "123456789"
        )
        val response = organizationResponse(
            id = 1L,
            name = "Test Org",
            orgNumber = "123456789"
        )

        `when`(organizationService.create(request, 10L)).thenReturn(response)

        val result = controller.create(request, auth)

        assertEquals(201, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(1L, result.body!!.id)
        assertEquals("Test Org", result.body!!.name)
        assertEquals("123456789", result.body!!.orgNumber)

        verify(organizationService).create(request, 10L)
    }

    @Test
    fun `getById returns 200 with organization response`() {
        val response = organizationResponse(
            id = 5L,
            name = "Existing Org",
            orgNumber = "999888777"
        )

        `when`(organizationService.getById(5L)).thenReturn(response)

        val result = controller.getById(5L)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(5L, result.body!!.id)
        assertEquals("Existing Org", result.body!!.name)
        assertEquals("999888777", result.body!!.orgNumber)

        verify(organizationService).getById(5L)
    }

    @Test
    fun `listAll returns 200 with organization list`() {
        val response = listOf(
            organizationResponse(id = 1L, name = "Org One", orgNumber = "111"),
            organizationResponse(id = 2L, name = "Org Two", orgNumber = null)
        )

        `when`(organizationService.listAll()).thenReturn(response)

        val result = controller.listAll()

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(2, result.body!!.size)
        assertEquals(1L, result.body!![0].id)
        assertEquals("Org One", result.body!![0].name)
        assertEquals("111", result.body!![0].orgNumber)
        assertEquals(2L, result.body!![1].id)
        assertEquals("Org Two", result.body!![1].name)
        assertNull(result.body!![1].orgNumber)

        verify(organizationService).listAll()
    }

    @Test
    fun `listAll returns 200 with empty list`() {
        `when`(organizationService.listAll()).thenReturn(emptyList())

        val result = controller.listAll()

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(0, result.body!!.size)

        verify(organizationService).listAll()
    }

    @Test
    fun `delete returns 204 with empty body`() {
        val result = controller.delete(8L)

        assertEquals(204, result.statusCode.value())
        assertNull(result.body)

        verify(organizationService).delete(8L)
    }

    private fun organizationResponse(
        id: Long,
        name: String,
        orgNumber: String?
    ) = OrganizationResponse(
        id = id,
        name = name,
        orgNumber = orgNumber
    )
}