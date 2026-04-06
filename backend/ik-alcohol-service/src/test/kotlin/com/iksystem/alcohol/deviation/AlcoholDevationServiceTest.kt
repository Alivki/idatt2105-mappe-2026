package com.iksystem.alcohol.deviation.service

import com.iksystem.alcohol.deviation.dto.CreateAlcoholDeviationRequest
import com.iksystem.alcohol.deviation.dto.UpdateAlcoholDeviationRequest
import com.iksystem.alcohol.deviation.model.*
import com.iksystem.alcohol.deviation.repository.AlcoholDeviationRepository
import com.iksystem.alcohol.penaltypoints.service.PenaltyPointService
import com.iksystem.common.exception.BadRequestException
import com.iksystem.common.exception.NotFoundException
import com.iksystem.common.membership.repository.MembershipRepository
import com.iksystem.common.notifications.model.NotificationType
import com.iksystem.common.notifications.model.ReferenceType
import com.iksystem.common.notifications.service.NotificationsService
import com.iksystem.common.security.AuthenticatedUser
import com.iksystem.common.user.model.User
import com.iksystem.common.user.repository.UserRepository
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

class AlcoholDeviationServiceTest {

    private val repository = mockk<AlcoholDeviationRepository>()
    private val userRepository = mockk<UserRepository>()
    private val membershipRepository = mockk<MembershipRepository>()
    private val penaltyPointService = mockk<PenaltyPointService>(relaxed = true)
    private val notificationsService = mockk<NotificationsService>(relaxed = true)

    private val service = AlcoholDeviationService(
        repository,
        userRepository,
        membershipRepository,
        penaltyPointService,
        notificationsService,
    )

    private val orgId = 1L
    private val userId = 10L

    private val auth = mockk<AuthenticatedUser> {
        every { requireOrganizationId() } returns orgId
        every { this@mockk.userId } returns userId
    }

    private val reporter = mockk<User> {
        every { id } returns userId
        every { fullName } returns "Test User"
    }

    private fun makeDeviation(
        id: Long = 1L,
        reportSource: AlcoholReportSource = AlcoholReportSource.EGENRAPPORT,
        deviationType: AlcoholDeviationType = AlcoholDeviationType.BERUSET_PERSON_I_LOKALET,
        status: AlcoholDeviationStatus = AlcoholDeviationStatus.OPEN,
        responsibleUser: User? = null,
    ) = AlcoholDeviation(
        id = id,
        organizationId = orgId,
        reportedAt = Instant.now(),
        reportedByUser = reporter,
        reportSource = reportSource,
        deviationType = deviationType,
        description = "Some description",
        status = status,
        preventiveResponsibleUser = responsibleUser,
    )

    @BeforeEach
    fun setUp() {
        every { userRepository.findById(userId) } returns Optional.of(reporter)
    }

    // List mapping

    @Test
    fun `list returns mapped responses ordered by reportedAt desc`() {
        val deviations = listOf(makeDeviation(1L), makeDeviation(2L))
        every { repository.findAllByOrganizationIdOrderByReportedAtDesc(orgId) } returns deviations

        val result = service.list(auth)

        assertThat(result).hasSize(2)
        assertThat(result.map { it.id }).containsExactly(1L, 2L)
    }

    @Test
    fun `list returns empty list when no deviations exist`() {
        every { repository.findAllByOrganizationIdOrderByReportedAtDesc(orgId) } returns emptyList()

        assertThat(service.list(auth)).isEmpty()
    }

    // getById

    @Test
    fun `getById returns response when found`() {
        val deviation = makeDeviation()
        every { repository.findByIdAndOrganizationId(1L, orgId) } returns deviation

        val result = service.getById(1L, auth)

        assertThat(result.id).isEqualTo(1L)
        assertThat(result.organizationId).isEqualTo(orgId)
    }

    @Test
    fun `getById throws NotFoundException when deviation does not exist`() {
        every { repository.findByIdAndOrganizationId(99L, orgId) } returns null

        assertThatThrownBy { service.getById(99L, auth) }
            .isInstanceOf(NotFoundException::class.java)
    }

    // Different create functions

    @Test
    fun `create saves deviation and returns response`() {
        val request = CreateAlcoholDeviationRequest(
            reportSource = AlcoholReportSource.EGENRAPPORT,
            deviationType = AlcoholDeviationType.BERUSET_PERSON_I_LOKALET,
            description = "Test description",
        )
        val saved = makeDeviation()
        every { repository.save(any()) } returns saved

        val result = service.create(request, auth)

        assertThat(result.id).isEqualTo(1L)
        verify { repository.save(any()) }
    }

    @Test
    fun `create adds penalty points when report source is SJENKEKONTROLL`() {
        val request = CreateAlcoholDeviationRequest(
            reportSource = AlcoholReportSource.SJENKEKONTROLL,
            deviationType = AlcoholDeviationType.SKJENKING_MINDREAARIGE,
            description = "Test",
        )
        val saved = makeDeviation(reportSource = AlcoholReportSource.SJENKEKONTROLL)
        every { repository.save(any()) } returns saved

        service.create(request, auth)

        verify { penaltyPointService.addForDeviation(saved) }
    }

    @Test
    fun `create adds penalty points when report source is POLITIRAPPORT`() {
        val request = CreateAlcoholDeviationRequest(
            reportSource = AlcoholReportSource.POLITIRAPPORT,
            deviationType = AlcoholDeviationType.SKJENKING_MINDREAARIGE,
            description = "Test",
        )
        val saved = makeDeviation(reportSource = AlcoholReportSource.POLITIRAPPORT)
        every { repository.save(any()) } returns saved

        service.create(request, auth)

        verify { penaltyPointService.addForDeviation(saved) }
    }

    @Test
    fun `create does NOT add penalty points for EGENRAPPORT`() {
        val request = CreateAlcoholDeviationRequest(
            reportSource = AlcoholReportSource.EGENRAPPORT,
            deviationType = AlcoholDeviationType.BERUSET_PERSON_I_LOKALET,
            description = "Test",
        )
        val saved = makeDeviation()
        every { repository.save(any()) } returns saved

        service.create(request, auth)

        verify(exactly = 0) { penaltyPointService.addForDeviation(any()) }
    }

    @Test
    fun `create sends notification to org admins and managers`() {
        val request = CreateAlcoholDeviationRequest(
            reportSource = AlcoholReportSource.EGENRAPPORT,
            deviationType = AlcoholDeviationType.BERUSET_PERSON_I_LOKALET,
            description = "Test",
        )
        val saved = makeDeviation()
        every { repository.save(any()) } returns saved

        service.create(request, auth)

        verify {
            notificationsService.sendToOrgAdminsAndManagers(
                organizationId = orgId,
                type = NotificationType.DEVIATION_CREATED,
                title = any(),
                message = any(),
                referenceType = ReferenceType.DEVIATION,
                referenceId = saved.id,
            )
        }
    }

    @Test
    fun `create sends assignment notification when responsible user is set`() {
        val responsibleUser = mockk<User> { every { id } returns 20L; every { fullName } returns "Bob" }
        every { userRepository.findById(20L) } returns Optional.of(responsibleUser)
        every { membershipRepository.existsByUserIdAndOrganizationId(20L, orgId) } returns true

        val request = CreateAlcoholDeviationRequest(
            reportSource = AlcoholReportSource.EGENRAPPORT,
            deviationType = AlcoholDeviationType.BERUSET_PERSON_I_LOKALET,
            description = "Test",
            preventiveResponsibleUserId = 20L,
        )
        val saved = makeDeviation(responsibleUser = responsibleUser)
        every { repository.save(any()) } returns saved

        service.create(request, auth)

        verify {
            notificationsService.send(
                organizationId = orgId,
                recipientUserId = 20L,
                type = NotificationType.DEVIATION_ASSIGNED,
                title = any(),
                message = any(),
                referenceType = ReferenceType.DEVIATION,
                referenceId = saved.id,
            )
        }
    }

    @Test
    fun `create parses custom reportedAt when provided`() {
        val instant = "2024-01-15T10:00:00Z"
        val request = CreateAlcoholDeviationRequest(
            reportedAt = instant,
            reportSource = AlcoholReportSource.EGENRAPPORT,
            deviationType = AlcoholDeviationType.BERUSET_PERSON_I_LOKALET,
            description = "Test",
        )
        val saved = makeDeviation()
        val savedSlot = slot<AlcoholDeviation>()
        every { repository.save(capture(savedSlot)) } returns saved

        service.create(request, auth)

        assertThat(savedSlot.captured.reportedAt).isEqualTo(Instant.parse(instant))
    }

    @Test
    fun `create trims whitespace from description`() {
        val request = CreateAlcoholDeviationRequest(
            reportSource = AlcoholReportSource.EGENRAPPORT,
            deviationType = AlcoholDeviationType.BERUSET_PERSON_I_LOKALET,
            description = "  needs trimming  ",
        )
        val savedSlot = slot<AlcoholDeviation>()
        val saved = makeDeviation()
        every { repository.save(capture(savedSlot)) } returns saved

        service.create(request, auth)

        assertThat(savedSlot.captured.description).isEqualTo("needs trimming")
    }

    @Test
    fun `create throws BadRequestException when responsible user is not org member`() {
        val outsider = mockk<User> { every { id } returns 99L; every { fullName } returns "Outsider" }
        every { userRepository.findById(99L) } returns Optional.of(outsider)
        every { membershipRepository.existsByUserIdAndOrganizationId(99L, orgId) } returns false

        val request = CreateAlcoholDeviationRequest(
            reportSource = AlcoholReportSource.EGENRAPPORT,
            deviationType = AlcoholDeviationType.BERUSET_PERSON_I_LOKALET,
            description = "Test",
            preventiveResponsibleUserId = 99L,
        )

        assertThatThrownBy { service.create(request, auth) }
            .isInstanceOf(BadRequestException::class.java)
    }

    // Update function

    @Test
    fun `update patches only provided fields`() {
        val existing = makeDeviation()
        every { repository.findByIdAndOrganizationId(1L, orgId) } returns existing
        val savedSlot = slot<AlcoholDeviation>()
        every { repository.save(capture(savedSlot)) } answers { savedSlot.captured }

        val request = UpdateAlcoholDeviationRequest(status = AlcoholDeviationStatus.CLOSED)
        service.update(1L, request, auth)

        assertThat(savedSlot.captured.status).isEqualTo(AlcoholDeviationStatus.CLOSED)
        assertThat(savedSlot.captured.description).isEqualTo(existing.description)
    }

    @Test
    fun `update preserves existing responsibleUser when not provided`() {
        val existingUser = mockk<User> { every { id } returns 20L; every { fullName } returns "Alice" }
        val existing = makeDeviation(responsibleUser = existingUser)
        every { repository.findByIdAndOrganizationId(1L, orgId) } returns existing
        val savedSlot = slot<AlcoholDeviation>()
        every { repository.save(capture(savedSlot)) } answers { savedSlot.captured }

        service.update(1L, UpdateAlcoholDeviationRequest(), auth)

        assertThat(savedSlot.captured.preventiveResponsibleUser).isEqualTo(existingUser)
    }

    @Test
    fun `update throws NotFoundException when deviation not found`() {
        every { repository.findByIdAndOrganizationId(99L, orgId) } returns null

        assertThatThrownBy { service.update(99L, UpdateAlcoholDeviationRequest(), auth) }
            .isInstanceOf(NotFoundException::class.java)
    }

    @Test
    fun `update sets updatedAt to now`() {
        val existing = makeDeviation()
        val before = Instant.now()
        every { repository.findByIdAndOrganizationId(1L, orgId) } returns existing
        val savedSlot = slot<AlcoholDeviation>()
        every { repository.save(capture(savedSlot)) } answers { savedSlot.captured }

        service.update(1L, UpdateAlcoholDeviationRequest(), auth)

        assertThat(savedSlot.captured.updatedAt).isAfterOrEqualTo(before)
    }

    // Deletion function

    @Test
    fun `delete removes deviation`() {
        val deviation = makeDeviation()
        every { repository.findByIdAndOrganizationId(1L, orgId) } returns deviation
        every { repository.delete(deviation) } just Runs

        service.delete(1L, auth)

        verify { repository.delete(deviation) }
    }

    @Test
    fun `delete throws NotFoundException when deviation not found`() {
        every { repository.findByIdAndOrganizationId(99L, orgId) } returns null

        assertThatThrownBy { service.delete(99L, auth) }
            .isInstanceOf(NotFoundException::class.java)
    }
}