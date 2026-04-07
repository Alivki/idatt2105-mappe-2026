package com.iksystem.food.temperature.service

import com.iksystem.common.exception.BadRequestException
import com.iksystem.common.exception.NotFoundException
import com.iksystem.common.security.AuthenticatedUser
import com.iksystem.common.user.model.User
import com.iksystem.common.user.repository.UserRepository
import com.iksystem.food.temperature.dto.CreateTemperatureApplianceRequest
import com.iksystem.food.temperature.dto.RegisterTemperatureMeasurementRequest
import com.iksystem.food.temperature.dto.UpdateTemperatureApplianceRequest
import com.iksystem.food.temperature.model.TemperatureAppliance
import com.iksystem.food.temperature.model.TemperatureApplianceType
import com.iksystem.food.temperature.model.TemperatureMeasurement
import com.iksystem.food.temperature.model.TemperatureMeasurementStatus
import com.iksystem.food.temperature.repository.TemperatureApplianceRepository
import com.iksystem.food.temperature.repository.TemperatureMeasurementRepository
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import java.util.Optional

class TemperatureServiceTest {

    private val applianceRepository = mockk<TemperatureApplianceRepository>()
    private val measurementRepository = mockk<TemperatureMeasurementRepository>()
    private val userRepository = mockk<UserRepository>()

    private val service = TemperatureService(
        applianceRepository,
        measurementRepository,
        userRepository,
    )

    private val auth = mockk<AuthenticatedUser>()
    private val user = mockk<User>()

    private val orgId = 1L
    private val userId = 10L

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        every { auth.requireOrganizationId() } returns orgId
        every { auth.userId } returns userId

        every { user.id } returns userId
        every { user.fullName } returns "Test User"

        every { userRepository.findById(userId) } returns Optional.of(user)
    }

    private fun appliance(
        id: Long = 1L,
        name: String = "Main fridge",
        type: TemperatureApplianceType = TemperatureApplianceType.FRIDGE,
        min: BigDecimal = BigDecimal("0.00"),
        max: BigDecimal = BigDecimal("4.00"),
        isActive: Boolean = true,
        updatedAt: Instant = Instant.parse("2024-01-15T10:00:00Z"),
    ) = TemperatureAppliance(
        id = id,
        organizationId = orgId,
        name = name,
        applianceType = type,
        minTemperature = min,
        maxTemperature = max,
        isActive = isActive,
        updatedAt = updatedAt,
    )

    private fun measurement(
        id: Long = 1L,
        appliance: TemperatureAppliance = appliance(),
        temperature: BigDecimal = BigDecimal("3.20"),
        status: TemperatureMeasurementStatus = TemperatureMeasurementStatus.OK,
        note: String? = "Looks fine",
        measuredAt: Instant = Instant.parse("2024-01-15T10:30:00Z"),
    ) = TemperatureMeasurement(
        id = id,
        organizationId = orgId,
        appliance = appliance,
        measuredByUser = user,
        measuredAt = measuredAt,
        temperature = temperature,
        note = note,
        status = status,
    )

    @Test
    fun `listAppliances returns mapped responses with last measurement`() {
        val appliance = appliance(id = 1L)
        val measurement = measurement(appliance = appliance)

        every { applianceRepository.findAllByOrganizationIdOrderByNameAsc(orgId) } returns listOf(appliance)
        every { measurementRepository.findTopByOrganizationIdAndApplianceIdOrderByMeasuredAtDesc(orgId, 1L) } returns measurement

        val result = service.listAppliances(auth)

        assertThat(result).hasSize(1)
        assertThat(result[0].id).isEqualTo(1L)
        assertThat(result[0].lastMeasurement).isNotNull
        assertThat(result[0].lastMeasurement!!.temperature).isEqualByComparingTo("3.20")
    }

    @Test
    fun `listAppliances returns responses without last measurement when none exists`() {
        val appliance = appliance(id = 1L)

        every { applianceRepository.findAllByOrganizationIdOrderByNameAsc(orgId) } returns listOf(appliance)
        every { measurementRepository.findTopByOrganizationIdAndApplianceIdOrderByMeasuredAtDesc(orgId, 1L) } returns null

        val result = service.listAppliances(auth)

        assertThat(result).hasSize(1)
        assertThat(result[0].lastMeasurement).isNull()
    }

    @Test
    fun `createAppliance trims name and saves appliance`() {
        val request = CreateTemperatureApplianceRequest(
            name = "  Cold room  ",
            applianceType = TemperatureApplianceType.FRIDGE,
            minTemperature = BigDecimal("0.00"),
            maxTemperature = BigDecimal("4.00"),
        )

        val savedSlot = slot<TemperatureAppliance>()
        every { applianceRepository.save(capture(savedSlot)) } answers { savedSlot.captured }

        val result = service.createAppliance(request, auth)

        assertThat(savedSlot.captured.name).isEqualTo("Cold room")
        assertThat(savedSlot.captured.organizationId).isEqualTo(orgId)
        assertThat(result.name).isEqualTo("Cold room")
    }

    @Test
    fun `createAppliance throws when min is not lower than max`() {
        val request = CreateTemperatureApplianceRequest(
            name = "Fridge",
            applianceType = TemperatureApplianceType.FRIDGE,
            minTemperature = BigDecimal("4.00"),
            maxTemperature = BigDecimal("4.00"),
        )

        assertThatThrownBy { service.createAppliance(request, auth) }
            .isInstanceOf(BadRequestException::class.java)
            .hasMessage("Min temperature must be lower than max temperature")
    }

    @Test
    fun `updateAppliance updates provided fields and returns last measurement`() {
        val existing = appliance(id = 1L, name = "Old fridge")
        val lastMeasurement = measurement(appliance = existing)

        every { applianceRepository.findByIdAndOrganizationId(1L, orgId) } returns existing
        every { applianceRepository.save(any()) } answers { firstArg() }
        every { measurementRepository.findTopByOrganizationIdAndApplianceIdOrderByMeasuredAtDesc(orgId, 1L) } returns lastMeasurement

        val result = service.updateAppliance(
            1L,
            UpdateTemperatureApplianceRequest(
                name = "  New fridge  ",
                minTemperature = BigDecimal("1.00"),
                maxTemperature = BigDecimal("5.00"),
                isActive = false,
            ),
            auth
        )

        assertThat(result.name).isEqualTo("New fridge")
        assertThat(result.minTemperature).isEqualByComparingTo("1.00")
        assertThat(result.maxTemperature).isEqualByComparingTo("5.00")
        assertThat(result.isActive).isFalse()
        assertThat(result.lastMeasurement).isNotNull
    }

    @Test
    fun `updateAppliance preserves existing name when blank string provided`() {
        val existing = appliance(id = 1L, name = "Original")
        every { applianceRepository.findByIdAndOrganizationId(1L, orgId) } returns existing
        every { applianceRepository.save(any()) } answers { firstArg() }
        every { measurementRepository.findTopByOrganizationIdAndApplianceIdOrderByMeasuredAtDesc(orgId, 1L) } returns null

        val result = service.updateAppliance(
            1L,
            UpdateTemperatureApplianceRequest(name = "   "),
            auth
        )

        assertThat(result.name).isEqualTo("Original")
    }

    @Test
    fun `updateAppliance throws when appliance not found`() {
        every { applianceRepository.findByIdAndOrganizationId(99L, orgId) } returns null

        assertThatThrownBy {
            service.updateAppliance(99L, UpdateTemperatureApplianceRequest(), auth)
        }
            .isInstanceOf(NotFoundException::class.java)
            .hasMessage("Temperature appliance not found")
    }

    @Test
    fun `deleteAppliance removes appliance`() {
        val appliance = appliance(id = 1L)
        every { applianceRepository.findByIdAndOrganizationId(1L, orgId) } returns appliance
        every { applianceRepository.delete(appliance) } just Runs

        service.deleteAppliance(1L, auth)

        verify { applianceRepository.delete(appliance) }
    }

    @Test
    fun `listMeasurements returns all measurements when applianceId is null`() {
        val appliance = appliance(id = 1L)
        val measurements = listOf(
            measurement(id = 1L, appliance = appliance),
            measurement(id = 2L, appliance = appliance, temperature = BigDecimal("5.50"), status = TemperatureMeasurementStatus.DEVIATION),
        )

        every { measurementRepository.findAllByOrganizationIdOrderByMeasuredAtDesc(orgId) } returns measurements

        val result = service.listMeasurements(auth, null)

        assertThat(result).hasSize(2)
        assertThat(result.map { it.id }).containsExactly(1L, 2L)
    }

    @Test
    fun `listMeasurements filters by applianceId when provided`() {
        val appliance = appliance(id = 1L)
        val measurements = listOf(measurement(id = 1L, appliance = appliance))

        every { applianceRepository.findByIdAndOrganizationId(1L, orgId) } returns appliance
        every { measurementRepository.findAllByOrganizationIdAndApplianceIdOrderByMeasuredAtDesc(orgId, 1L) } returns measurements

        val result = service.listMeasurements(auth, 1L)

        assertThat(result).hasSize(1)
        assertThat(result[0].applianceId).isEqualTo(1L)
    }

    @Test
    fun `registerMeasurement saves OK measurement when temperature is in range`() {
        val appliance = appliance(min = BigDecimal("0.00"), max = BigDecimal("4.00"))
        val request = RegisterTemperatureMeasurementRequest(
            applianceId = 1L,
            temperature = BigDecimal("3.00"),
            note = "  Stable  ",
        )

        val savedSlot = slot<TemperatureMeasurement>()
        every { applianceRepository.findByIdAndOrganizationId(1L, orgId) } returns appliance
        every { measurementRepository.save(capture(savedSlot)) } answers { savedSlot.captured }

        val result = service.registerMeasurement(request, auth)

        assertThat(savedSlot.captured.status).isEqualTo(TemperatureMeasurementStatus.OK)
        assertThat(savedSlot.captured.note).isEqualTo("Stable")
        assertThat(result.status).isEqualTo(TemperatureMeasurementStatus.OK)
    }

    @Test
    fun `registerMeasurement saves DEVIATION measurement when temperature is out of range`() {
        val appliance = appliance(min = BigDecimal("0.00"), max = BigDecimal("4.00"))
        val request = RegisterTemperatureMeasurementRequest(
            applianceId = 1L,
            temperature = BigDecimal("5.10"),
        )

        val savedSlot = slot<TemperatureMeasurement>()
        every { applianceRepository.findByIdAndOrganizationId(1L, orgId) } returns appliance
        every { measurementRepository.save(capture(savedSlot)) } answers { savedSlot.captured }

        val result = service.registerMeasurement(request, auth)

        assertThat(savedSlot.captured.status).isEqualTo(TemperatureMeasurementStatus.DEVIATION)
        assertThat(result.status).isEqualTo(TemperatureMeasurementStatus.DEVIATION)
    }

    @Test
    fun `registerMeasurement throws when appliance is inactive`() {
        val appliance = appliance(id = 1L, isActive = false)
        val request = RegisterTemperatureMeasurementRequest(
            applianceId = 1L,
            temperature = BigDecimal("2.00"),
        )

        every { applianceRepository.findByIdAndOrganizationId(1L, orgId) } returns appliance

        assertThatThrownBy { service.registerMeasurement(request, auth) }
            .isInstanceOf(BadRequestException::class.java)
            .hasMessage("Cannot register measurement for inactive appliance")
    }

    @Test
    fun `registerMeasurement uses provided measuredAt`() {
        val appliance = appliance(id = 1L)
        val request = RegisterTemperatureMeasurementRequest(
            applianceId = 1L,
            temperature = BigDecimal("2.00"),
            measuredAt = "2024-01-15T11:00:00Z",
        )

        val savedSlot = slot<TemperatureMeasurement>()
        every { applianceRepository.findByIdAndOrganizationId(1L, orgId) } returns appliance
        every { measurementRepository.save(capture(savedSlot)) } answers { savedSlot.captured }

        service.registerMeasurement(request, auth)

        assertThat(savedSlot.captured.measuredAt).isEqualTo(Instant.parse("2024-01-15T11:00:00Z"))
    }

    @Test
    fun `deleteMeasurements returns zero when ids list is empty`() {
        val result = service.deleteMeasurements(emptyList(), auth)

        assertThat(result).isZero()
        verify(exactly = 0) { measurementRepository.findAllByOrganizationIdAndIdIn(any(), any()) }
        verify(exactly = 0) { measurementRepository.deleteAll(any<List<TemperatureMeasurement>>()) }
    }

    @Test
    fun `deleteMeasurements deletes only matching rows and returns deleted count`() {
        val appliance = appliance(id = 1L)
        val rows = listOf(
            measurement(id = 1L, appliance = appliance),
            measurement(id = 2L, appliance = appliance),
        )

        every { measurementRepository.findAllByOrganizationIdAndIdIn(orgId, listOf(1L, 2L, 3L)) } returns rows
        every { measurementRepository.deleteAll(rows) } just Runs

        val result = service.deleteMeasurements(listOf(1L, 2L, 3L), auth)

        assertThat(result).isEqualTo(2)
        verify { measurementRepository.deleteAll(rows) }
    }
}