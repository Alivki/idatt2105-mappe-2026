package com.iksystem.alcohol.ageverification.dto

import com.iksystem.alcohol.ageverification.model.ShiftStatus
import com.iksystem.alcohol.deviation.model.AlcoholDeviationType
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

/**
 * Request object for updating the number of ID checks performed during a shift.
 *
 * @property idsCheckedCount Total number of IDs checked. Must be zero or greater.
 */
data class UpdateIdCheckCountRequest(
    @field:NotNull(message = "Count is required")
    @field:Min(value = 0, message = "Count cannot be negative")
    val idsCheckedCount: Int,
)

/**
 * Request object for creating a deviation associated with a shift.
 *
 * @property deviationType Type/category of the deviation
 * @property description Optional description providing additional context (max 5000 characters)
 */
data class CreateShiftDeviationRequest(
    @field:NotNull(message = "Deviation type is required")
    val deviationType: AlcoholDeviationType,

    @field:Size(max = 5000, message = "Description cannot exceed 5000 characters")
    val description: String? = null,
)

/**
 * Represents a summary view of a shift.
 *
 * This DTO is used in most responses where shift-level information is required.
 *
 * @property id Unique identifier of the shift
 * @property organizationId ID of the organization the shift belongs to
 * @property userId ID of the user who performed the shift
 * @property userName Name of the user
 * @property shiftDate Date of the shift (ISO format)
 * @property startedAt Timestamp when the shift started
 * @property endedAt Timestamp when the shift ended (null if ongoing)
 * @property idsCheckedCount Number of IDs checked during the shift
 * @property signedOff Indicates whether the shift has been signed off
 * @property signedOffAt Timestamp of sign-off (if applicable)
 * @property status Current status of the shift
 * @property deviationCount Number of deviations registered during the shift
 * @property createdAt Timestamp when the shift was created
 * @property updatedAt Timestamp when the shift was last updated
 */
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

/**
 * Represents a detailed view of a shift, including associated deviations.
 *
 * @property shift The shift summary information
 * @property deviations List of deviations linked to the shift
 */
data class ShiftDetailResponse(
    val shift: ShiftResponse,
    val deviations: List<ShiftDeviationResponse>,
)

/**
 * Represents a deviation recorded during a shift.
 *
 * @property id Unique identifier of the deviation
 * @property deviationType Type/category of the deviation
 * @property description Description of the deviation
 * @property reportedAt Timestamp when the deviation was reported
 */
data class ShiftDeviationResponse(
    val id: Long,
    val deviationType: AlcoholDeviationType,
    val description: String,
    val reportedAt: String,
)

/**
 * Represents an aggregated summary for a single day.
 *
 * @property date The date of the summary (ISO format)
 * @property shiftCount Total number of shifts that day
 * @property totalIdsChecked Total number of IDs checked across all shifts
 * @property totalDeviations Total number of deviations recorded
 */
data class DailySummaryResponse(
    val date: String,
    val shiftCount: Long,
    val totalIdsChecked: Long,
    val totalDeviations: Long,
)

/**
 * Represents a detailed breakdown of a specific day.
 *
 * Includes all shifts, totals, and deviation distribution.
 *
 * @property date The date (ISO format)
 * @property shifts List of detailed shifts for the day
 * @property totalIdsChecked Total number of IDs checked
 * @property totalDeviations Total number of deviations
 * @property deviationsByType Breakdown of deviations grouped by type
 */
data class DayDetailResponse(
    val date: String,
    val shifts: List<ShiftDetailResponse>,
    val totalIdsChecked: Int,
    val totalDeviations: Int,
    val deviationsByType: Map<AlcoholDeviationType, Int>,
)

/**
 * Represents aggregated statistics and trends over a given period.
 *
 * @property periodFrom Start date of the period (ISO format)
 * @property periodTo End date of the period (ISO format)
 * @property totalShifts Total number of shifts in the period
 * @property totalIdsChecked Total number of IDs checked
 * @property totalDeviations Total number of deviations recorded
 * @property avgIdsPerShift Average number of IDs checked per shift
 * @property dailySummaries List of daily summaries within the period
 */
data class StatsResponse(
    val periodFrom: String,
    val periodTo: String,
    val totalShifts: Long,
    val totalIdsChecked: Long,
    val totalDeviations: Long,
    val avgIdsPerShift: Double,
    val dailySummaries: List<DailySummaryResponse>,
)