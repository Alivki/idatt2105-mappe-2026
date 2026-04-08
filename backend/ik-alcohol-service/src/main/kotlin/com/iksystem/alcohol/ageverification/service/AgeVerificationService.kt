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

@Service
class AgeVerificationService(
    private val shiftRepository: AgeVerificationShiftRepository,
    private val deviationRepository: AlcoholDeviationRepository,
    private val userRepository: UserRepository,
) {


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

    // ── Employee: own shift history ──

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

    // ── Manager: aggregated views ──

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

    // ── Helpers ──

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

    private fun requireUser(userId: Long): User {
        return userRepository.findById(userId)
            .orElseThrow { NotFoundException("Bruker ikke funnet") }
    }

    private fun toShiftDetail(shift: AgeVerificationShift): ShiftDetailResponse {
        val deviations = deviationRepository.findAllByAgeVerificationShiftId(shift.id)
        return ShiftDetailResponse(
            shift = shift.toResponse(deviations.size),
            deviations = deviations.map { it.toShiftDeviationResponse() },
        )
    }
}

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

private fun AlcoholDeviation.toShiftDeviationResponse() = ShiftDeviationResponse(
    id = id,
    deviationType = deviationType,
    description = description,
    reportedAt = reportedAt.toString(),
)
