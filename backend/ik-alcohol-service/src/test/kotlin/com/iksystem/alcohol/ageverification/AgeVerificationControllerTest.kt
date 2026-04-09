package com.iksystem.alcohol.ageverification.controller

import com.iksystem.alcohol.ageverification.dto.CreateShiftDeviationRequest
import com.iksystem.alcohol.ageverification.dto.DailySummaryResponse
import com.iksystem.alcohol.ageverification.dto.DayDetailResponse
import com.iksystem.alcohol.ageverification.dto.ShiftDetailResponse
import com.iksystem.alcohol.ageverification.dto.ShiftDeviationResponse
import com.iksystem.alcohol.ageverification.dto.ShiftResponse
import com.iksystem.alcohol.ageverification.dto.StatsResponse
import com.iksystem.alcohol.ageverification.dto.UpdateIdCheckCountRequest
import com.iksystem.alcohol.ageverification.model.ShiftStatus
import com.iksystem.alcohol.ageverification.service.AgeVerificationService
import com.iksystem.alcohol.deviation.model.AlcoholDeviationType
import com.iksystem.common.security.AuthenticatedUser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.LocalDate

class AgeVerificationControllerTest {

    private lateinit var service: AgeVerificationService
    private lateinit var controller: AgeVerificationController

    @BeforeEach
    fun setUp() {
        service = mock(AgeVerificationService::class.java)
        controller = AgeVerificationController(service)
    }

    @Test
    fun `getActiveShift returns 200 with payload`() {
        val auth = orgEmployee()
        val response = shiftDetailResponse(id = 1L)

        `when`(service.getActiveShift(auth)).thenReturn(response)

        val result = controller.getActiveShift(auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(1L, result.body!!.shift.id)
        assertEquals(ShiftStatus.ACTIVE, result.body!!.shift.status)

        verify(service).getActiveShift(auth)
    }

    @Test
    fun `startShift returns 201`() {
        val auth = orgEmployee()
        val response = shiftResponse(id = 2L)

        `when`(service.startShift(auth)).thenReturn(response)

        val result = controller.startShift(auth)

        assertEquals(201, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(2L, result.body!!.id)
        assertEquals(ShiftStatus.ACTIVE, result.body!!.status)

        verify(service).startShift(auth)
    }

    @Test
    fun `updateIdCheckCount returns 200 and body`() {
        val auth = orgEmployee()
        val request = UpdateIdCheckCountRequest(idsCheckedCount = 14)
        val response = shiftResponse(id = 3L, idsCheckedCount = 14)

        `when`(service.updateIdCheckCount(3L, request, auth)).thenReturn(response)

        val result = controller.updateIdCheckCount(3L, request, auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(3L, result.body!!.id)
        assertEquals(14, result.body!!.idsCheckedCount)

        verify(service).updateIdCheckCount(3L, request, auth)
    }

    @Test
    fun `createShiftDeviation returns 201`() {
        val auth = orgEmployee()
        val request = CreateShiftDeviationRequest(
            deviationType = AlcoholDeviationType.FALSK_LEGITIMASJON,
            description = "Customer used fake ID"
        )
        val response = ShiftDeviationResponse(
            id = 12L,
            deviationType = AlcoholDeviationType.FALSK_LEGITIMASJON,
            description = "Customer used fake ID",
            reportedAt = "2026-01-01T12:00:00Z"
        )

        `when`(service.createShiftDeviation(5L, request, auth)).thenReturn(response)

        val result = controller.createShiftDeviation(5L, request, auth)

        assertEquals(201, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(12L, result.body!!.id)
        assertEquals(AlcoholDeviationType.FALSK_LEGITIMASJON, result.body!!.deviationType)

        verify(service).createShiftDeviation(5L, request, auth)
    }

    @Test
    fun `endShift returns 200`() {
        val auth = orgEmployee()
        val response = shiftResponse(id = 8L, status = ShiftStatus.COMPLETED, signedOff = true)

        `when`(service.endShift(8L, auth)).thenReturn(response)

        val result = controller.endShift(8L, auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(ShiftStatus.COMPLETED, result.body!!.status)
        assertEquals(true, result.body!!.signedOff)

        verify(service).endShift(8L, auth)
    }

    @Test
    fun `reopenShift returns 200`() {
        val auth = orgEmployee()
        val response = shiftResponse(id = 8L, status = ShiftStatus.ACTIVE, signedOff = false)

        `when`(service.reopenShift(8L, auth)).thenReturn(response)

        val result = controller.reopenShift(8L, auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(ShiftStatus.ACTIVE, result.body!!.status)
        assertEquals(false, result.body!!.signedOff)

        verify(service).reopenShift(8L, auth)
    }

    @Test
    fun `getShift returns 200`() {
        val auth = orgEmployee()
        val response = shiftDetailResponse(id = 15L)

        `when`(service.getShiftById(15L, auth)).thenReturn(response)

        val result = controller.getShift(15L, auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(15L, result.body!!.shift.id)

        verify(service).getShiftById(15L, auth)
    }

    @Test
    fun `getDailySummaries returns 200`() {
        val auth = orgManager()
        val from = LocalDate.parse("2026-01-01")
        val to = LocalDate.parse("2026-01-07")
        val response = listOf(
            DailySummaryResponse(
                date = "2026-01-05",
                shiftCount = 2,
                totalIdsChecked = 10,
                totalDeviations = 1
            )
        )

        `when`(service.getDailySummaries(from, to, auth)).thenReturn(response)

        val result = controller.getDailySummaries(from, to, auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(1, result.body!!.size)
        assertEquals("2026-01-05", result.body!![0].date)
        assertEquals(2L, result.body!![0].shiftCount)

        verify(service).getDailySummaries(from, to, auth)
    }

    @Test
    fun `getDayDetail returns 200`() {
        val auth = orgManager()
        val date = LocalDate.parse("2026-01-05")
        val response = DayDetailResponse(
            date = "2026-01-05",
            shifts = listOf(shiftDetailResponse(15L)),
            totalIdsChecked = 12,
            totalDeviations = 1,
            deviationsByType = mapOf(AlcoholDeviationType.FALSK_LEGITIMASJON to 1)
        )

        `when`(service.getDayDetail(date, auth)).thenReturn(response)

        val result = controller.getDayDetail(date, auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals("2026-01-05", result.body!!.date)
        assertEquals(12, result.body!!.totalIdsChecked)
        assertEquals(1, result.body!!.totalDeviations)

        verify(service).getDayDetail(date, auth)
    }

    @Test
    fun `getStats returns 200`() {
        val auth = orgManager()
        val from = LocalDate.parse("2026-01-01")
        val to = LocalDate.parse("2026-01-07")
        val response = StatsResponse(
            periodFrom = "2026-01-01",
            periodTo = "2026-01-07",
            totalShifts = 3,
            totalIdsChecked = 15,
            totalDeviations = 2,
            avgIdsPerShift = 5.0,
            dailySummaries = listOf(
                DailySummaryResponse("2026-01-05", 2, 10, 1)
            )
        )

        `when`(service.getStats(from, to, auth)).thenReturn(response)

        val result = controller.getStats(from, to, auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals("2026-01-01", result.body!!.periodFrom)
        assertEquals("2026-01-07", result.body!!.periodTo)
        assertEquals(3L, result.body!!.totalShifts)
        assertEquals(15L, result.body!!.totalIdsChecked)
        assertEquals(2L, result.body!!.totalDeviations)
        assertEquals(5.0, result.body!!.avgIdsPerShift)

        verify(service).getStats(from, to, auth)
    }

    private fun orgEmployee() = AuthenticatedUser(
        10L,
        1L,
        "EMPLOYEE"
    )

    private fun orgManager() = AuthenticatedUser(
        20L,
        1L,
        "MANAGER"
    )

    private fun shiftResponse(
        id: Long,
        idsCheckedCount: Int = 0,
        signedOff: Boolean = false,
        status: ShiftStatus = ShiftStatus.ACTIVE
    ) = ShiftResponse(
        id = id,
        organizationId = 1L,
        userId = 10L,
        userName = "Test User",
        shiftDate = "2026-01-01",
        startedAt = "2026-01-01T10:00:00Z",
        endedAt = if (status == ShiftStatus.COMPLETED) "2026-01-01T11:00:00Z" else null,
        idsCheckedCount = idsCheckedCount,
        signedOff = signedOff,
        signedOffAt = if (signedOff) "2026-01-01T11:00:00Z" else null,
        status = status,
        deviationCount = 1,
        createdAt = "2026-01-01T10:00:00Z",
        updatedAt = "2026-01-01T10:30:00Z"
    )

    private fun shiftDetailResponse(id: Long) = ShiftDetailResponse(
        shift = shiftResponse(id = id),
        deviations = listOf(
            ShiftDeviationResponse(
                id = 1L,
                deviationType = AlcoholDeviationType.FALSK_LEGITIMASJON,
                description = "Deviation",
                reportedAt = "2026-01-01T10:10:00Z"
            )
        )
    )
}