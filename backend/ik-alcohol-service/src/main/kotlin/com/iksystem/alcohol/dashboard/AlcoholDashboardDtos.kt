package com.iksystem.alcohol.dashboard

/**
 * Top-level response DTO for the alcohol dashboard, aggregating all statistical
 * categories into a single response.
 *
 * @property deviations Statistics related to alcohol deviation registrations.
 * @property penaltyPoints Statistics related to accumulated penalty points.
 * @property ageVerification Statistics related to age verification shifts and ID checks.
 * @property policyStatus The current status of the organization's alcohol policy.
 */
data class AlcoholDashboardStatsResponse(
    val deviations: AlcoholDeviationStats,
    val penaltyPoints: PenaltyPointStats,
    val ageVerification: AgeVerificationStats,
    val policyStatus: PolicyStatus,
)

/**
 * Statistics summarizing the state and distribution of alcohol deviation registrations.
 *
 * @property open The number of deviations currently in an open state.
 * @property underTreatment The number of deviations currently being handled.
 * @property closed The number of deviations that have been resolved and closed.
 * @property byType A breakdown of deviation counts keyed by deviation type name.
 * @property trend A chronological list of daily deviation counts for trend visualization.
 */
data class AlcoholDeviationStats(
    val open: Long,
    val underTreatment: Long,
    val closed: Long,
    val byType: Map<String, Long>,
    val trend: List<AlcoholDailyCount>,
)

/**
 * Represents the number of deviations registered on a specific date.
 *
 * Used to build trend charts over a time range.
 *
 * @property date The date of the data point, formatted as an ISO-8601 date string (e.g. `2024-06-01`).
 * @property count The number of deviations registered on this date.
 */
data class AlcoholDailyCount(
    val date: String,
    val count: Long,
)

/**
 * Statistics summarizing the organization's accumulated penalty points.
 *
 * @property totalPoints The total number of penalty points currently accumulated.
 * @property maxPoints The maximum number of penalty points allowed before licence revocation.
 * @property entries A list of individual penalty point entries contributing to the total.
 */
data class PenaltyPointStats(
    val totalPoints: Int,
    val maxPoints: Int,
    val entries: List<PenaltyEntry>,
)

/**
 * Represents a single penalty point entry issued against the organization.
 *
 * @property id The unique identifier of the penalty entry.
 * @property violationType The type of violation that triggered this penalty.
 * @property points The number of points assigned for this violation.
 * @property description An optional description providing further context for the penalty.
 * @property createdAt The timestamp when the penalty was recorded, as an ISO-8601 string.
 */
data class PenaltyEntry(
    val id: Long,
    val violationType: String,
    val points: Int,
    val description: String?,
    val createdAt: String,
)

/**
 * Statistics summarizing age verification activity across shifts.
 *
 * @property totalShifts The total number of shifts during which age verification was performed.
 * @property totalIdsChecked The total number of ID checks carried out across all shifts.
 * @property totalDeviations The total number of age verification deviations registered.
 * @property avgIdsPerShift The average number of ID checks performed per shift.
 */
data class AgeVerificationStats(
    val totalShifts: Long,
    val totalIdsChecked: Long,
    val totalDeviations: Long,
    val avgIdsPerShift: Double,
)

/**
 * Represents the current status of an organization's alcohol policy (bevilling).
 *
 * @property exists Whether the organization has an alcohol policy on record.
 * @property bevillingNumber The liquor licence number, if available.
 * @property bevillingValidUntil The expiry date of the licence as an ISO-8601 date string, if set.
 * @property isExpiringSoon Whether the licence is close to its expiry date and requires attention.
 */
data class PolicyStatus(
    val exists: Boolean,
    val bevillingNumber: String?,
    val bevillingValidUntil: String?,
    val isExpiringSoon: Boolean,
)