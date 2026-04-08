package com.iksystem.food.dashboard

import com.iksystem.common.security.AuthenticatedUser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.math.BigDecimal

class FoodDashboardControllerTest {

    private lateinit var dashboardService: FoodDashboardService
    private lateinit var controller: FoodDashboardController

    @BeforeEach
    fun setUp() {
        dashboardService = mock(FoodDashboardService::class.java)
        controller = FoodDashboardController(dashboardService)
    }

    @Test
    fun `getStats returns 200 with dashboard payload`() {
        val auth = AuthenticatedUser(
            10L,
            1L,
            "MANAGER"
        )

        val response = FoodDashboardStatsResponse(
            deviations = DeviationStats(
                open = 2L,
                underTreatment = 1L,
                closed = 3L,
                critical = 1L,
                high = 2L,
                medium = 2L,
                low = 1L,
                byType = mapOf("TEMP" to 2L),
                trend = listOf(DailyCount("2026-04-08", 2L))
            ),
            temperature = TemperatureStats(
                totalMeasurements = 10L,
                deviationCount = 3L,
                okCount = 7L,
                appliances = listOf(
                    ApplianceStatus(
                        id = 1L,
                        name = "Cooler 1",
                        applianceType = "FRIDGE",
                        lastTemperature = BigDecimal("4.2"),
                        lastStatus = "OK",
                        minThreshold = BigDecimal("0.0"),
                        maxThreshold = BigDecimal("5.0")
                    )
                )
            ),
            checklists = ChecklistStats(
                totalActive = 4L,
                completedToday = 2L,
                totalItems = 12L,
                completedItems = 9L,
                byFrequency = mapOf("DAILY" to 2L, "WEEKLY" to 1L)
            ),
            training = TrainingStats(
                completed = 5L,
                expiresSoon = 2L,
                expired = 1L,
                notCompleted = 2L,
                total = 10L,
                compliancePercent = 50
            )
        )

        `when`(dashboardService.getStats(auth)).thenReturn(response)

        val result = controller.getStats(auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(2L, result.body!!.deviations.open)
        assertEquals(10L, result.body!!.temperature.totalMeasurements)
        assertEquals(4L, result.body!!.checklists.totalActive)
        assertEquals(50, result.body!!.training.compliancePercent)

        verify(dashboardService).getStats(auth)
    }
}