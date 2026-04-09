package com.iksystem.food.dashboard

import com.iksystem.common.checklist.model.Checklist
import com.iksystem.common.checklist.model.ChecklistFrequency
import com.iksystem.common.checklist.model.ChecklistItem
import com.iksystem.common.checklist.repository.ChecklistItemRepository
import com.iksystem.common.checklist.repository.ChecklistRepository
import com.iksystem.common.deviation.model.DeviationSeverity
import com.iksystem.common.security.AuthenticatedUser
import com.iksystem.common.training.model.TrainingStatus
import com.iksystem.common.training.repository.TrainingRepository
import com.iksystem.food.deviation.model.FoodDeviationStatus
import com.iksystem.food.deviation.repository.FoodDeviationRepository
import com.iksystem.food.temperature.model.TemperatureMeasurementStatus
import com.iksystem.food.temperature.repository.TemperatureApplianceRepository
import com.iksystem.food.temperature.repository.TemperatureMeasurementRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.Instant

class FoodDashboardServiceTest {

    private lateinit var foodDeviationRepository: FoodDeviationRepository
    private lateinit var measurementRepository: TemperatureMeasurementRepository
    private lateinit var applianceRepository: TemperatureApplianceRepository
    private lateinit var checklistRepository: ChecklistRepository
    private lateinit var checklistItemRepository: ChecklistItemRepository
    private lateinit var trainingRepository: TrainingRepository
    private lateinit var service: FoodDashboardService

    @BeforeEach
    fun setUp() {
        foodDeviationRepository = mock(FoodDeviationRepository::class.java)
        measurementRepository = mock(TemperatureMeasurementRepository::class.java)
        applianceRepository = mock(TemperatureApplianceRepository::class.java)
        checklistRepository = mock(ChecklistRepository::class.java)
        checklistItemRepository = mock(ChecklistItemRepository::class.java)
        trainingRepository = mock(TrainingRepository::class.java)

        service = FoodDashboardService(
            foodDeviationRepository = foodDeviationRepository,
            measurementRepository = measurementRepository,
            applianceRepository = applianceRepository,
            checklistRepository = checklistRepository,
            checklistItemRepository = checklistItemRepository,
            trainingRepository = trainingRepository
        )
    }

    @Test
    fun `getStats aggregates stable dashboard counts`() {
        val auth = AuthenticatedUser(
            10L,
            1L,
            "MANAGER"
        )

        `when`(foodDeviationRepository.countByOrganizationIdAndStatus(1L, FoodDeviationStatus.OPEN)).thenReturn(2L)
        `when`(foodDeviationRepository.countByOrganizationIdAndStatus(1L, FoodDeviationStatus.UNDER_TREATMENT)).thenReturn(1L)
        `when`(foodDeviationRepository.countByOrganizationIdAndStatus(1L, FoodDeviationStatus.CLOSED)).thenReturn(3L)

        `when`(foodDeviationRepository.countByOrganizationIdAndSeverity(1L, DeviationSeverity.CRITICAL)).thenReturn(1L)
        `when`(foodDeviationRepository.countByOrganizationIdAndSeverity(1L, DeviationSeverity.HIGH)).thenReturn(2L)
        `when`(foodDeviationRepository.countByOrganizationIdAndSeverity(1L, DeviationSeverity.MEDIUM)).thenReturn(2L)
        `when`(foodDeviationRepository.countByOrganizationIdAndSeverity(1L, DeviationSeverity.LOW)).thenReturn(1L)

        `when`(measurementRepository.countByOrganizationIdAndStatus(1L, TemperatureMeasurementStatus.DEVIATION)).thenReturn(3L)
        `when`(measurementRepository.countByOrganizationIdAndStatus(1L, TemperatureMeasurementStatus.OK)).thenReturn(7L)
        `when`(applianceRepository.findAllByOrganizationIdOrderByNameAsc(1L)).thenReturn(emptyList())

        val daily1 = checklist(id = 1L, frequency = ChecklistFrequency.DAILY, active = true)
        val daily2 = checklist(id = 2L, frequency = ChecklistFrequency.DAILY, active = true)

        `when`(checklistRepository.countByOrganizationIdAndActive(1L, true)).thenReturn(4L)
        `when`(checklistItemRepository.countAllByOrganizationId(1L)).thenReturn(12L)
        `when`(checklistItemRepository.countCompletedByOrganizationId(1L)).thenReturn(9L)

        `when`(
            checklistRepository.findAllByOrganizationIdAndFrequencyAndActive(
                1L,
                ChecklistFrequency.DAILY,
                true
            )
        ).thenReturn(listOf(daily1, daily2))

        `when`(checklistItemRepository.findAllByChecklistIdOrderByIdAsc(1L)).thenReturn(
            listOf(
                checklistItem(id = 11L, checklist = daily1, completed = true),
                checklistItem(id = 12L, checklist = daily1, completed = true)
            )
        )
        `when`(checklistItemRepository.findAllByChecklistIdOrderByIdAsc(2L)).thenReturn(
            listOf(
                checklistItem(id = 21L, checklist = daily2, completed = true),
                checklistItem(id = 22L, checklist = daily2, completed = false)
            )
        )

        for (frequency in ChecklistFrequency.entries) {
            val count = when (frequency) {
                ChecklistFrequency.DAILY -> 2L
                ChecklistFrequency.WEEKLY -> 1L
                ChecklistFrequency.MONTHLY -> 1L
                else -> 0L
            }

            `when`(
                checklistRepository.countByOrganizationIdAndFrequencyAndActive(
                    1L,
                    frequency,
                    true
                )
            ).thenReturn(count)
        }

        `when`(trainingRepository.countByOrganizationIdAndStatus(1L, TrainingStatus.COMPLETED)).thenReturn(5L)
        `when`(trainingRepository.countByOrganizationIdAndStatus(1L, TrainingStatus.EXPIRES_SOON)).thenReturn(2L)
        `when`(trainingRepository.countByOrganizationIdAndStatus(1L, TrainingStatus.EXPIRED)).thenReturn(1L)
        `when`(trainingRepository.countByOrganizationIdAndStatus(1L, TrainingStatus.NOT_COMPLETED)).thenReturn(2L)
        `when`(trainingRepository.countByOrganizationId(1L)).thenReturn(10L)

        val result = service.getStats(auth)

        assertEquals(2L, result.deviations.open)
        assertEquals(1L, result.deviations.underTreatment)
        assertEquals(3L, result.deviations.closed)
        assertEquals(1L, result.deviations.critical)
        assertEquals(2L, result.deviations.high)
        assertEquals(2L, result.deviations.medium)
        assertEquals(1L, result.deviations.low)
        assertTrue(result.deviations.byType.isEmpty())
        assertTrue(result.deviations.trend.isEmpty())

        assertEquals(10L, result.temperature.totalMeasurements)
        assertEquals(3L, result.temperature.deviationCount)
        assertEquals(7L, result.temperature.okCount)
        assertTrue(result.temperature.appliances.isEmpty())

        assertEquals(4L, result.checklists.totalActive)
        assertEquals(1L, result.checklists.completedToday)
        assertEquals(12L, result.checklists.totalItems)
        assertEquals(9L, result.checklists.completedItems)
        assertEquals(2L, result.checklists.byFrequency[ChecklistFrequency.DAILY.name])
        assertEquals(1L, result.checklists.byFrequency[ChecklistFrequency.WEEKLY.name])
        assertEquals(1L, result.checklists.byFrequency[ChecklistFrequency.MONTHLY.name])

        assertEquals(5L, result.training.completed)
        assertEquals(2L, result.training.expiresSoon)
        assertEquals(1L, result.training.expired)
        assertEquals(2L, result.training.notCompleted)
        assertEquals(10L, result.training.total)
        assertEquals(50, result.training.compliancePercent)
    }

    @Test
    fun `getStats returns zero compliance when no training exists`() {
        val auth = AuthenticatedUser(
            10L,
            1L,
            "MANAGER"
        )

        `when`(foodDeviationRepository.countByOrganizationIdAndStatus(1L, FoodDeviationStatus.OPEN)).thenReturn(0L)
        `when`(foodDeviationRepository.countByOrganizationIdAndStatus(1L, FoodDeviationStatus.UNDER_TREATMENT)).thenReturn(0L)
        `when`(foodDeviationRepository.countByOrganizationIdAndStatus(1L, FoodDeviationStatus.CLOSED)).thenReturn(0L)

        `when`(foodDeviationRepository.countByOrganizationIdAndSeverity(1L, DeviationSeverity.CRITICAL)).thenReturn(0L)
        `when`(foodDeviationRepository.countByOrganizationIdAndSeverity(1L, DeviationSeverity.HIGH)).thenReturn(0L)
        `when`(foodDeviationRepository.countByOrganizationIdAndSeverity(1L, DeviationSeverity.MEDIUM)).thenReturn(0L)
        `when`(foodDeviationRepository.countByOrganizationIdAndSeverity(1L, DeviationSeverity.LOW)).thenReturn(0L)

        `when`(measurementRepository.countByOrganizationIdAndStatus(1L, TemperatureMeasurementStatus.DEVIATION)).thenReturn(0L)
        `when`(measurementRepository.countByOrganizationIdAndStatus(1L, TemperatureMeasurementStatus.OK)).thenReturn(0L)
        `when`(applianceRepository.findAllByOrganizationIdOrderByNameAsc(1L)).thenReturn(emptyList())

        `when`(checklistRepository.countByOrganizationIdAndActive(1L, true)).thenReturn(0L)
        `when`(checklistItemRepository.countAllByOrganizationId(1L)).thenReturn(0L)
        `when`(checklistItemRepository.countCompletedByOrganizationId(1L)).thenReturn(0L)
        `when`(
            checklistRepository.findAllByOrganizationIdAndFrequencyAndActive(
                1L,
                ChecklistFrequency.DAILY,
                true
            )
        ).thenReturn(emptyList())

        for (frequency in ChecklistFrequency.entries) {
            `when`(
                checklistRepository.countByOrganizationIdAndFrequencyAndActive(
                    1L,
                    frequency,
                    true
                )
            ).thenReturn(0L)
        }

        `when`(trainingRepository.countByOrganizationIdAndStatus(1L, TrainingStatus.COMPLETED)).thenReturn(0L)
        `when`(trainingRepository.countByOrganizationIdAndStatus(1L, TrainingStatus.EXPIRES_SOON)).thenReturn(0L)
        `when`(trainingRepository.countByOrganizationIdAndStatus(1L, TrainingStatus.EXPIRED)).thenReturn(0L)
        `when`(trainingRepository.countByOrganizationIdAndStatus(1L, TrainingStatus.NOT_COMPLETED)).thenReturn(0L)
        `when`(trainingRepository.countByOrganizationId(1L)).thenReturn(0L)

        val result = service.getStats(auth)

        assertEquals(0, result.training.compliancePercent)
        assertEquals(0L, result.temperature.totalMeasurements)
        assertEquals(0L, result.checklists.completedToday)
        assertEquals(0L, result.checklists.totalItems)
        assertEquals(0L, result.checklists.completedItems)
        assertTrue(result.deviations.byType.isEmpty())
        assertTrue(result.deviations.trend.isEmpty())
    }

    private fun checklist(
        id: Long,
        frequency: ChecklistFrequency,
        active: Boolean
    ) = Checklist(
        id = id,
        organizationId = 1L,
        name = "Checklist $id",
        description = null,
        frequency = frequency,
        active = active,
        source = "MANUAL"
    )

    private fun checklistItem(
        id: Long,
        checklist: Checklist,
        completed: Boolean
    ) = ChecklistItem(
        id = id,
        checklist = checklist,
        title = "Item $id",
        description = null,
        sortOrder = 0,
        completed = completed,
        completedAt = if (completed) Instant.parse("2026-04-08T10:00:00Z") else null
    )
}