package com.iksystem.common.checklist.controller

import com.iksystem.common.checklist.dto.ChecklistItemResponse
import com.iksystem.common.checklist.dto.ChecklistResponse
import com.iksystem.common.checklist.dto.ChecklistStatsResponse
import com.iksystem.common.checklist.dto.ChecklistStatus
import com.iksystem.common.checklist.dto.CreateChecklistItemRequest
import com.iksystem.common.checklist.dto.CreateChecklistRequest
import com.iksystem.common.checklist.dto.SetChecklistCompletionRequest
import com.iksystem.common.checklist.dto.UpdateChecklistItemRequest
import com.iksystem.common.checklist.dto.UpdateChecklistRequest
import com.iksystem.common.checklist.model.ChecklistFrequency
import com.iksystem.common.checklist.service.ChecklistService
import com.iksystem.common.security.AuthenticatedUser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.Instant

class ChecklistControllerTest {

    private lateinit var checklistService: ChecklistService
    private lateinit var controller: ChecklistController

    @BeforeEach
    fun setUp() {
        checklistService = mock(ChecklistService::class.java)
        controller = ChecklistController(checklistService)
    }

    @Test
    fun `list returns 200 with checklist list`() {
        val auth = managerAuth()
        val response = listOf(
            checklistResponse(id = 1L, name = "Opening"),
            checklistResponse(id = 2L, name = "Closing")
        )

        `when`(checklistService.list(auth)).thenReturn(response)

        val result = controller.list(auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(2, result.body!!.size)
        assertEquals("Opening", result.body!![0].name)
        assertEquals("Closing", result.body!![1].name)

        verify(checklistService).list(auth)
    }

    @Test
    fun `stats returns 200 with checklist stats`() {
        val auth = employeeAuth()
        val response = ChecklistStatsResponse(
            activeChecklists = 3L,
            totalChecklistItems = 12L
        )

        `when`(checklistService.stats(auth)).thenReturn(response)

        val result = controller.stats(auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(3L, result.body!!.activeChecklists)
        assertEquals(12L, result.body!!.totalChecklistItems)

        verify(checklistService).stats(auth)
    }

    @Test
    fun `createChecklist returns 201`() {
        val auth = managerAuth()
        val request = CreateChecklistRequest(
            name = "Opening checklist",
            description = "Daily open",
            frequency = ChecklistFrequency.DAILY
        )
        val response = checklistResponse(id = 10L, name = "Opening checklist")

        `when`(checklistService.createChecklist(request, auth)).thenReturn(response)

        val result = controller.createChecklist(request, auth)

        assertEquals(201, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(10L, result.body!!.id)
        assertEquals("Opening checklist", result.body!!.name)

        verify(checklistService).createChecklist(request, auth)
    }

    @Test
    fun `updateChecklist returns 200`() {
        val auth = managerAuth()
        val request = UpdateChecklistRequest(
            name = "Updated checklist",
            description = "Updated desc",
            frequency = ChecklistFrequency.WEEKLY,
            active = false
        )
        val response = checklistResponse(
            id = 11L,
            name = "Updated checklist",
            frequency = ChecklistFrequency.WEEKLY,
            active = false
        )

        `when`(checklistService.updateChecklist(11L, request, auth)).thenReturn(response)

        val result = controller.updateChecklist(11L, request, auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals("Updated checklist", result.body!!.name)
        assertEquals(ChecklistFrequency.WEEKLY, result.body!!.frequency)
        assertEquals(false, result.body!!.active)

        verify(checklistService).updateChecklist(11L, request, auth)
    }

    @Test
    fun `deleteChecklist returns 204`() {
        val auth = managerAuth()

        val result = controller.deleteChecklist(12L, auth)

        assertEquals(204, result.statusCode.value())
        assertNull(result.body)

        verify(checklistService).deleteChecklist(12L, auth)
    }

    @Test
    fun `setChecklistCompletion returns 200`() {
        val auth = employeeAuth()
        val request = SetChecklistCompletionRequest(completed = true)
        val response = checklistResponse(
            id = 13L,
            name = "Close checklist",
            status = ChecklistStatus.COMPLETED,
            itemCount = 2,
            completedItemCount = 2
        )

        `when`(checklistService.setChecklistCompletion(13L, request, auth)).thenReturn(response)

        val result = controller.setChecklistCompletion(13L, request, auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(ChecklistStatus.COMPLETED, result.body!!.status)
        assertEquals(2, result.body!!.completedItemCount)

        verify(checklistService).setChecklistCompletion(13L, request, auth)
    }

    @Test
    fun `createItem returns 201`() {
        val auth = managerAuth()
        val request = CreateChecklistItemRequest(
            title = "Check doors",
            description = "Make sure all are locked",
            completed = false
        )
        val response = checklistItemResponse(id = 21L, title = "Check doors")

        `when`(checklistService.createItem(14L, request, auth)).thenReturn(response)

        val result = controller.createItem(14L, request, auth)

        assertEquals(201, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(21L, result.body!!.id)
        assertEquals("Check doors", result.body!!.title)

        verify(checklistService).createItem(14L, request, auth)
    }

    @Test
    fun `updateItem returns 200`() {
        val auth = employeeAuth()
        val request = UpdateChecklistItemRequest(
            title = null,
            description = null,
            completed = true
        )
        val response = checklistItemResponse(
            id = 22L,
            title = "Check lights",
            completed = true,
            completedAt = Instant.parse("2026-01-01T10:00:00Z")
        )

        `when`(checklistService.updateItem(15L, 22L, request, auth)).thenReturn(response)

        val result = controller.updateItem(15L, 22L, request, auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(22L, result.body!!.id)
        assertEquals(true, result.body!!.completed)

        verify(checklistService).updateItem(15L, 22L, request, auth)
    }

    @Test
    fun `deleteItem returns 204`() {
        val auth = managerAuth()

        val result = controller.deleteItem(16L, 23L, auth)

        assertEquals(204, result.statusCode.value())
        assertNull(result.body)

        verify(checklistService).deleteItem(16L, 23L, auth)
    }

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

    private fun checklistResponse(
        id: Long,
        name: String,
        description: String? = "Description",
        frequency: ChecklistFrequency = ChecklistFrequency.DAILY,
        active: Boolean = true,
        source: String = "MANUAL",
        itemCount: Int = 0,
        completedItemCount: Int = 0,
        status: ChecklistStatus = ChecklistStatus.NOT_STARTED,
        items: List<ChecklistItemResponse> = emptyList()
    ) = ChecklistResponse(
        id = id,
        name = name,
        description = description,
        frequency = frequency,
        active = active,
        source = source,
        itemCount = itemCount,
        completedItemCount = completedItemCount,
        status = status,
        items = items
    )

    private fun checklistItemResponse(
        id: Long,
        title: String,
        description: String? = "Item description",
        completed: Boolean = false,
        completedAt: Instant? = null
    ) = ChecklistItemResponse(
        id = id,
        title = title,
        description = description,
        completed = completed,
        completedAt = completedAt
    )
}