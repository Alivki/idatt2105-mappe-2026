package com.iksystem.alcohol.penaltypoints.controller

import com.iksystem.alcohol.deviation.model.AlcoholDeviationType
import com.iksystem.alcohol.penaltypoints.dto.CreatePenaltyPointRequest
import com.iksystem.alcohol.penaltypoints.dto.PenaltyPointResponse
import com.iksystem.alcohol.penaltypoints.dto.PenaltyPointSummaryResponse
import com.iksystem.alcohol.penaltypoints.service.PenaltyPointService
import com.iksystem.common.security.AuthenticatedUser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class PenaltyPointControllerTest {

    private lateinit var service: PenaltyPointService
    private lateinit var controller: PenaltyPointController

    @BeforeEach
    fun setUp() {
        service = mock(PenaltyPointService::class.java)
        controller = PenaltyPointController(service)
    }

    @Test
    fun `getSummary returns 200 with summary payload`() {
        val auth = authEmployee()
        val response = PenaltyPointSummaryResponse(
            organizationId = 1L,
            totalPoints = 12,
            entries = listOf(
                penaltyPointResponse(
                    id = 1L,
                    points = 8,
                    violationType = AlcoholDeviationType.SKJENKING_MINDREAARIGE,
                    description = "Serious violation"
                ),
                penaltyPointResponse(
                    id = 2L,
                    points = 4,
                    violationType = AlcoholDeviationType.BRUDD_SJENKETIDER,
                    description = "Closing time breach"
                )
            )
        )

        `when`(service.getSummary(auth)).thenReturn(response)

        val result = controller.getSummary(auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(1L, result.body!!.organizationId)
        assertEquals(12, result.body!!.totalPoints)
        assertEquals(2, result.body!!.entries.size)
        assertEquals(1L, result.body!!.entries[0].id)
        assertEquals(2L, result.body!!.entries[1].id)

        verify(service).getSummary(auth)
    }

    @Test
    fun `getSummary returns 200 with empty entries`() {
        val auth = authManager()
        val response = PenaltyPointSummaryResponse(
            organizationId = 1L,
            totalPoints = 0,
            entries = emptyList()
        )

        `when`(service.getSummary(auth)).thenReturn(response)

        val result = controller.getSummary(auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(1L, result.body!!.organizationId)
        assertEquals(0, result.body!!.totalPoints)
        assertEquals(0, result.body!!.entries.size)

        verify(service).getSummary(auth)
    }

    @Test
    fun `add returns 201 with created penalty point`() {
        val auth = authManager()
        val request = CreatePenaltyPointRequest(
            violationType = AlcoholDeviationType.SKJENKING_APENBART_BERUSET,
            description = "  Manual registration after inspection  "
        )
        val response = penaltyPointResponse(
            id = 10L,
            points = 4,
            violationType = AlcoholDeviationType.SKJENKING_APENBART_BERUSET,
            description = "Manual registration after inspection"
        )

        `when`(service.add(request, auth)).thenReturn(response)

        val result = controller.add(request, auth)

        assertEquals(201, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(10L, result.body!!.id)
        assertEquals(1L, result.body!!.organizationId)
        assertEquals(4, result.body!!.points)
        assertEquals(AlcoholDeviationType.SKJENKING_APENBART_BERUSET, result.body!!.violationType)

        verify(service).add(request, auth)
    }

    @Test
    fun `add returns created penalty point with null description`() {
        val auth = authManager()
        val request = CreatePenaltyPointRequest(
            violationType = AlcoholDeviationType.REKLAMEBRUDD,
            description = null
        )
        val response = penaltyPointResponse(
            id = 11L,
            points = 1,
            violationType = AlcoholDeviationType.REKLAMEBRUDD,
            description = null
        )

        `when`(service.add(request, auth)).thenReturn(response)

        val result = controller.add(request, auth)

        assertEquals(201, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(11L, result.body!!.id)
        assertEquals(1, result.body!!.points)
        assertNull(result.body!!.description)

        verify(service).add(request, auth)
    }

    @Test
    fun `delete returns 204 with empty body`() {
        val auth = authAdmin()

        val result = controller.delete(15L, auth)

        assertEquals(204, result.statusCode.value())
        assertNull(result.body)

        verify(service).delete(15L, auth)
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

    private fun authAdmin() = AuthenticatedUser(
        30L,
        1L,
        "ADMIN"
    )

    private fun penaltyPointResponse(
        id: Long,
        organizationId: Long = 1L,
        alcoholDeviationId: Long? = null,
        points: Int,
        violationType: AlcoholDeviationType,
        description: String?,
        createdAt: String = "2026-04-08T10:00:00Z"
    ) = PenaltyPointResponse(
        id = id,
        organizationId = organizationId,
        alcoholDeviationId = alcoholDeviationId,
        points = points,
        violationType = violationType,
        description = description,
        createdAt = createdAt
    )
}