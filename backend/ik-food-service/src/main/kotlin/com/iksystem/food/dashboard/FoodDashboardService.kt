package com.iksystem.food.dashboard

import com.iksystem.common.checklist.model.ChecklistFrequency
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
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Service responsible for aggregating food dashboard statistics.
 */
@Service
class FoodDashboardService(
    private val foodDeviationRepository: FoodDeviationRepository,
    private val measurementRepository: TemperatureMeasurementRepository,
    private val applianceRepository: TemperatureApplianceRepository,
    private val checklistRepository: ChecklistRepository,
    private val checklistItemRepository: ChecklistItemRepository,
    private val trainingRepository: TrainingRepository,
) {

    /**
     * Returns all dashboard statistics for the user's organization.
     */
    @Transactional(readOnly = true)
    fun getStats(auth: AuthenticatedUser): FoodDashboardStatsResponse {
        val orgId = auth.requireOrganizationId()
        return FoodDashboardStatsResponse(
            deviations = getDeviationStats(orgId),
            temperature = getTemperatureStats(orgId),
            checklists = getChecklistStats(orgId),
            training = getTrainingStats(orgId),
        )
    }

    /**
     * Builds deviation-related statistics.
     */
    private fun getDeviationStats(orgId: Long): DeviationStats {
        val open = foodDeviationRepository.countByOrganizationIdAndStatus(orgId, FoodDeviationStatus.OPEN)
        val underTreatment = foodDeviationRepository.countByOrganizationIdAndStatus(orgId, FoodDeviationStatus.UNDER_TREATMENT)
        val closed = foodDeviationRepository.countByOrganizationIdAndStatus(orgId, FoodDeviationStatus.CLOSED)

        val critical = foodDeviationRepository.countByOrganizationIdAndSeverity(orgId, DeviationSeverity.CRITICAL)
        val high = foodDeviationRepository.countByOrganizationIdAndSeverity(orgId, DeviationSeverity.HIGH)
        val medium = foodDeviationRepository.countByOrganizationIdAndSeverity(orgId, DeviationSeverity.MEDIUM)
        val low = foodDeviationRepository.countByOrganizationIdAndSeverity(orgId, DeviationSeverity.LOW)

        val since = Instant.now().minus(30, ChronoUnit.DAYS)
        val recent = foodDeviationRepository.findRecentByOrganizationId(orgId, since)

        val byType = recent.groupBy { it.deviationType.name }.mapValues { it.value.size.toLong() }

        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val zone = ZoneId.systemDefault()
        val trend = recent
            .groupBy { it.reportedAt.atZone(zone).toLocalDate() }
            .map { (date, items) -> DailyCount(date.format(formatter), items.size.toLong()) }
            .sortedBy { it.date }

        return DeviationStats(
            open = open, underTreatment = underTreatment, closed = closed,
            critical = critical, high = high, medium = medium, low = low,
            byType = byType, trend = trend,
        )
    }

    /**
     * Builds temperature-related statistics.
     */
    private fun getTemperatureStats(orgId: Long): TemperatureStats {
        val deviationCount = measurementRepository.countByOrganizationIdAndStatus(orgId, TemperatureMeasurementStatus.DEVIATION)
        val okCount = measurementRepository.countByOrganizationIdAndStatus(orgId, TemperatureMeasurementStatus.OK)

        val appliances = applianceRepository.findAllByOrganizationIdOrderByNameAsc(orgId).map { appliance ->
            val last = measurementRepository.findTopByOrganizationIdAndApplianceIdOrderByMeasuredAtDesc(orgId, appliance.id)
            ApplianceStatus(
                id = appliance.id,
                name = appliance.name,
                applianceType = appliance.applianceType.name,
                lastTemperature = last?.temperature,
                lastStatus = last?.status?.name,
                minThreshold = appliance.minTemperature,
                maxThreshold = appliance.maxTemperature,
            )
        }

        return TemperatureStats(
            totalMeasurements = deviationCount + okCount,
            deviationCount = deviationCount,
            okCount = okCount,
            appliances = appliances,
        )
    }

    /**
     * Builds checklist-related statistics.
     */
    private fun getChecklistStats(orgId: Long): ChecklistStats {
        val totalActive = checklistRepository.countByOrganizationIdAndActive(orgId, true)
        val totalItems = checklistItemRepository.countAllByOrganizationId(orgId)
        val completedItems = checklistItemRepository.countCompletedByOrganizationId(orgId)

        val dailyChecklists = checklistRepository.findAllByOrganizationIdAndFrequencyAndActive(orgId, ChecklistFrequency.DAILY, true)
        val completedToday = dailyChecklists.count { checklist ->
            val items = checklistItemRepository.findAllByChecklistIdOrderByIdAsc(checklist.id)
            items.isNotEmpty() && items.all { it.completed }
        }.toLong()

        val byFrequency = ChecklistFrequency.entries.associate { freq ->
            freq.name to checklistRepository.countByOrganizationIdAndFrequencyAndActive(orgId, freq, true)
        }

        return ChecklistStats(
            totalActive = totalActive,
            completedToday = completedToday,
            totalItems = totalItems,
            completedItems = completedItems,
            byFrequency = byFrequency,
        )
    }

    /**
     * Builds training-related statistics.
     */
    private fun getTrainingStats(orgId: Long): TrainingStats {
        val completed = trainingRepository.countByOrganizationIdAndStatus(orgId, TrainingStatus.COMPLETED)
        val expiresSoon = trainingRepository.countByOrganizationIdAndStatus(orgId, TrainingStatus.EXPIRES_SOON)
        val expired = trainingRepository.countByOrganizationIdAndStatus(orgId, TrainingStatus.EXPIRED)
        val notCompleted = trainingRepository.countByOrganizationIdAndStatus(orgId, TrainingStatus.NOT_COMPLETED)
        val total = trainingRepository.countByOrganizationId(orgId)
        val compliancePercent = if (total > 0) ((completed * 100) / total).toInt() else 0

        return TrainingStats(
            completed = completed,
            expiresSoon = expiresSoon,
            expired = expired,
            notCompleted = notCompleted,
            total = total,
            compliancePercent = compliancePercent,
        )
    }
}