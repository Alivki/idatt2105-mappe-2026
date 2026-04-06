package com.iksystem.food.deviation.service

import com.iksystem.common.deviation.model.DeviationSeverity
import com.iksystem.common.exception.NotFoundException
import com.iksystem.common.membership.repository.MembershipRepository
import com.iksystem.common.notifications.service.NotificationsService
import com.iksystem.common.security.AuthenticatedUser
import com.iksystem.common.user.model.User
import com.iksystem.common.user.repository.UserRepository
import com.iksystem.food.deviation.dto.CreateFoodDeviationRequest
import com.iksystem.food.deviation.model.FoodDeviation
import com.iksystem.food.deviation.model.FoodDeviationStatus
import com.iksystem.food.deviation.model.FoodDeviationType
import com.iksystem.food.deviation.repository.FoodDeviationRepository
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.Optional

class FoodDeviationServiceTest {

    private val repository = mockk<FoodDeviationRepository>()
    private val userRepository = mockk<UserRepository>()
    private val membershipRepository = mockk<MembershipRepository>()
    private val notificationsService = mockk<NotificationsService>(relaxed = true)

    private val service = FoodDeviationService(
        repository,
        userRepository,
        membershipRepository,
        notificationsService
    )

    private val reporter = mockk<User>()
    private val auth = mockk<AuthenticatedUser>()

    private val orgId = 1L
    private val userId = 10L

    @BeforeEach
    fun setup() {
        every { auth.requireOrganizationId() } returns orgId
        every { auth.userId } returns userId

        every { reporter.id } returns userId
        every { reporter.fullName } returns "Test User"

        every { userRepository.findById(userId) } returns Optional.of(reporter)
    }

    private fun makeDeviation(
        id: Long = 1L,
        description: String = "Test description",
        deviationType: FoodDeviationType = FoodDeviationType.RENHOLD,
        severity: DeviationSeverity = DeviationSeverity.MEDIUM,
        status: FoodDeviationStatus = FoodDeviationStatus.OPEN,
    ) = FoodDeviation(
        id = id,
        organizationId = orgId,
        reportedAt = Instant.parse("2024-01-15T10:00:00Z"),
        reportedByUser = reporter,
        deviationType = deviationType,
        severity = severity,
        description = description,
        status = status,
    )

    @Test
    fun `create saves and returns deviation`() {
        val request = CreateFoodDeviationRequest(
            deviationType = FoodDeviationType.RENHOLD,
            severity = DeviationSeverity.HIGH,
            description = "Dirty kitchen"
        )

        val saved = makeDeviation(
            id = 1L,
            description = "Dirty kitchen",
            deviationType = FoodDeviationType.RENHOLD,
            severity = DeviationSeverity.HIGH
        )

        every { repository.save(any()) } returns saved

        val result = service.create(request, auth)

        assertThat(result.id).isEqualTo(1L)
        assertThat(result.description).isEqualTo("Dirty kitchen")
        assertThat(result.deviationType).isEqualTo(FoodDeviationType.RENHOLD)

        verify { repository.save(any()) }
    }

    @Test
    fun `getById returns deviation when found`() {
        val deviation = makeDeviation(id = 1L)

        every {
            repository.findByIdAndOrganizationId(1L, orgId)
        } returns deviation

        val result = service.getById(1L, auth)

        assertThat(result.id).isEqualTo(1L)
        assertThat(result.organizationId).isEqualTo(orgId)
    }

    @Test
    fun `getById throws when not found`() {
        every {
            repository.findByIdAndOrganizationId(1L, orgId)
        } returns null

        assertThatThrownBy { service.getById(1L, auth) }
            .isInstanceOf(NotFoundException::class.java)
            .hasMessage("Food deviation not found")
    }

    @Test
    fun `delete removes deviation`() {
        val deviation = makeDeviation(id = 1L)

        every {
            repository.findByIdAndOrganizationId(1L, orgId)
        } returns deviation
        every { repository.delete(deviation) } just Runs

        service.delete(1L, auth)

        verify { repository.delete(deviation) }
    }

    @Test
    fun `default status is OPEN`() {
        val deviation = FoodDeviation(
            organizationId = orgId,
            reportedByUser = reporter,
            deviationType = FoodDeviationType.RENHOLD,
            severity = DeviationSeverity.LOW,
            description = "Test"
        )

        assertThat(deviation.status).isEqualTo(FoodDeviationStatus.OPEN)
    }
}