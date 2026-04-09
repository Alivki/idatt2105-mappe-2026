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

/**
 * Service responsible for aggregating dashboard statistics for the IK-Alkohol module.
 *
 * Combines data from deviations, penalty points, age verification shifts, and the
 * alcohol policy into a single [AlcoholDashboardStatsResponse] for the authenticated
 * user's organization.
 *
 * @property deviationRepository Repository for querying alcohol deviation records.
 * @property penaltyPointRepository Repository for querying penalty point records.
 * @property shiftRepository Repository for querying age verification shift records.
 * @property policyRepository Repository for querying alcohol policy records.
 */
@Service
class AlcoholDashboardService(
    private val deviationRepository: AlcoholDeviationRepository,
    private val penaltyPointRepository: PenaltyPointRepository,
    private val shiftRepository: AgeVerificationShiftRepository,
    private val policyRepository: AlcoholPolicyRepository,
) {

    /**
     * Retrieves aggregated dashboard statistics for the authenticated user's organization.
     *
     * Delegates to private helpers for each statistical category and combines the results.
     *
     * @param auth The authenticated user, used to resolve the organization ID.
     * @return A fully populated [AlcoholDashboardStatsResponse].
     */
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

    /**
     * Computes deviation statistics for the given organization.
     *
     * Counts deviations by status (open, under treatment, closed) and computes a 30-day
     * trend by grouping recent deviations by their reported date. Also breaks down
     * recent deviations by type.
     *
     * @param orgId The organization ID to scope the query.
     * @return An [AlcoholDeviationStats] instance with counts, type breakdown, and trend data.
     */
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

    /**
     * Computes penalty point statistics for the given organization.
     *
     * Retrieves the total accumulated points and all individual penalty entries,
     * ordered by most recent first. The maximum allowed points threshold is fixed at 12.
     *
     * @param orgId The organization ID to scope the query.
     * @return A [PenaltyPointStats] instance with total points, the maximum threshold, and all entries.
     */
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

    /**
     * Computes age verification statistics for the given organization.
     *
     * Counts completed shifts, sums the total number of IDs checked, counts all deviations
     * regardless of status, and calculates the average number of ID checks per shift.
     * The average defaults to `0.0` if no completed shifts exist.
     *
     * @param orgId The organization ID to scope the query.
     * @return An [AgeVerificationStats] instance with shift, ID check, and deviation counts.
     */
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

    /**
     * Retrieves the alcohol policy status for the given organization.
     *
     * A policy is considered expiring soon if its bevilling expiry date falls within
     * the next 30 days and has not yet passed. If no policy exists, [PolicyStatus.exists]
     * is `false` and all other fields are `null` or `false`.
     *
     * @param orgId The organization ID to scope the query.
     * @return A [PolicyStatus] reflecting the existence and expiry state of the policy.
     */
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