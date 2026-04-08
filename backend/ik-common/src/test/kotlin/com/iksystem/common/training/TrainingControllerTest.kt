package com.iksystem.common.training.controller

import com.iksystem.common.security.AuthenticatedUser
import com.iksystem.common.training.dto.CreateTrainingLogRequest
import com.iksystem.common.training.dto.TrainingLogResponse
import com.iksystem.common.training.dto.UpdateTrainingLogRequest
import com.iksystem.common.training.model.TrainingStatus
import com.iksystem.common.training.service.TrainingService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class TrainingControllerTest {

    private lateinit var trainingService: TrainingService
    private lateinit var controller: TrainingController

    @BeforeEach
    fun setUp() {
        trainingService = mock(TrainingService::class.java)
        controller = TrainingController(trainingService)
    }

    @Test
    fun `list returns 200 with training logs`() {
        val auth = managerAuth()
        val response = listOf(
            trainingLogResponse(id = 1L, title = "Alcohol law basics"),
            trainingLogResponse(id = 2L, title = "ID verification")
        )

        `when`(trainingService.list(auth)).thenReturn(response)

        val result = controller.list(auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(2, result.body!!.size)
        assertEquals(1L, result.body!![0].id)
        assertEquals("Alcohol law basics", result.body!![0].title)
        assertEquals(2L, result.body!![1].id)
        assertEquals("ID verification", result.body!![1].title)

        verify(trainingService).list(auth)
    }

    @Test
    fun `list returns 200 with empty list`() {
        val auth = employeeAuth()

        `when`(trainingService.list(auth)).thenReturn(emptyList())

        val result = controller.list(auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(0, result.body!!.size)

        verify(trainingService).list(auth)
    }

    @Test
    fun `getById returns 200 with training log`() {
        val auth = employeeAuth()
        val response = trainingLogResponse(
            id = 10L,
            title = "Food hygiene",
            status = TrainingStatus.COMPLETED
        )

        `when`(trainingService.getById(10L, auth)).thenReturn(response)

        val result = controller.getById(10L, auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(10L, result.body!!.id)
        assertEquals("Food hygiene", result.body!!.title)
        assertEquals(TrainingStatus.COMPLETED, result.body!!.status)

        verify(trainingService).getById(10L, auth)
    }

    @Test
    fun `create returns 201 with created training log`() {
        val auth = managerAuth()
        val request = CreateTrainingLogRequest(
            employeeUserId = 22L,
            title = "Responsible alcohol service",
            description = "Mandatory onboarding course",
            completedAt = "2026-04-08T10:00:00Z",
            expiresAt = "2027-04-08T10:00:00Z",
            status = TrainingStatus.COMPLETED
        )
        val response = trainingLogResponse(
            id = 11L,
            employeeUserId = 22L,
            title = "Responsible alcohol service",
            description = "Mandatory onboarding course",
            completedAt = "2026-04-08T10:00:00Z",
            expiresAt = "2027-04-08T10:00:00Z",
            status = TrainingStatus.COMPLETED
        )

        `when`(trainingService.create(request, auth)).thenReturn(response)

        val result = controller.create(request, auth)

        assertEquals(201, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(11L, result.body!!.id)
        assertEquals(22L, result.body!!.employeeUserId)
        assertEquals("Responsible alcohol service", result.body!!.title)
        assertEquals(TrainingStatus.COMPLETED, result.body!!.status)

        verify(trainingService).create(request, auth)
    }

    @Test
    fun `update returns 200 with updated training log`() {
        val auth = managerAuth()
        val request = UpdateTrainingLogRequest(
            employeeUserId = 33L,
            title = "Updated title",
            description = "Updated description",
            completedAt = "2026-05-01T12:00:00Z",
            expiresAt = "2027-05-01T12:00:00Z",
            status = TrainingStatus.EXPIRES_SOON
        )
        val response = trainingLogResponse(
            id = 12L,
            employeeUserId = 33L,
            title = "Updated title",
            description = "Updated description",
            completedAt = "2026-05-01T12:00:00Z",
            expiresAt = "2027-05-01T12:00:00Z",
            status = TrainingStatus.EXPIRES_SOON
        )

        `when`(trainingService.update(12L, request, auth)).thenReturn(response)

        val result = controller.update(12L, request, auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(12L, result.body!!.id)
        assertEquals(33L, result.body!!.employeeUserId)
        assertEquals("Updated title", result.body!!.title)
        assertEquals(TrainingStatus.EXPIRES_SOON, result.body!!.status)

        verify(trainingService).update(12L, request, auth)
    }

    @Test
    fun `delete returns 204 with empty body`() {
        val auth = managerAuth()

        val result = controller.delete(15L, auth)

        assertEquals(204, result.statusCode.value())
        assertNull(result.body)

        verify(trainingService).delete(15L, auth)
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

    private fun trainingLogResponse(
        id: Long,
        organizationId: Long = 1L,
        employeeUserId: Long = 22L,
        employeeUserName: String = "Employee User",
        loggedByUserId: Long = 10L,
        loggedByUserName: String = "Manager User",
        title: String = "Training title",
        description: String? = "Training description",
        completedAt: String? = null,
        expiresAt: String? = null,
        status: TrainingStatus = TrainingStatus.NOT_COMPLETED,
        createdAt: String = "2026-04-08T10:00:00Z",
        updatedAt: String = "2026-04-08T10:00:00Z"
    ) = TrainingLogResponse(
        id = id,
        organizationId = organizationId,
        employeeUserId = employeeUserId,
        employeeUserName = employeeUserName,
        loggedByUserId = loggedByUserId,
        loggedByUserName = loggedByUserName,
        title = title,
        description = description,
        completedAt = completedAt,
        expiresAt = expiresAt,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}