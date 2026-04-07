package com.iksystem.alcohol.ageverification.dto

import com.iksystem.alcohol.ageverification.model.ShiftStatus
import com.iksystem.alcohol.deviation.model.AlcoholDeviationType
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size


data class UpdateIdCheckCountRequest(
    @field:NotNull(message = "Count is required")
    @field:Min(value = 0, message = "Count cannot be negative")
    val idsCheckedCount: Int,
)

data class CreateShiftDeviationRequest(
    @field:NotNull(message = "Deviation type is required")
    val deviationType: AlcoholDeviationType,

    @field:Size(max = 5000, message = "Description cannot exceed 5000 characters")
    val description: String? = null,
)


data class ShiftResponse(
    val id: Long,
    val organizationId: Long,
    val userId: Long,
    val userName: String,
    val shiftDate: String,
    val startedAt: String,
    val endedAt: String?,
    val idsCheckedCount: Int,
    val signedOff: Boolean,
    val signedOffAt: String?,
    val status: ShiftStatus,
    val deviationCount: Int,
    val createdAt: String,
    val updatedAt: String,
)

data class ShiftDetailResponse(
    val shift: ShiftResponse,
    val deviations: List<ShiftDeviationResponse>,
)

data class ShiftDeviationResponse(
    val id: Long,
    val deviationType: AlcoholDeviationType,
    val description: String,
    val reportedAt: String,
)


data class DailySummaryResponse(
    val date: String,
    val shiftCount: Long,
    val totalIdsChecked: Long,
    val totalDeviations: Long,
)

data class DayDetailResponse(
    val date: String,
    val shifts: List<ShiftDetailResponse>,
    val totalIdsChecked: Int,
    val totalDeviations: Int,
    val deviationsByType: Map<AlcoholDeviationType, Int>,
)

data class StatsResponse(
    val periodFrom: String,
    val periodTo: String,
    val totalShifts: Long,
    val totalIdsChecked: Long,
    val totalDeviations: Long,
    val avgIdsPerShift: Double,
    val dailySummaries: List<DailySummaryResponse>,
)
