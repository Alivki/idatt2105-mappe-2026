package com.iksystem.alcohol.dashboard

data class AlcoholDashboardStatsResponse(
    val deviations: AlcoholDeviationStats,
    val penaltyPoints: PenaltyPointStats,
    val ageVerification: AgeVerificationStats,
    val policyStatus: PolicyStatus,
)

data class AlcoholDeviationStats(
    val open: Long,
    val underTreatment: Long,
    val closed: Long,
    val byType: Map<String, Long>,
    val trend: List<AlcoholDailyCount>,
)

data class AlcoholDailyCount(
    val date: String,
    val count: Long,
)

data class PenaltyPointStats(
    val totalPoints: Int,
    val maxPoints: Int,
    val entries: List<PenaltyEntry>,
)

data class PenaltyEntry(
    val id: Long,
    val violationType: String,
    val points: Int,
    val description: String?,
    val createdAt: String,
)

data class AgeVerificationStats(
    val totalShifts: Long,
    val totalIdsChecked: Long,
    val totalDeviations: Long,
    val avgIdsPerShift: Double,
)

data class PolicyStatus(
    val exists: Boolean,
    val bevillingNumber: String?,
    val bevillingValidUntil: String?,
    val isExpiringSoon: Boolean,
)
