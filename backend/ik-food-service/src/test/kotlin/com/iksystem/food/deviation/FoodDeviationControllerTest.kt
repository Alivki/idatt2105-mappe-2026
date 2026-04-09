package com.iksystem.food.deviation.controller

import com.iksystem.common.deviation.model.DeviationSeverity
import com.iksystem.common.security.AuthenticatedUser
import com.iksystem.food.deviation.dto.CreateFoodDeviationRequest
import com.iksystem.food.deviation.dto.FoodDeviationResponse
import com.iksystem.food.deviation.dto.UpdateFoodDeviationRequest
import com.iksystem.food.deviation.model.FoodDeviationStatus
import com.iksystem.food.deviation.model.FoodDeviationType
import com.iksystem.food.deviation.service.FoodDeviationService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class FoodDeviationControllerTest {

    private lateinit var service: FoodDeviationService
    private lateinit var controller: FoodDeviationController

    @BeforeEach
    fun setUp() {
        service = mock(FoodDeviationService::class.java)
        controller = FoodDeviationController(service)
    }

    @Test
    fun `list returns 200 with deviations`() {
        val auth = employeeAuth()
        val response = listOf(
            deviationResponse(
                id = 1L,
                deviationType = FoodDeviationType.TEMPERATUR,
                severity = DeviationSeverity.HIGH,
                description = "Fridge too warm"
            ),
            deviationResponse(
                id = 2L,
                deviationType = FoodDeviationType.RENHOLD,
                severity = DeviationSeverity.MEDIUM,
                description = "Cleaning routine missed"
            )
        )

        `when`(service.list(auth)).thenReturn(response)

        val result = controller.list(auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(2, result.body!!.size)
        assertEquals(1L, result.body!![0].id)
        assertEquals(FoodDeviationType.TEMPERATUR, result.body!![0].deviationType)
        assertEquals(2L, result.body!![1].id)
        assertEquals(FoodDeviationType.RENHOLD, result.body!![1].deviationType)

        verify(service).list(auth)
    }

    @Test
    fun `list returns 200 with empty list`() {
        val auth = employeeAuth()

        `when`(service.list(auth)).thenReturn(emptyList())

        val result = controller.list(auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(0, result.body!!.size)

        verify(service).list(auth)
    }

    @Test
    fun `getById returns 200 with deviation`() {
        val auth = employeeAuth()
        val response = deviationResponse(
            id = 10L,
            deviationType = FoodDeviationType.ALLERGEN,
            severity = DeviationSeverity.CRITICAL,
            description = "Allergen label missing"
        )

        `when`(service.getById(10L, auth)).thenReturn(response)

        val result = controller.getById(10L, auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(10L, result.body!!.id)
        assertEquals(FoodDeviationType.ALLERGEN, result.body!!.deviationType)
        assertEquals(DeviationSeverity.CRITICAL, result.body!!.severity)

        verify(service).getById(10L, auth)
    }

    @Test
    fun `create returns 201 with created deviation`() {
        val auth = employeeAuth()
        val request = CreateFoodDeviationRequest(
            reportedAt = "2026-04-08T10:00:00Z",
            deviationType = FoodDeviationType.TEMPERATUR,
            severity = DeviationSeverity.HIGH,
            description = "  Fridge too warm  ",
            immediateAction = "  Moved goods to backup fridge  ",
            immediateActionByUserId = 20L,
            immediateActionAt = "2026-04-08T10:05:00Z",
            cause = "  Door left open  ",
            preventiveMeasures = "  Staff reminder  ",
            preventiveResponsibleUserId = 30L,
            preventiveDeadline = "2026-04-10T12:00:00Z"
        )
        val response = deviationResponse(
            id = 11L,
            deviationType = FoodDeviationType.TEMPERATUR,
            severity = DeviationSeverity.HIGH,
            description = "Fridge too warm",
            immediateAction = "Moved goods to backup fridge",
            immediateActionByUserId = 20L,
            immediateActionByUserName = "Action User",
            cause = "Door left open",
            preventiveMeasures = "Staff reminder",
            preventiveResponsibleUserId = 30L,
            preventiveResponsibleUserName = "Responsible User",
            status = FoodDeviationStatus.OPEN
        )

        `when`(service.create(request, auth)).thenReturn(response)

        val result = controller.create(request, auth)

        assertEquals(201, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(11L, result.body!!.id)
        assertEquals(FoodDeviationType.TEMPERATUR, result.body!!.deviationType)
        assertEquals("Fridge too warm", result.body!!.description)
        assertEquals("Responsible User", result.body!!.preventiveResponsibleUserName)

        verify(service).create(request, auth)
    }

    @Test
    fun `update returns 200 with updated deviation`() {
        val auth = managerAuth()
        val request = UpdateFoodDeviationRequest(
            reportedAt = "2026-04-09T08:00:00Z",
            deviationType = FoodDeviationType.SKADEDYR,
            severity = DeviationSeverity.CRITICAL,
            description = "  Pest activity found  ",
            immediateAction = "  Area isolated  ",
            immediateActionByUserId = 21L,
            immediateActionAt = "2026-04-09T08:05:00Z",
            cause = "  Entry point near storage  ",
            preventiveMeasures = "  Pest control booked  ",
            preventiveResponsibleUserId = 31L,
            preventiveDeadline = "2026-04-12T10:00:00Z",
            status = FoodDeviationStatus.UNDER_TREATMENT
        )
        val response = deviationResponse(
            id = 12L,
            deviationType = FoodDeviationType.SKADEDYR,
            severity = DeviationSeverity.CRITICAL,
            description = "Pest activity found",
            immediateAction = "Area isolated",
            immediateActionByUserId = 21L,
            immediateActionByUserName = "Responder",
            cause = "Entry point near storage",
            preventiveMeasures = "Pest control booked",
            preventiveResponsibleUserId = 31L,
            preventiveResponsibleUserName = "Manager User",
            status = FoodDeviationStatus.UNDER_TREATMENT
        )

        `when`(service.update(12L, request, auth)).thenReturn(response)

        val result = controller.update(12L, request, auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(12L, result.body!!.id)
        assertEquals(FoodDeviationType.SKADEDYR, result.body!!.deviationType)
        assertEquals(FoodDeviationStatus.UNDER_TREATMENT, result.body!!.status)

        verify(service).update(12L, request, auth)
    }

    @Test
    fun `delete returns 204 with empty body`() {
        val auth = managerAuth()

        val result = controller.delete(15L, auth)

        assertEquals(204, result.statusCode.value())
        assertNull(result.body)

        verify(service).delete(15L, auth)
    }

    private fun employeeAuth() = AuthenticatedUser(
        10L,
        1L,
        "EMPLOYEE"
    )

    private fun managerAuth() = AuthenticatedUser(
        20L,
        1L,
        "MANAGER"
    )

    private fun deviationResponse(
        id: Long,
        organizationId: Long = 1L,
        reportedAt: String = "2026-04-08T10:00:00Z",
        reportedByUserId: Long = 10L,
        reportedByUserName: String = "Reporter User",
        deviationType: FoodDeviationType = FoodDeviationType.ANNET,
        severity: DeviationSeverity = DeviationSeverity.LOW,
        description: String = "Deviation description",
        immediateAction: String? = null,
        immediateActionByUserId: Long? = null,
        immediateActionByUserName: String? = null,
        immediateActionAt: String? = null,
        cause: String? = null,
        preventiveMeasures: String? = null,
        preventiveResponsibleUserId: Long? = null,
        preventiveResponsibleUserName: String? = null,
        preventiveDeadline: String? = null,
        status: FoodDeviationStatus = FoodDeviationStatus.OPEN,
        createdAt: String = "2026-04-08T10:00:00Z",
        updatedAt: String = "2026-04-08T10:00:00Z"
    ) = FoodDeviationResponse(
        id = id,
        organizationId = organizationId,
        reportedAt = reportedAt,
        reportedByUserId = reportedByUserId,
        reportedByUserName = reportedByUserName,
        deviationType = deviationType,
        severity = severity,
        description = description,
        immediateAction = immediateAction,
        immediateActionByUserId = immediateActionByUserId,
        immediateActionByUserName = immediateActionByUserName,
        immediateActionAt = immediateActionAt,
        cause = cause,
        preventiveMeasures = preventiveMeasures,
        preventiveResponsibleUserId = preventiveResponsibleUserId,
        preventiveResponsibleUserName = preventiveResponsibleUserName,
        preventiveDeadline = preventiveDeadline,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}