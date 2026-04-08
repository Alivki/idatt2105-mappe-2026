package com.iksystem.alcohol.dashboard

import com.iksystem.alcohol.ageverification.model.ShiftStatus
import com.iksystem.alcohol.ageverification.repository.AgeVerificationShiftRepository
import com.iksystem.alcohol.alcoholpolicy.model.AgeCheckLimit
import com.iksystem.alcohol.alcoholpolicy.model.AlcoholPolicy
import com.iksystem.alcohol.alcoholpolicy.repository.AlcoholPolicyRepository
import com.iksystem.alcohol.deviation.model.AlcoholDeviation
import com.iksystem.alcohol.deviation.model.AlcoholDeviationStatus
import com.iksystem.alcohol.deviation.model.AlcoholDeviationType
import com.iksystem.alcohol.deviation.model.AlcoholReportSource
import com.iksystem.alcohol.deviation.repository.AlcoholDeviationRepository
import com.iksystem.alcohol.penaltypoints.repository.PenaltyPointRepository
import com.iksystem.common.security.AuthenticatedUser
import com.iksystem.common.user.model.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class AlcoholDashboardServiceTest {

    private lateinit var deviationRepository: AlcoholDeviationRepository
    private lateinit var penaltyPointRepository: PenaltyPointRepository
    private lateinit var shiftRepository: AgeVerificationShiftRepository
    private lateinit var policyRepository: AlcoholPolicyRepository
    private lateinit var service: AlcoholDashboardService

    @BeforeEach
    fun setUp() {
        deviationRepository = mock(AlcoholDeviationRepository::class.java)
        penaltyPointRepository = mock(PenaltyPointRepository::class.java)
        shiftRepository = mock(AgeVerificationShiftRepository::class.java)
        policyRepository = mock(AlcoholPolicyRepository::class.java)

        service = AlcoholDashboardService(
            deviationRepository,
            penaltyPointRepository,
            shiftRepository,
            policyRepository
        )
    }

    @Test
    fun `getStats aggregates all sections with average and expiring soon policy`() {
        val auth = auth()
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val zone = ZoneId.systemDefault()
        val since = Instant.now().minus(30, ChronoUnit.DAYS)

        val recentDeviations = listOf(
            deviation(
                id = 1L,
                type = AlcoholDeviationType.FALSK_LEGITIMASJON,
                status = AlcoholDeviationStatus.OPEN,
                reportedAt = yesterday.atStartOfDay(zone).toInstant()
            ),
            deviation(
                id = 2L,
                type = AlcoholDeviationType.FALSK_LEGITIMASJON,
                status = AlcoholDeviationStatus.UNDER_TREATMENT,
                reportedAt = today.atStartOfDay(zone).toInstant()
            ),
            deviation(
                id = 3L,
                type = AlcoholDeviationType.NEKTET_VISE_LEGITIMASJON,
                status = AlcoholDeviationStatus.CLOSED,
                reportedAt = today.atStartOfDay(zone).plusHours(2).toInstant()
            ),
        )

        `when`(deviationRepository.countByOrganizationIdAndStatus(1L, AlcoholDeviationStatus.OPEN)).thenReturn(1L)
        `when`(deviationRepository.countByOrganizationIdAndStatus(1L, AlcoholDeviationStatus.UNDER_TREATMENT)).thenReturn(1L)
        `when`(deviationRepository.countByOrganizationIdAndStatus(1L, AlcoholDeviationStatus.CLOSED)).thenReturn(1L)
        `when`(deviationRepository.findRecentByOrganizationId(1L, since)).thenReturn(recentDeviations)

        `when`(penaltyPointRepository.sumPointsByOrganizationId(1L)).thenReturn(6)
        `when`(penaltyPointRepository.findAllByOrganizationIdOrderByCreatedAtDesc(1L)).thenReturn(emptyList())

        `when`(shiftRepository.countByOrganizationIdAndStatus(1L, ShiftStatus.COMPLETED)).thenReturn(4L)
        `when`(shiftRepository.sumIdsCheckedByOrganizationId(1L)).thenReturn(20L)

        `when`(policyRepository.findByOrganizationId(1L)).thenReturn(
            AlcoholPolicy(
                id = 10L,
                organizationId = 1L,
                bevillingNumber = "BEV-77",
                bevillingValidUntil = today.plusDays(10),
                ageCheckLimit = AgeCheckLimit.UNDER_25,
                acceptedIdTypes = "PASS,FORERKORT"
            )
        )

        val result = service.getStats(auth)

        assertEquals(1L, result.deviations.open)
        assertEquals(1L, result.deviations.underTreatment)
        assertEquals(1L, result.deviations.closed)

        assertEquals(6, result.penaltyPoints.totalPoints)
        assertEquals(12, result.penaltyPoints.maxPoints)
        assertTrue(result.penaltyPoints.entries.isEmpty())

        assertEquals(4L, result.ageVerification.totalShifts)
        assertEquals(20L, result.ageVerification.totalIdsChecked)
        assertEquals(3L, result.ageVerification.totalDeviations)
        assertEquals(5.0, result.ageVerification.avgIdsPerShift)

        assertTrue(result.policyStatus.exists)
        assertEquals("BEV-77", result.policyStatus.bevillingNumber)
        assertEquals(today.plusDays(10).toString(), result.policyStatus.bevillingValidUntil)
        assertTrue(result.policyStatus.isExpiringSoon)

        verify(policyRepository).findByOrganizationId(1L)
    }

    @Test
    fun `getStats returns zero average and missing policy when nothing exists`() {
        val auth = auth()
        val since = Instant.now().minus(30, ChronoUnit.DAYS)

        `when`(deviationRepository.countByOrganizationIdAndStatus(1L, AlcoholDeviationStatus.OPEN)).thenReturn(0L)
        `when`(deviationRepository.countByOrganizationIdAndStatus(1L, AlcoholDeviationStatus.UNDER_TREATMENT)).thenReturn(0L)
        `when`(deviationRepository.countByOrganizationIdAndStatus(1L, AlcoholDeviationStatus.CLOSED)).thenReturn(0L)
        `when`(deviationRepository.findRecentByOrganizationId(1L, since)).thenReturn(emptyList())

        `when`(penaltyPointRepository.sumPointsByOrganizationId(1L)).thenReturn(0)
        `when`(penaltyPointRepository.findAllByOrganizationIdOrderByCreatedAtDesc(1L)).thenReturn(emptyList())

        `when`(shiftRepository.countByOrganizationIdAndStatus(1L, ShiftStatus.COMPLETED)).thenReturn(0L)
        `when`(shiftRepository.sumIdsCheckedByOrganizationId(1L)).thenReturn(0L)

        `when`(policyRepository.findByOrganizationId(1L)).thenReturn(null)

        val result = service.getStats(auth)

        assertEquals(0L, result.deviations.open)
        assertEquals(0L, result.deviations.underTreatment)
        assertEquals(0L, result.deviations.closed)
        assertTrue(result.deviations.byType.isEmpty())
        assertTrue(result.deviations.trend.isEmpty())

        assertEquals(0, result.penaltyPoints.totalPoints)
        assertEquals(12, result.penaltyPoints.maxPoints)
        assertTrue(result.penaltyPoints.entries.isEmpty())

        assertEquals(0L, result.ageVerification.totalShifts)
        assertEquals(0L, result.ageVerification.totalIdsChecked)
        assertEquals(0L, result.ageVerification.totalDeviations)
        assertEquals(0.0, result.ageVerification.avgIdsPerShift)

        assertFalse(result.policyStatus.exists)
        assertNull(result.policyStatus.bevillingNumber)
        assertNull(result.policyStatus.bevillingValidUntil)
        assertFalse(result.policyStatus.isExpiringSoon)
    }

    @Test
    fun `getStats marks policy as not expiring soon when expiry is far away`() {
        val auth = auth()
        val since = Instant.now().minus(30, ChronoUnit.DAYS)

        `when`(deviationRepository.countByOrganizationIdAndStatus(1L, AlcoholDeviationStatus.OPEN)).thenReturn(0L)
        `when`(deviationRepository.countByOrganizationIdAndStatus(1L, AlcoholDeviationStatus.UNDER_TREATMENT)).thenReturn(0L)
        `when`(deviationRepository.countByOrganizationIdAndStatus(1L, AlcoholDeviationStatus.CLOSED)).thenReturn(0L)
        `when`(deviationRepository.findRecentByOrganizationId(1L, since)).thenReturn(emptyList())

        `when`(penaltyPointRepository.sumPointsByOrganizationId(1L)).thenReturn(0)
        `when`(penaltyPointRepository.findAllByOrganizationIdOrderByCreatedAtDesc(1L)).thenReturn(emptyList())

        `when`(shiftRepository.countByOrganizationIdAndStatus(1L, ShiftStatus.COMPLETED)).thenReturn(1L)
        `when`(shiftRepository.sumIdsCheckedByOrganizationId(1L)).thenReturn(2L)

        `when`(policyRepository.findByOrganizationId(1L)).thenReturn(
            AlcoholPolicy(
                id = 2L,
                organizationId = 1L,
                bevillingNumber = "FUTURE",
                bevillingValidUntil = LocalDate.now().plusDays(40),
                ageCheckLimit = AgeCheckLimit.UNDER_25
            )
        )

        val result = service.getStats(auth)

        assertTrue(result.policyStatus.exists)
        assertEquals("FUTURE", result.policyStatus.bevillingNumber)
        assertFalse(result.policyStatus.isExpiringSoon)
    }

    private fun auth() = AuthenticatedUser(
        10L,
        1L,
        "MANAGER"
    )

    private fun user() = User(
        id = 10L,
        email = "user@example.com",
        password = "hashed",
        fullName = "Test User",
        phoneNumber = "12345678"
    )

    private fun deviation(
        id: Long,
        type: AlcoholDeviationType,
        status: AlcoholDeviationStatus,
        reportedAt: Instant
    ) = AlcoholDeviation(
        id = id,
        organizationId = 1L,
        reportedAt = reportedAt,
        reportedByUser = user(),
        reportSource = AlcoholReportSource.EGENRAPPORT,
        deviationType = type,
        description = "Deviation $id",
        status = status,
        createdAt = reportedAt,
        updatedAt = reportedAt
    )
}