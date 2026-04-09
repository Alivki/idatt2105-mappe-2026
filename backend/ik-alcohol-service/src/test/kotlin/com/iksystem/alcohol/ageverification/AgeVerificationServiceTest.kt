package com.iksystem.alcohol.ageverification

import com.iksystem.alcohol.ageverification.dto.CreateShiftDeviationRequest
import com.iksystem.alcohol.ageverification.dto.UpdateIdCheckCountRequest
import com.iksystem.alcohol.ageverification.model.AgeVerificationShift
import com.iksystem.alcohol.ageverification.model.ShiftStatus
import com.iksystem.alcohol.ageverification.repository.AgeVerificationShiftRepository
import com.iksystem.alcohol.ageverification.repository.DailySummaryProjection
import com.iksystem.alcohol.ageverification.service.AgeVerificationService
import com.iksystem.alcohol.deviation.model.AlcoholDeviation
import com.iksystem.alcohol.deviation.model.AlcoholDeviationType
import com.iksystem.alcohol.deviation.model.AlcoholReportSource
import com.iksystem.alcohol.deviation.repository.AlcoholDeviationRepository
import com.iksystem.common.exception.BadRequestException
import com.iksystem.common.exception.ConflictException
import com.iksystem.common.exception.ForbiddenException
import com.iksystem.common.security.AuthenticatedUser
import com.iksystem.common.user.model.User
import com.iksystem.common.user.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.Instant
import java.time.LocalDate
import java.util.Optional

class AgeVerificationServiceTest {

    private lateinit var shiftRepository: AgeVerificationShiftRepository
    private lateinit var deviationRepository: AlcoholDeviationRepository
    private lateinit var userRepository: UserRepository
    private lateinit var service: AgeVerificationService

    @BeforeEach
    fun setUp() {
        shiftRepository = mock(AgeVerificationShiftRepository::class.java)
        deviationRepository = mock(AlcoholDeviationRepository::class.java)
        userRepository = mock(UserRepository::class.java)
        service = AgeVerificationService(shiftRepository, deviationRepository, userRepository)
    }

    @Test
    fun `startShift saves new shift when no active or completed shift exists`() {
        val auth = authUser()
        val user = user()
        val today = LocalDate.now()

        `when`(userRepository.findById(10L)).thenReturn(Optional.of(user))
        `when`(
            shiftRepository.findByUserIdAndOrganizationIdAndStatus(10L, 1L, ShiftStatus.ACTIVE)
        ).thenReturn(null)
        `when`(
            shiftRepository.findFirstByUserIdAndOrganizationIdAndShiftDateOrderByStartedAtDesc(
                10L,
                1L,
                today
            )
        ).thenReturn(null)

        `when`(shiftRepository.save(any(AgeVerificationShift::class.java))).thenAnswer { invocation ->
            val shift = invocation.arguments[0] as AgeVerificationShift
            shift.copy(id = 99L)
        }

        val result = service.startShift(auth)

        assertEquals(99L, result.id)
        assertEquals(1L, result.organizationId)
        assertEquals(10L, result.userId)
        assertEquals("Test User", result.userName)
        assertEquals(0, result.idsCheckedCount)
        assertEquals(ShiftStatus.ACTIVE, result.status)

        val captor = ArgumentCaptor.forClass(AgeVerificationShift::class.java)
        verify(shiftRepository).save(captor.capture())
        assertEquals(1L, captor.value.organizationId)
        assertEquals(10L, captor.value.user.id)
        assertEquals(today, captor.value.shiftDate)
    }

    @Test
    fun `startShift throws conflict when active shift already exists`() {
        val auth = authUser()
        val existing = shift(id = 5L, status = ShiftStatus.ACTIVE)

        `when`(userRepository.findById(10L)).thenReturn(Optional.of(user()))
        `when`(
            shiftRepository.findByUserIdAndOrganizationIdAndStatus(10L, 1L, ShiftStatus.ACTIVE)
        ).thenReturn(existing)

        assertThrows<ConflictException> {
            service.startShift(auth)
        }

        verify(shiftRepository, never()).save(any(AgeVerificationShift::class.java))
    }

    @Test
    fun `startShift throws bad request when todays shift is already completed`() {
        val auth = authUser()
        val today = LocalDate.now()

        `when`(userRepository.findById(10L)).thenReturn(Optional.of(user()))
        `when`(
            shiftRepository.findByUserIdAndOrganizationIdAndStatus(10L, 1L, ShiftStatus.ACTIVE)
        ).thenReturn(null)
        `when`(
            shiftRepository.findFirstByUserIdAndOrganizationIdAndShiftDateOrderByStartedAtDesc(
                10L,
                1L,
                today
            )
        ).thenReturn(shift(id = 2L, status = ShiftStatus.COMPLETED, shiftDate = today))

        assertThrows<BadRequestException> {
            service.startShift(auth)
        }
    }

    @Test
    fun `getActiveShift falls back to todays completed shift when no active shift exists`() {
        val auth = authUser()
        val today = LocalDate.now()
        val completedShift = shift(id = 7L, status = ShiftStatus.COMPLETED, shiftDate = today)
        val deviation = deviation(ageVerificationShiftId = 7L)

        `when`(
            shiftRepository.findByUserIdAndOrganizationIdAndStatus(10L, 1L, ShiftStatus.ACTIVE)
        ).thenReturn(null)
        `when`(
            shiftRepository.findFirstByUserIdAndOrganizationIdAndShiftDateOrderByStartedAtDesc(
                10L,
                1L,
                today
            )
        ).thenReturn(completedShift)
        `when`(deviationRepository.findAllByAgeVerificationShiftId(7L)).thenReturn(listOf(deviation))

        val result = service.getActiveShift(auth)

        assertNotNull(result)
        assertEquals(7L, result!!.shift.id)
        assertEquals(1, result.deviations.size)
        assertEquals(1, result.shift.deviationCount)
        assertEquals(ShiftStatus.COMPLETED, result.shift.status)
    }

    @Test
    fun `updateIdCheckCount updates active own shift`() {
        val auth = authUser()
        val activeShift = shift(id = 3L, status = ShiftStatus.ACTIVE)

        `when`(shiftRepository.findByIdAndOrganizationId(3L, 1L)).thenReturn(activeShift)
        `when`(shiftRepository.save(any(AgeVerificationShift::class.java))).thenAnswer { it.arguments[0] }
        `when`(deviationRepository.findAllByAgeVerificationShiftId(3L)).thenReturn(emptyList())

        val result = service.updateIdCheckCount(3L, UpdateIdCheckCountRequest(12), auth)

        assertEquals(12, result.idsCheckedCount)
        assertEquals(ShiftStatus.ACTIVE, result.status)
    }

    @Test
    fun `createShiftDeviation uses trimmed description when provided`() {
        val auth = authUser()
        val activeShift = shift(id = 11L, status = ShiftStatus.ACTIVE)
        val request = CreateShiftDeviationRequest(
            deviationType = AlcoholDeviationType.FALSK_LEGITIMASJON,
            description = "  customer refused details  "
        )

        `when`(shiftRepository.findByIdAndOrganizationId(11L, 1L)).thenReturn(activeShift)
        `when`(userRepository.findById(10L)).thenReturn(Optional.of(user()))
        `when`(deviationRepository.save(any(AlcoholDeviation::class.java))).thenAnswer { invocation ->
            val dev = invocation.arguments[0] as AlcoholDeviation
            dev.copy(id = 101L)
        }

        val result = service.createShiftDeviation(11L, request, auth)

        assertEquals(101L, result.id)
        assertEquals(AlcoholDeviationType.FALSK_LEGITIMASJON, result.deviationType)
        assertEquals("customer refused details", result.description)
    }

    @Test
    fun `createShiftDeviation falls back to deviation type when description is blank`() {
        val auth = authUser()
        val activeShift = shift(id = 11L, status = ShiftStatus.ACTIVE)
        val request = CreateShiftDeviationRequest(
            deviationType = AlcoholDeviationType.FALSK_LEGITIMASJON,
            description = "   "
        )

        `when`(shiftRepository.findByIdAndOrganizationId(11L, 1L)).thenReturn(activeShift)
        `when`(userRepository.findById(10L)).thenReturn(Optional.of(user()))
        `when`(deviationRepository.save(any(AlcoholDeviation::class.java))).thenAnswer { invocation ->
            val dev = invocation.arguments[0] as AlcoholDeviation
            dev.copy(id = 101L)
        }

        val result = service.createShiftDeviation(11L, request, auth)

        assertEquals("Falsk legitimasjon", result.description)
    }

    @Test
    fun `endShift signs off and completes shift`() {
        val auth = authUser()
        val activeShift = shift(id = 4L, status = ShiftStatus.ACTIVE, signedOff = false)

        `when`(shiftRepository.findByIdAndOrganizationId(4L, 1L)).thenReturn(activeShift)
        `when`(shiftRepository.save(any(AgeVerificationShift::class.java))).thenAnswer { it.arguments[0] }
        `when`(deviationRepository.findAllByAgeVerificationShiftId(4L)).thenReturn(emptyList())

        val result = service.endShift(4L, auth)

        assertEquals(ShiftStatus.COMPLETED, result.status)
        assertTrue(result.signedOff)
        assertNotNull(result.endedAt)
        assertNotNull(result.signedOffAt)
    }

    @Test
    fun `reopenShift throws forbidden for another users shift`() {
        val auth = authUser()
        val otherUsersShift = shift(id = 8L, user = user(id = 999L), status = ShiftStatus.COMPLETED)

        `when`(shiftRepository.findByIdAndOrganizationId(8L, 1L)).thenReturn(otherUsersShift)

        assertThrows<ForbiddenException> {
            service.reopenShift(8L, auth)
        }
    }

    @Test
    fun `reopenShift reopens todays completed shift`() {
        val auth = authUser()
        val completedShift = shift(id = 8L, status = ShiftStatus.COMPLETED)

        `when`(shiftRepository.findByIdAndOrganizationId(8L, 1L)).thenReturn(completedShift)
        `when`(
            shiftRepository.findByUserIdAndOrganizationIdAndStatus(10L, 1L, ShiftStatus.ACTIVE)
        ).thenReturn(null)
        `when`(shiftRepository.save(any(AgeVerificationShift::class.java))).thenAnswer { it.arguments[0] }
        `when`(deviationRepository.findAllByAgeVerificationShiftId(8L)).thenReturn(emptyList())

        val result = service.reopenShift(8L, auth)

        assertEquals(ShiftStatus.ACTIVE, result.status)
        assertFalse(result.signedOff)
        assertNull(result.endedAt)
        assertNull(result.signedOffAt)
    }

    @Test
    fun `getShiftById allows manager access to another users shift`() {
        val manager = AuthenticatedUser(10L, 1L, "MANAGER")
        val someoneElsesShift = shift(id = 15L, user = user(id = 77L))
        val dev = deviation(ageVerificationShiftId = 15L)

        `when`(shiftRepository.findByIdAndOrganizationId(15L, 1L)).thenReturn(someoneElsesShift)
        `when`(deviationRepository.findAllByAgeVerificationShiftId(15L)).thenReturn(listOf(dev))

        val result = service.getShiftById(15L, manager)

        assertEquals(15L, result.shift.id)
        assertEquals(1, result.deviations.size)
    }

    @Test
    fun `getDailySummaries combines repository summaries with deviation counts`() {
        val auth = authUser()
        val today = LocalDate.now()
        val yday = today.minusDays(1)

        val shifts = listOf(
            shift(id = 1L, shiftDate = today, idsCheckedCount = 5, status = ShiftStatus.COMPLETED),
            shift(id = 2L, shiftDate = today, idsCheckedCount = 3, status = ShiftStatus.COMPLETED),
            shift(id = 3L, shiftDate = yday, idsCheckedCount = 7, status = ShiftStatus.COMPLETED),
        )

        val summaries = listOf(
            projection(today, 2L, 8L),
            projection(yday, 1L, 7L),
        )

        val deviations = listOf(
            deviation(id = 1L, ageVerificationShiftId = 1L),
            deviation(id = 2L, ageVerificationShiftId = 1L),
            deviation(id = 3L, ageVerificationShiftId = 3L),
        )

        `when`(shiftRepository.findDailySummaries(1L, yday, today)).thenReturn(summaries)
        `when`(shiftRepository.findByOrganizationIdAndShiftDateBetweenOrderByShiftDateDesc(1L, yday, today))
            .thenReturn(shifts)
        `when`(deviationRepository.findAllByAgeVerificationShiftIdIn(listOf(1L, 2L, 3L)))
            .thenReturn(deviations)

        val result = service.getDailySummaries(yday, today, auth)

        assertEquals(2, result.size)
        assertEquals(today.toString(), result[0].date)
        assertEquals(2L, result[0].shiftCount)
        assertEquals(8L, result[0].totalIdsChecked)
        assertEquals(2L, result[0].totalDeviations)
        assertEquals(1L, result[1].totalDeviations)
    }

    @Test
    fun `getStats calculates totals and average ids per shift`() {
        val auth = authUser()
        val from = LocalDate.now().minusDays(1)
        val to = LocalDate.now()

        val summaries = listOf(
            projection(to, 2L, 8L),
            projection(from, 1L, 7L),
        )
        val shifts = listOf(
            shift(id = 1L, shiftDate = to, idsCheckedCount = 5, status = ShiftStatus.COMPLETED),
            shift(id = 2L, shiftDate = to, idsCheckedCount = 3, status = ShiftStatus.COMPLETED),
            shift(id = 3L, shiftDate = from, idsCheckedCount = 7, status = ShiftStatus.COMPLETED),
        )
        val deviations = listOf(
            deviation(id = 1L, ageVerificationShiftId = 1L),
            deviation(id = 2L, ageVerificationShiftId = 1L),
            deviation(id = 3L, ageVerificationShiftId = 3L),
        )

        `when`(shiftRepository.findDailySummaries(1L, from, to)).thenReturn(summaries)
        `when`(shiftRepository.findByOrganizationIdAndShiftDateBetweenOrderByShiftDateDesc(1L, from, to))
            .thenReturn(shifts)
        `when`(deviationRepository.countByAgeVerificationShiftIdIn(listOf(1L, 2L, 3L))).thenReturn(3L)
        `when`(deviationRepository.findAllByAgeVerificationShiftIdIn(listOf(1L, 2L, 3L))).thenReturn(deviations)

        val result = service.getStats(from, to, auth)

        assertEquals(from.toString(), result.periodFrom)
        assertEquals(to.toString(), result.periodTo)
        assertEquals(3L, result.totalShifts)
        assertEquals(15L, result.totalIdsChecked)
        assertEquals(3L, result.totalDeviations)
        assertEquals(5.0, result.avgIdsPerShift)
        assertEquals(2, result.dailySummaries.size)
    }

    private fun authUser() = AuthenticatedUser(
        10L,
        1L,
        "EMPLOYEE"
    )

    private fun user(
        id: Long = 10L,
        fullName: String = "Test User"
    ) = User(
        id = id,
        email = "user@example.com",
        password = "hashed",
        fullName = fullName,
        phoneNumber = "12345678"
    )

    private fun shift(
        id: Long = 1L,
        organizationId: Long = 1L,
        user: User = user(),
        shiftDate: LocalDate = LocalDate.now(),
        startedAt: Instant = Instant.parse("2026-01-01T10:00:00Z"),
        endedAt: Instant? = null,
        idsCheckedCount: Int = 0,
        signedOff: Boolean = false,
        signedOffAt: Instant? = null,
        status: ShiftStatus = ShiftStatus.ACTIVE,
        createdAt: Instant = Instant.parse("2026-01-01T10:00:00Z"),
        updatedAt: Instant = Instant.parse("2026-01-01T10:00:00Z"),
    ) = AgeVerificationShift(
        id = id,
        organizationId = organizationId,
        user = user,
        shiftDate = shiftDate,
        startedAt = startedAt,
        endedAt = endedAt,
        idsCheckedCount = idsCheckedCount,
        signedOff = signedOff,
        signedOffAt = signedOffAt,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    private fun deviation(
        id: Long = 1L,
        ageVerificationShiftId: Long? = 1L,
        type: AlcoholDeviationType = AlcoholDeviationType.FALSK_LEGITIMASJON,
    ) = AlcoholDeviation(
        id = id,
        organizationId = 1L,
        reportedByUser = user(),
        reportSource = AlcoholReportSource.EGENRAPPORT,
        deviationType = type,
        description = "Deviation",
        ageVerificationShiftId = ageVerificationShiftId,
        reportedAt = Instant.parse("2026-01-01T10:05:00Z"),
        createdAt = Instant.parse("2026-01-01T10:05:00Z"),
        updatedAt = Instant.parse("2026-01-01T10:05:00Z"),
    )

    private fun projection(date: LocalDate, shiftCount: Long, totalIdsChecked: Long): DailySummaryProjection =
        object : DailySummaryProjection {
            override val shiftDate: LocalDate = date
            override val shiftCount: Long = shiftCount
            override val totalIdsChecked: Long = totalIdsChecked
        }
}