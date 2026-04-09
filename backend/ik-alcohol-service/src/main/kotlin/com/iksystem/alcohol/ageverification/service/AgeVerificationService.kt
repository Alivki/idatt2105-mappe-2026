package com.iksystem.alcohol.ageverification.service

import com.iksystem.alcohol.ageverification.dto.*
import com.iksystem.alcohol.ageverification.model.AgeVerificationShift
import com.iksystem.alcohol.ageverification.model.ShiftStatus
import com.iksystem.alcohol.ageverification.repository.AgeVerificationShiftRepository
import com.iksystem.alcohol.deviation.model.*
import com.iksystem.alcohol.deviation.repository.AlcoholDeviationRepository
import com.iksystem.common.exception.BadRequestException
import com.iksystem.common.exception.ConflictException
import com.iksystem.common.exception.ForbiddenException
import com.iksystem.common.exception.NotFoundException
import com.iksystem.common.security.AuthenticatedUser
import com.iksystem.common.user.model.User
import com.iksystem.common.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate

/**
 * Service responsible for managing age verification shifts and related reporting.
 *
 * Handles:
 * - Shift lifecycle operations such as start, update, end, and reopen
 * - Registration of shift-related deviations
 * - Access control for employees and managers
 * - Aggregated reporting for daily summaries and statistics
 *
 * All operations are scoped to the authenticated user's organization.
 */
@Service
class AgeVerificationService(
    private val shiftRepository: AgeVerificationShiftRepository,
    private val deviationRepository: AlcoholDeviationRepository,
    private val userRepository: UserRepository,
) {

    /**
     * Retrieves the authenticated user's current active shift.
     *
     * If no active shift exists, today's most recent completed shift is returned if available.
     * Otherwise, the method returns null.
     *
     * @param auth The authenticated user
     * @return Active shift, latest completed shift for today, or null
     */
    @Transactional(readOnly = true)
    fun getActiveShift(auth: AuthenticatedUser): ShiftDetailResponse? {
        val orgId = auth.requireOrganizationId()
        val shift = shiftRepository.findByUserIdAndOrganizationIdAndStatus(
            auth.userId, orgId, ShiftStatus.ACTIVE
        )
            ?: shiftRepository.findFirstByUserIdAndOrganizationIdAndShiftDateOrderByStartedAtDesc(
                auth.userId,
                orgId,
                LocalDate.now(),
            )
                ?.takeIf { it.status == ShiftStatus.COMPLETED }
            ?: return null
        return toShiftDetail(shift)
    }

    /**
     * Starts a new shift for the authenticated user.
     *
     * Business rules:
     * - A user can only have one active shift at a time
     * - A new shift cannot be started if today's shift has already been completed
     *
     * @param auth The authenticated user
     * @return The created shift response
     * @throws ConflictException If the user already has an active shift
     * @throws BadRequestException If today's shift has already been completed
     */
    @Transactional
    fun startShift(auth: AuthenticatedUser): ShiftResponse {
        val orgId = auth.requireOrganizationId()
        val user = requireUser(auth.userId)
        val today = LocalDate.now()

        val existing = shiftRepository.findByUserIdAndOrganizationIdAndStatus(
            auth.userId, orgId, ShiftStatus.ACTIVE
        )
        if (existing != null) {
            throw ConflictException("Du har allerede et aktivt skift")
        }

        val todayShift = shiftRepository.findFirstByUserIdAndOrganizationIdAndShiftDateOrderByStartedAtDesc(
            auth.userId,
            orgId,
            today,
        )
        if (todayShift != null) {
            throw BadRequestException("Skiftet for i dag er allerede avsluttet. Du kan gjenapne dagens skift, eller starte nytt i morgen.")
        }

        val shift = shiftRepository.save(
            AgeVerificationShift(
                organizationId = orgId,
                user = user,
                shiftDate = today,
            )
        )
        return shift.toResponse(0)
    }

    /**
     * Updates the number of IDs checked for an active shift.
     *
     * Only the owner of an active shift may perform this operation.
     *
     * @param shiftId The shift ID
     * @param request Request containing the updated ID check count
     * @param auth The authenticated user
     * @return The updated shift response
     */
    @Transactional
    fun updateIdCheckCount(shiftId: Long, request: UpdateIdCheckCountRequest, auth: AuthenticatedUser): ShiftResponse {
        val shift = requireOwnActiveShift(shiftId, auth)
        val updated = shiftRepository.save(
            shift.copy(
                idsCheckedCount = request.idsCheckedCount,
                updatedAt = Instant.now(),
            )
        )
        val deviationCount = deviationRepository.findAllByAgeVerificationShiftId(shiftId).size
        return updated.toResponse(deviationCount)
    }

    /**
     * Creates a deviation linked to the specified active shift.
     *
     * If no description is provided, a default human-readable description
     * is generated from the deviation type.
     *
     * @param shiftId The shift ID
     * @param request Request containing deviation details
     * @param auth The authenticated user
     * @return The created shift deviation response
     */
    @Transactional
    fun createShiftDeviation(shiftId: Long, request: CreateShiftDeviationRequest, auth: AuthenticatedUser): ShiftDeviationResponse {
        val shift = requireOwnActiveShift(shiftId, auth)
        val user = requireUser(auth.userId)

        val description = request.description?.trim()?.takeIf { it.isNotEmpty() }
            ?: request.deviationType.name.replace("_", " ").lowercase()
                .replaceFirstChar { it.uppercase() }

        val deviation = deviationRepository.save(
            AlcoholDeviation(
                organizationId = shift.organizationId,
                reportedByUser = user,
                reportSource = AlcoholReportSource.EGENRAPPORT,
                deviationType = request.deviationType,
                description = description,
                ageVerificationShiftId = shift.id,
            )
        )
        return deviation.toShiftDeviationResponse()
    }

    /**
     * Ends and signs off an active shift.
     *
     * The shift is marked as completed and finalized.
     *
     * @param shiftId The shift ID
     * @param auth The authenticated user
     * @return The completed shift response
     */
    @Transactional
    fun endShift(shiftId: Long, auth: AuthenticatedUser): ShiftResponse {
        val shift = requireOwnActiveShift(shiftId, auth)
        val now = Instant.now()
        val updated = shiftRepository.save(
            shift.copy(
                endedAt = now,
                signedOff = true,
                signedOffAt = now,
                status = ShiftStatus.COMPLETED,
                updatedAt = now,
            )
        )
        val deviationCount = deviationRepository.findAllByAgeVerificationShiftId(shiftId).size
        return updated.toResponse(deviationCount)
    }

    /**
     * Reopens a previously completed shift from the current day.
     *
     * Business rules:
     * - Only the owner can reopen the shift
     * - Only completed shifts can be reopened
     * - Only today's shift can be reopened
     * - The user cannot already have another active shift
     *
     * @param shiftId The shift ID
     * @param auth The authenticated user
     * @return The reopened shift response
     */
    @Transactional
    fun reopenShift(shiftId: Long, auth: AuthenticatedUser): ShiftResponse {
        val orgId = auth.requireOrganizationId()
        val shift = shiftRepository.findByIdAndOrganizationId(shiftId, orgId)
            ?: throw NotFoundException("Skift ikke funnet")

        if (shift.user.id != auth.userId) {
            throw ForbiddenException("Du kan bare gjenapne ditt eget skift")
        }
        if (shift.status != ShiftStatus.COMPLETED) {
            throw BadRequestException("Kun avsluttede skift kan gjenapnes")
        }
        if (shift.shiftDate != LocalDate.now()) {
            throw BadRequestException("Kun dagens skift kan gjenapnes")
        }

        val active = shiftRepository.findByUserIdAndOrganizationIdAndStatus(auth.userId, orgId, ShiftStatus.ACTIVE)
        if (active != null && active.id != shift.id) {
            throw ConflictException("Du har allerede et aktivt skift")
        }

        val updated = shiftRepository.save(
            shift.copy(
                endedAt = null,
                signedOff = false,
                signedOffAt = null,
                status = ShiftStatus.ACTIVE,
                updatedAt = Instant.now(),
            )
        )
        val deviationCount = deviationRepository.findAllByAgeVerificationShiftId(shiftId).size
        return updated.toResponse(deviationCount)
    }

    /**
     * Retrieves detailed information about a specific shift.
     *
     * Access is granted to:
     * - The owner of the shift
     * - Users with ADMIN or MANAGER role
     *
     * @param shiftId The shift ID
     * @param auth The authenticated user
     * @return Detailed shift information
     * @throws ForbiddenException If the user does not have access to the shift
     */
    @Transactional(readOnly = true)
    fun getShiftById(shiftId: Long, auth: AuthenticatedUser): ShiftDetailResponse {
        val orgId = auth.requireOrganizationId()
        val shift = shiftRepository.findByIdAndOrganizationId(shiftId, orgId)
            ?: throw NotFoundException("Skift ikke funnet")

        val role = auth.requireRole()
        if (role != "ADMIN" && role != "MANAGER" && shift.user.id != auth.userId) {
            throw ForbiddenException("Ingen tilgang til dette skiftet")
        }
        return toShiftDetail(shift)
    }

    /**
     * Retrieves aggregated daily summaries for a date range.
     *
     * Each summary includes:
     * - Shift count
     * - Total IDs checked
     * - Total deviations
     *
     * @param from Start date, inclusive
     * @param to End date, inclusive
     * @param auth The authenticated user
     * @return List of daily summaries
     */
    @Transactional(readOnly = true)
    fun getDailySummaries(from: LocalDate, to: LocalDate, auth: AuthenticatedUser): List<DailySummaryResponse> {
        val orgId = auth.requireOrganizationId()
        val summaries = shiftRepository.findDailySummaries(orgId, from, to)

        val shifts = shiftRepository.findByOrganizationIdAndShiftDateBetweenOrderByShiftDateDesc(orgId, from, to)
        val shiftIds = shifts.map { it.id }
        val deviationsByShift = if (shiftIds.isNotEmpty()) {
            deviationRepository.findAllByAgeVerificationShiftIdIn(shiftIds)
                .groupBy { it.ageVerificationShiftId }
        } else emptyMap()

        val deviationsByDate = shifts.groupBy { it.shiftDate }.mapValues { (_, dateShifts) ->
            dateShifts.sumOf { s -> deviationsByShift[s.id]?.size ?: 0 }.toLong()
        }

        return summaries.map { s ->
            DailySummaryResponse(
                date = s.shiftDate.toString(),
                shiftCount = s.shiftCount,
                totalIdsChecked = s.totalIdsChecked,
                totalDeviations = deviationsByDate[s.shiftDate] ?: 0,
            )
        }
    }

    /**
     * Retrieves detailed information for a specific day.
     *
     * Includes all shifts, total ID checks, total deviations,
     * and a breakdown of deviations by type.
     *
     * @param date The date to retrieve details for
     * @param auth The authenticated user
     * @return Detailed day information
     */
    @Transactional(readOnly = true)
    fun getDayDetail(date: LocalDate, auth: AuthenticatedUser): DayDetailResponse {
        val orgId = auth.requireOrganizationId()
        val shifts = shiftRepository.findByOrganizationIdAndShiftDateOrderByStartedAt(orgId, date)
        val shiftIds = shifts.map { it.id }

        val allDeviations = if (shiftIds.isNotEmpty()) {
            deviationRepository.findAllByAgeVerificationShiftIdIn(shiftIds)
        } else emptyList()

        val deviationsByShift = allDeviations.groupBy { it.ageVerificationShiftId }

        val shiftDetails = shifts.map { shift ->
            val shiftDeviations = deviationsByShift[shift.id] ?: emptyList()
            ShiftDetailResponse(
                shift = shift.toResponse(shiftDeviations.size),
                deviations = shiftDeviations.map { it.toShiftDeviationResponse() },
            )
        }

        val deviationsByType = allDeviations.groupBy { it.deviationType }
            .mapValues { (_, devs) -> devs.size }

        return DayDetailResponse(
            date = date.toString(),
            shifts = shiftDetails,
            totalIdsChecked = shifts.sumOf { it.idsCheckedCount },
            totalDeviations = allDeviations.size,
            deviationsByType = deviationsByType,
        )
    }

    /**
     * Retrieves statistics and trends for a date range.
     *
     * Includes:
     * - Total shifts
     * - Total IDs checked
     * - Total deviations
     * - Average IDs checked per shift
     * - Daily summaries
     *
     * @param from Start date, inclusive
     * @param to End date, inclusive
     * @param auth The authenticated user
     * @return Statistics for the selected period
     */
    @Transactional(readOnly = true)
    fun getStats(from: LocalDate, to: LocalDate, auth: AuthenticatedUser): StatsResponse {
        val orgId = auth.requireOrganizationId()
        val summaries = shiftRepository.findDailySummaries(orgId, from, to)

        val totalShifts = summaries.sumOf { it.shiftCount }
        val totalIds = summaries.sumOf { it.totalIdsChecked }

        val shifts = shiftRepository.findByOrganizationIdAndShiftDateBetweenOrderByShiftDateDesc(orgId, from, to)
        val shiftIds = shifts.map { it.id }
        val totalDeviations = if (shiftIds.isNotEmpty()) {
            deviationRepository.countByAgeVerificationShiftIdIn(shiftIds)
        } else 0L

        val deviationsByShift = if (shiftIds.isNotEmpty()) {
            deviationRepository.findAllByAgeVerificationShiftIdIn(shiftIds)
                .groupBy { it.ageVerificationShiftId }
        } else emptyMap()

        val deviationsByDate = shifts.groupBy { it.shiftDate }.mapValues { (_, dateShifts) ->
            dateShifts.sumOf { s -> deviationsByShift[s.id]?.size ?: 0 }.toLong()
        }

        val dailySummaries = summaries.map { s ->
            DailySummaryResponse(
                date = s.shiftDate.toString(),
                shiftCount = s.shiftCount,
                totalIdsChecked = s.totalIdsChecked,
                totalDeviations = deviationsByDate[s.shiftDate] ?: 0,
            )
        }

        return StatsResponse(
            periodFrom = from.toString(),
            periodTo = to.toString(),
            totalShifts = totalShifts,
            totalIdsChecked = totalIds,
            totalDeviations = totalDeviations,
            avgIdsPerShift = if (totalShifts > 0) totalIds.toDouble() / totalShifts else 0.0,
            dailySummaries = dailySummaries,
        )
    }

    /**
     * Ensures that the specified shift belongs to the authenticated user and is active.
     *
     * @param shiftId The shift ID
     * @param auth The authenticated user
     * @return The active shift
     * @throws NotFoundException If the shift does not exist
     * @throws ForbiddenException If the user does not own the shift
     * @throws BadRequestException If the shift is already completed
     */
    private fun requireOwnActiveShift(shiftId: Long, auth: AuthenticatedUser): AgeVerificationShift {
        val orgId = auth.requireOrganizationId()
        val shift = shiftRepository.findByIdAndOrganizationId(shiftId, orgId)
            ?: throw NotFoundException("Skift ikke funnet")
        if (shift.user.id != auth.userId) {
            throw ForbiddenException("Du kan bare endre ditt eget skift")
        }
        if (shift.status != ShiftStatus.ACTIVE) {
            throw BadRequestException("Skiftet er allerede avsluttet")
        }
        return shift
    }

    /**
     * Retrieves a user by ID.
     *
     * @param userId The user ID
     * @return The user entity
     * @throws NotFoundException If the user does not exist
     */
    private fun requireUser(userId: Long): User {
        return userRepository.findById(userId)
            .orElseThrow { NotFoundException("Bruker ikke funnet") }
    }

    /**
     * Converts a shift entity into a detailed response including linked deviations.
     *
     * @param shift The shift entity
     * @return Detailed shift response
     */
    private fun toShiftDetail(shift: AgeVerificationShift): ShiftDetailResponse {
        val deviations = deviationRepository.findAllByAgeVerificationShiftId(shift.id)
        return ShiftDetailResponse(
            shift = shift.toResponse(deviations.size),
            deviations = deviations.map { it.toShiftDeviationResponse() },
        )
    }
}

/**
 * Maps an [AgeVerificationShift] entity to a [ShiftResponse].
 *
 * @param deviationCount Number of deviations linked to the shift
 * @return Shift response DTO
 */
private fun AgeVerificationShift.toResponse(deviationCount: Int) = ShiftResponse(
    id = id,
    organizationId = organizationId,
    userId = user.id,
    userName = user.fullName,
    shiftDate = shiftDate.toString(),
    startedAt = startedAt.toString(),
    endedAt = endedAt?.toString(),
    idsCheckedCount = idsCheckedCount,
    signedOff = signedOff,
    signedOffAt = signedOffAt?.toString(),
    status = status,
    deviationCount = deviationCount,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString(),
)

/**
 * Maps an [AlcoholDeviation] entity to a [ShiftDeviationResponse].
 *
 * @return Shift deviation response DTO
 */
private fun AlcoholDeviation.toShiftDeviationResponse() = ShiftDeviationResponse(
    id = id,
    deviationType = deviationType,
    description = description,
    reportedAt = reportedAt.toString(),
)