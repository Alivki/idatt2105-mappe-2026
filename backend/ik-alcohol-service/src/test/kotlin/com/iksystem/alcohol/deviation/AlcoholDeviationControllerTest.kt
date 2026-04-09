package com.iksystem.alcohol.deviation.controller

import com.iksystem.alcohol.deviation.dto.AlcoholDeviationResponse
import com.iksystem.alcohol.deviation.dto.CreateAlcoholDeviationRequest
import com.iksystem.alcohol.deviation.dto.UpdateAlcoholDeviationRequest
import com.iksystem.alcohol.deviation.model.AlcoholCausalAnalysis
import com.iksystem.alcohol.deviation.model.AlcoholDeviationStatus
import com.iksystem.alcohol.deviation.model.AlcoholDeviationType
import com.iksystem.alcohol.deviation.model.AlcoholReportSource
import com.iksystem.alcohol.deviation.service.AlcoholDeviationService
import com.iksystem.common.security.AuthenticatedUser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class AlcoholDeviationControllerTest {

    private lateinit var service: AlcoholDeviationService
    private lateinit var controller: AlcoholDeviationController

    @BeforeEach
    fun setUp() {
        service = mock(AlcoholDeviationService::class.java)
        controller = AlcoholDeviationController(service)
    }

    @Test
    fun `list returns 200 with deviations`() {
        val auth = authEmployee()
        val response = listOf(
            deviationResponse(id = 1L),
            deviationResponse(id = 2L, description = "Another deviation")
        )

        `when`(service.list(auth)).thenReturn(response)

        val result = controller.list(auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(2, result.body!!.size)
        assertEquals(1L, result.body!![0].id)
        assertEquals(2L, result.body!![1].id)

        verify(service).list(auth)
    }

    @Test
    fun `list returns 200 with empty list`() {
        val auth = authEmployee()

        `when`(service.list(auth)).thenReturn(emptyList())

        val result = controller.list(auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(0, result.body!!.size)

        verify(service).list(auth)
    }

    @Test
    fun `getById returns 200 with deviation`() {
        val auth = authEmployee()
        val response = deviationResponse(id = 10L)

        `when`(service.getById(10L, auth)).thenReturn(response)

        val result = controller.getById(10L, auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(10L, result.body!!.id)
        assertEquals("Deviation description", result.body!!.description)

        verify(service).getById(10L, auth)
    }

    @Test
    fun `create returns 201 with created deviation`() {
        val auth = authEmployee()
        val request = CreateAlcoholDeviationRequest(
            reportedAt = "2026-04-08T10:00:00Z",
            reportSource = AlcoholReportSource.EGENRAPPORT,
            deviationType = AlcoholDeviationType.FALSK_LEGITIMASJON,
            description = "  Customer used fake ID  ",
            immediateAction = "  Refused service  ",
            causalAnalysis = AlcoholCausalAnalysis.KOMMUNIKASJON,
            causalExplanation = "  Staff misunderstood the situation  ",
            preventiveMeasures = "  More training  ",
            preventiveDeadline = "2026-04-20T10:00:00Z",
            preventiveResponsibleUserId = 22L
        )
        val response = deviationResponse(
            id = 11L,
            description = "Customer used fake ID",
            immediateAction = "Refused service",
            preventiveResponsibleUserId = 22L,
            preventiveResponsibleUserName = "Responsible User"
        )

        `when`(service.create(request, auth)).thenReturn(response)

        val result = controller.create(request, auth)

        assertEquals(201, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(11L, result.body!!.id)
        assertEquals("Customer used fake ID", result.body!!.description)
        assertEquals("Responsible User", result.body!!.preventiveResponsibleUserName)

        verify(service).create(request, auth)
    }

    @Test
    fun `update returns 200 with updated deviation`() {
        val auth = authManager()
        val request = UpdateAlcoholDeviationRequest(
            reportedAt = "2026-04-08T12:00:00Z",
            reportSource = AlcoholReportSource.SJENKEKONTROLL,
            deviationType = AlcoholDeviationType.BRUDD_SJENKETIDER,
            description = "  Updated description  ",
            immediateAction = "  Updated action  ",
            causalAnalysis = AlcoholCausalAnalysis.RUTINE_IKKE_FULGT,
            causalExplanation = "  Updated explanation  ",
            preventiveMeasures = "  Updated measures  ",
            preventiveDeadline = "2026-05-01T12:00:00Z",
            preventiveResponsibleUserId = 33L,
            status = AlcoholDeviationStatus.UNDER_TREATMENT
        )
        val response = deviationResponse(
            id = 12L,
            reportSource = AlcoholReportSource.SJENKEKONTROLL,
            deviationType = AlcoholDeviationType.BRUDD_SJENKETIDER,
            description = "Updated description",
            status = AlcoholDeviationStatus.UNDER_TREATMENT,
            preventiveResponsibleUserId = 33L,
            preventiveResponsibleUserName = "Assigned Manager"
        )

        `when`(service.update(12L, request, auth)).thenReturn(response)

        val result = controller.update(12L, request, auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(12L, result.body!!.id)
        assertEquals(AlcoholDeviationStatus.UNDER_TREATMENT, result.body!!.status)
        assertEquals(AlcoholDeviationType.BRUDD_SJENKETIDER, result.body!!.deviationType)

        verify(service).update(12L, request, auth)
    }

    @Test
    fun `delete returns 204 with empty body`() {
        val auth = authManager()

        val result = controller.delete(14L, auth)

        assertEquals(204, result.statusCode.value())
        assertNull(result.body)

        verify(service).delete(14L, auth)
    }

    private fun authEmployee() = AuthenticatedUser(
        10L,
        1L,
        "EMPLOYEE"
    )

    private fun authManager() = AuthenticatedUser(
        20L,
        1L,
        "MANAGER"
    )

    private fun deviationResponse(
        id: Long,
        organizationId: Long = 1L,
        reportedAt: String = "2026-04-08T10:00:00Z",
        reportedByUserId: Long = 10L,
        reportedByUserName: String = "Test User",
        reportSource: AlcoholReportSource = AlcoholReportSource.EGENRAPPORT,
        deviationType: AlcoholDeviationType = AlcoholDeviationType.FALSK_LEGITIMASJON,
        description: String = "Deviation description",
        immediateAction: String? = "Immediate action",
        causalAnalysis: AlcoholCausalAnalysis? = AlcoholCausalAnalysis.KOMMUNIKASJON,
        causalExplanation: String? = "Causal explanation",
        preventiveMeasures: String? = "Preventive measures",
        preventiveDeadline: String? = "2026-04-20T10:00:00Z",
        preventiveResponsibleUserId: Long? = 22L,
        preventiveResponsibleUserName: String? = "Responsible User",
        status: AlcoholDeviationStatus = AlcoholDeviationStatus.OPEN,
        ageVerificationShiftId: Long? = null,
        createdAt: String = "2026-04-08T10:00:00Z",
        updatedAt: String = "2026-04-08T10:00:00Z"
    ) = AlcoholDeviationResponse(
        id = id,
        organizationId = organizationId,
        reportedAt = reportedAt,
        reportedByUserId = reportedByUserId,
        reportedByUserName = reportedByUserName,
        reportSource = reportSource,
        deviationType = deviationType,
        description = description,
        immediateAction = immediateAction,
        causalAnalysis = causalAnalysis,
        causalExplanation = causalExplanation,
        preventiveMeasures = preventiveMeasures,
        preventiveDeadline = preventiveDeadline,
        preventiveResponsibleUserId = preventiveResponsibleUserId,
        preventiveResponsibleUserName = preventiveResponsibleUserName,
        status = status,
        ageVerificationShiftId = ageVerificationShiftId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}