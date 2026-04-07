package com.iksystem.alcohol.dashboard

import com.iksystem.alcohol.ageverification.model.ShiftStatus
import com.iksystem.alcohol.ageverification.repository.AgeVerificationShiftRepository
import com.iksystem.alcohol.alcoholpolicy.repository.AlcoholPolicyRepository
import com.iksystem.alcohol.deviation.model.AlcoholDeviationStatus
import com.iksystem.alcohol.deviation.repository.AlcoholDeviationRepository
import com.iksystem.alcohol.penaltypoints.repository.PenaltyPointRepository
import com.iksystem.common.security.AuthenticatedUser
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Service
class AlcoholDashboardService(
    private val deviationRepository: AlcoholDeviationRepository,
    private val penaltyPointRepository: PenaltyPointRepository,
    private val shiftRepository: AgeVerificationShiftRepository,
    private val policyRepository: AlcoholPolicyRepository,
) {

    @Transactional(readOnly = true)
    fun getStats(auth: AuthenticatedUser): AlcoholDashboardStatsResponse {
        val orgId = auth.requireOrganizationId()
        return AlcoholDashboardStatsResponse(
            deviations = getDeviationStats(orgId),
            penaltyPoints = getPenaltyPointStats(orgId),
            ageVerification = getAgeVerificationStats(orgId),
            policyStatus = getPolicyStatus(orgId),
        )
    }

    private fun getDeviationStats(orgId: Long): AlcoholDeviationStats {
        val open = deviationRepository.countByOrganizationIdAndStatus(orgId, AlcoholDeviationStatus.OPEN)
        val underTreatment = deviationRepository.countByOrganizationIdAndStatus(orgId, AlcoholDeviationStatus.UNDER_TREATMENT)
        val closed = deviationRepository.countByOrganizationIdAndStatus(orgId, AlcoholDeviationStatus.CLOSED)

        val since = Instant.now().minus(30, ChronoUnit.DAYS)
        val recent = deviationRepository.findRecentByOrganizationId(orgId, since)

        val byType = recent.groupBy { it.deviationType.name }.mapValues { it.value.size.toLong() }

        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val zone = ZoneId.systemDefault()
        val trend = recent
            .groupBy { it.reportedAt.atZone(zone).toLocalDate() }
            .map { (date, items) -> AlcoholDailyCount(date.format(formatter), items.size.toLong()) }
            .sortedBy { it.date }

        return AlcoholDeviationStats(
            open = open, underTreatment = underTreatment, closed = closed,
            byType = byType, trend = trend,
        )
    }

    private fun getPenaltyPointStats(orgId: Long): PenaltyPointStats {
        val total = penaltyPointRepository.sumPointsByOrganizationId(orgId)
        val entries = penaltyPointRepository.findAllByOrganizationIdOrderByCreatedAtDesc(orgId)

        return PenaltyPointStats(
            totalPoints = total,
            maxPoints = 12,
            entries = entries.map {
                PenaltyEntry(
                    id = it.id,
                    violationType = it.violationType.name,
                    points = it.points,
                    description = it.description,
                    createdAt = it.createdAt.toString(),
                )
            },
        )
    }

    private fun getAgeVerificationStats(orgId: Long): AgeVerificationStats {
        val completed = shiftRepository.countByOrganizationIdAndStatus(orgId, ShiftStatus.COMPLETED)
        val totalIds = shiftRepository.sumIdsCheckedByOrganizationId(orgId)
        val totalDeviations = deviationRepository.countByOrganizationIdAndStatus(orgId, AlcoholDeviationStatus.OPEN) +
            deviationRepository.countByOrganizationIdAndStatus(orgId, AlcoholDeviationStatus.UNDER_TREATMENT) +
            deviationRepository.countByOrganizationIdAndStatus(orgId, AlcoholDeviationStatus.CLOSED)
        val avg = if (completed > 0) totalIds.toDouble() / completed else 0.0

        return AgeVerificationStats(
            totalShifts = completed,
            totalIdsChecked = totalIds,
            totalDeviations = totalDeviations,
            avgIdsPerShift = avg,
        )
    }

    private fun getPolicyStatus(orgId: Long): PolicyStatus {
        val policy = policyRepository.findByOrganizationId(orgId)
        val today = LocalDate.now()
        val expiringSoon = policy?.bevillingValidUntil?.let {
            it.isBefore(today.plusDays(30)) && !it.isBefore(today)
        } ?: false

        return PolicyStatus(
            exists = policy != null,
            bevillingNumber = policy?.bevillingNumber,
            bevillingValidUntil = policy?.bevillingValidUntil?.toString(),
            isExpiringSoon = expiringSoon,
        )
    }
}
