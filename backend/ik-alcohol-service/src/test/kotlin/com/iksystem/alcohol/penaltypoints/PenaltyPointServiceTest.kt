package com.iksystem.alcohol.penaltypoints.service

import com.iksystem.alcohol.deviation.model.AlcoholDeviation
import com.iksystem.alcohol.deviation.model.AlcoholDeviationType
import com.iksystem.alcohol.deviation.model.AlcoholReportSource
import com.iksystem.alcohol.penaltypoints.dto.CreatePenaltyPointRequest
import com.iksystem.alcohol.penaltypoints.model.PenaltyPoint
import com.iksystem.alcohol.penaltypoints.repository.PenaltyPointRepository
import com.iksystem.common.exception.NotFoundException
import com.iksystem.common.security.AuthenticatedUser
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class PenaltyPointServiceTest {

    private val repository = mockk<PenaltyPointRepository>()
    private val auth = mockk<AuthenticatedUser>()

    private val service = PenaltyPointService(repository)

    private val orgId = 1L

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        every { auth.requireOrganizationId() } returns orgId
    }

    private fun penaltyPointMock(
        id: Long = 1L,
        organizationId: Long = orgId,
        alcoholDeviationId: Long? = null,
        points: Int = 8,
        violationType: AlcoholDeviationType = AlcoholDeviationType.SKJENKING_MINDREAARIGE,
        description: String? = "Some description",
        createdAt: Instant = Instant.parse("2024-01-15T10:00:00Z"),
    ): PenaltyPoint {
        val penaltyPoint = mockk<PenaltyPoint>()
        val deviation = alcoholDeviationId?.let {
            mockk<AlcoholDeviation> {
                every { id } returns it
            }
        }

        every { penaltyPoint.id } returns id
        every { penaltyPoint.organizationId } returns organizationId
        every { penaltyPoint.alcoholDeviation } returns deviation
        every { penaltyPoint.points } returns points
        every { penaltyPoint.violationType } returns violationType
        every { penaltyPoint.description } returns description
        every { penaltyPoint.createdAt } returns createdAt

        return penaltyPoint
    }

    @Test
    fun `getSummary returns summary for organization`() {
        val entry1 = penaltyPointMock(id = 1L, points = 8)
        val entry2 = penaltyPointMock(id = 2L, points = 4)

        every { repository.findAllByOrganizationIdOrderByCreatedAtDesc(orgId) } returns listOf(entry1, entry2)
        every { repository.sumPointsByOrganizationId(orgId) } returns 12

        val result = service.getSummary(auth)

        assertThat(result.organizationId).isEqualTo(orgId)
        assertThat(result.totalPoints).isEqualTo(12)
        assertThat(result.entries).hasSize(2)
        assertThat(result.entries.map { it.id }).containsExactly(1L, 2L)

        verify {
            repository.findAllByOrganizationIdOrderByCreatedAtDesc(orgId)
            repository.sumPointsByOrganizationId(orgId)
        }
    }

    @Test
    fun `getSummary returns empty entries when no penalty points exist`() {
        every { repository.findAllByOrganizationIdOrderByCreatedAtDesc(orgId) } returns emptyList()
        every { repository.sumPointsByOrganizationId(orgId) } returns 0

        val result = service.getSummary(auth)

        assertThat(result.organizationId).isEqualTo(orgId)
        assertThat(result.totalPoints).isZero()
        assertThat(result.entries).isEmpty()
    }

    @Test
    fun `add saves penalty point with trimmed description`() {
        val request = CreatePenaltyPointRequest(
            violationType = AlcoholDeviationType.SKJENKING_MINDREAARIGE,
            description = "  Test description  ",
        )

        val savedSlot = slot<PenaltyPoint>()
        val saved = penaltyPointMock(
            id = 1L,
            points = AlcoholDeviationType.SKJENKING_MINDREAARIGE.penaltyPoints,
            violationType = AlcoholDeviationType.SKJENKING_MINDREAARIGE,
            description = "Test description",
        )

        every { repository.save(capture(savedSlot)) } returns saved

        val result = service.add(request, auth)

        assertThat(savedSlot.captured.organizationId).isEqualTo(orgId)
        assertThat(savedSlot.captured.points).isEqualTo(AlcoholDeviationType.SKJENKING_MINDREAARIGE.penaltyPoints)
        assertThat(savedSlot.captured.violationType).isEqualTo(AlcoholDeviationType.SKJENKING_MINDREAARIGE)
        assertThat(savedSlot.captured.description).isEqualTo("Test description")

        assertThat(result.id).isEqualTo(1L)
        assertThat(result.organizationId).isEqualTo(orgId)
        assertThat(result.points).isEqualTo(AlcoholDeviationType.SKJENKING_MINDREAARIGE.penaltyPoints)
    }

    @Test
    fun `add stores null description when description is blank after trim`() {
        val request = CreatePenaltyPointRequest(
            violationType = AlcoholDeviationType.SKJENKING_MINDREAARIGE,
            description = "   ",
        )

        val savedSlot = slot<PenaltyPoint>()
        val saved = penaltyPointMock(
            description = null,
            points = AlcoholDeviationType.SKJENKING_MINDREAARIGE.penaltyPoints,
            violationType = AlcoholDeviationType.SKJENKING_MINDREAARIGE,
        )

        every { repository.save(capture(savedSlot)) } returns saved

        service.add(request, auth)

        assertThat(savedSlot.captured.description).isNull()
    }

    @Test
    fun `add stores null description when description is null`() {
        val request = CreatePenaltyPointRequest(
            violationType = AlcoholDeviationType.SKJENKING_MINDREAARIGE,
            description = null,
        )

        val savedSlot = slot<PenaltyPoint>()
        val saved = penaltyPointMock(
            description = null,
            points = AlcoholDeviationType.SKJENKING_MINDREAARIGE.penaltyPoints,
            violationType = AlcoholDeviationType.SKJENKING_MINDREAARIGE,
        )

        every { repository.save(capture(savedSlot)) } returns saved

        service.add(request, auth)

        assertThat(savedSlot.captured.description).isNull()
    }

    @Test
    fun `delete removes penalty point when found in same organization`() {
        val entry = penaltyPointMock(id = 1L)

        every { repository.findByIdAndOrganizationId(1L, orgId) } returns entry
        every { repository.delete(entry) } just Runs

        service.delete(1L, auth)

        verify {
            repository.findByIdAndOrganizationId(1L, orgId)
            repository.delete(entry)
        }
    }

    @Test
    fun `delete throws NotFoundException when entry does not exist`() {
        every { repository.findByIdAndOrganizationId(99L, orgId) } returns null

        assertThatThrownBy { service.delete(99L, auth) }
            .isInstanceOf(NotFoundException::class.java)
            .hasMessage("Penalty point entry not found")

        verify { repository.findByIdAndOrganizationId(99L, orgId) }
        verify(exactly = 0) { repository.delete(any()) }
    }

    @Test
    fun `addForDeviation saves penalty point linked to deviation`() {
        val deviation = mockk<AlcoholDeviation>()
        every { deviation.organizationId } returns orgId
        every { deviation.reportSource } returns AlcoholReportSource.SJENKEKONTROLL
        every { deviation.deviationType } returns AlcoholDeviationType.SKJENKING_MINDREAARIGE

        val savedSlot = slot<PenaltyPoint>()
        every { repository.save(capture(savedSlot)) } answers { savedSlot.captured }

        service.addForDeviation(deviation)

        assertThat(savedSlot.captured.organizationId).isEqualTo(orgId)
        assertThat(savedSlot.captured.alcoholDeviation).isEqualTo(deviation)
        assertThat(savedSlot.captured.points).isEqualTo(AlcoholDeviationType.SKJENKING_MINDREAARIGE.penaltyPoints)
        assertThat(savedSlot.captured.violationType).isEqualTo(AlcoholDeviationType.SKJENKING_MINDREAARIGE)
        assertThat(savedSlot.captured.description)
            .isEqualTo("Auto: sjenkekontroll – SKJENKING_MINDREAARIGE")
    }

    @Test
    fun `addForDeviation uses police report source in description`() {
        val deviation = mockk<AlcoholDeviation>()
        every { deviation.organizationId } returns orgId
        every { deviation.reportSource } returns AlcoholReportSource.POLITIRAPPORT
        every { deviation.deviationType } returns AlcoholDeviationType.SKJENKING_MINDREAARIGE

        val savedSlot = slot<PenaltyPoint>()
        every { repository.save(capture(savedSlot)) } answers { savedSlot.captured }

        service.addForDeviation(deviation)

        assertThat(savedSlot.captured.description)
            .isEqualTo("Auto: politirapport – SKJENKING_MINDREAARIGE")
    }
}