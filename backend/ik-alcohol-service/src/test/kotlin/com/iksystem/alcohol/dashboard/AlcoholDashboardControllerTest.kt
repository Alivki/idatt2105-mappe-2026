package com.iksystem.alcohol.dashboard

import com.iksystem.common.security.AuthenticatedUser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class AlcoholDashboardControllerTest {

    private lateinit var dashboardService: AlcoholDashboardService
    private lateinit var controller: AlcoholDashboardController

    @BeforeEach
    fun setUp() {
        dashboardService = mock(AlcoholDashboardService::class.java)
        controller = AlcoholDashboardController(dashboardService)
    }

    @Test
    fun `getStats returns 200 with dashboard payload`() {
        val auth = AuthenticatedUser(
            10L,
            1L,
            "MANAGER"
        )

        val response = AlcoholDashboardStatsResponse(
            deviations = AlcoholDeviationStats(
                open = 2L,
                underTreatment = 1L,
                closed = 3L,
                byType = mapOf(
                    "FALSK_LEGITIMASJON" to 2L,
                    "NEKTET_VISE_LEGITIMASJON" to 1L
                ),
                trend = listOf(
                    AlcoholDailyCount("2026-04-01", 1L),
                    AlcoholDailyCount("2026-04-02", 2L)
                )
            ),
            penaltyPoints = PenaltyPointStats(
                totalPoints = 6,
                maxPoints = 12,
                entries = emptyList()
            ),
            ageVerification = AgeVerificationStats(
                totalShifts = 4L,
                totalIdsChecked = 20L,
                totalDeviations = 6L,
                avgIdsPerShift = 5.0
            ),
            policyStatus = PolicyStatus(
                exists = true,
                bevillingNumber = "BEV-77",
                bevillingValidUntil = "2026-05-01",
                isExpiringSoon = true
            )
        )

        `when`(dashboardService.getStats(auth)).thenReturn(response)

        val result = controller.getStats(auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(2L, result.body!!.deviations.open)
        assertEquals(6, result.body!!.penaltyPoints.totalPoints)
        assertEquals(4L, result.body!!.ageVerification.totalShifts)
        assertEquals(true, result.body!!.policyStatus.exists)
        assertEquals("BEV-77", result.body!!.policyStatus.bevillingNumber)

        verify(dashboardService).getStats(auth)
    }
}