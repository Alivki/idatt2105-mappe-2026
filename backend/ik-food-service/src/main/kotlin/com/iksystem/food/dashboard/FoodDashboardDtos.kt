package com.iksystem.food.dashboard

import java.math.BigDecimal

/**
 * Aggregated response containing all food dashboard statistics.
 */
data class FoodDashboardStatsResponse(
    val deviations: DeviationStats,
    val temperature: TemperatureStats,
    val checklists: ChecklistStats,
    val training: TrainingStats,
)

/**
 * Statistics related to deviations.
 */
data class DeviationStats(
    val open: Long,
    val underTreatment: Long,
    val closed: Long,
    val critical: Long,
    val high: Long,
    val medium: Long,
    val low: Long,
    val byType: Map<String, Long>,
    val trend: List<DailyCount>,
)

/**
 * Represents count per day.
 */
data class DailyCount(
    val date: String,
    val count: Long,
)

/**
 * Statistics for temperature monitoring.
 */
data class TemperatureStats(
    val totalMeasurements: Long,
    val deviationCount: Long,
    val okCount: Long,
    val appliances: List<ApplianceStatus>,
)

/**
 * Status of a monitored appliance.
 */
data class ApplianceStatus(
    val id: Long,
    val name: String,
    val applianceType: String,
    val lastTemperature: BigDecimal?,
    val lastStatus: String?,
    val minThreshold: BigDecimal,
    val maxThreshold: BigDecimal,
)

/**
 * Statistics for checklists.
 */
data class ChecklistStats(
    val totalActive: Long,
    val completedToday: Long,
    val totalItems: Long,
    val completedItems: Long,
    val byFrequency: Map<String, Long>,
)

/**
 * Statistics for training compliance.
 */
data class TrainingStats(
    val completed: Long,
    val expiresSoon: Long,
    val expired: Long,
    val notCompleted: Long,
    val total: Long,
    val compliancePercent: Int,
)