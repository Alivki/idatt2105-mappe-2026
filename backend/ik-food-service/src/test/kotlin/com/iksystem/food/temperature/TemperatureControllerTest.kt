package com.iksystem.food.temperature.controller

import com.iksystem.common.security.AuthenticatedUser
import com.iksystem.food.temperature.dto.CreateTemperatureApplianceRequest
import com.iksystem.food.temperature.dto.DeleteTemperatureMeasurementsRequest
import com.iksystem.food.temperature.dto.RegisterTemperatureMeasurementRequest
import com.iksystem.food.temperature.dto.TemperatureApplianceResponse
import com.iksystem.food.temperature.dto.TemperatureMeasurementResponse
import com.iksystem.food.temperature.dto.UpdateTemperatureApplianceRequest
import com.iksystem.food.temperature.model.TemperatureApplianceType
import com.iksystem.food.temperature.model.TemperatureMeasurementStatus
import com.iksystem.food.temperature.service.TemperatureService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.math.BigDecimal

class TemperatureControllerTest {

    private lateinit var service: TemperatureService
    private lateinit var controller: TemperatureController

    @BeforeEach
    fun setUp() {
        service = mock(TemperatureService::class.java)
        controller = TemperatureController(service)
    }

    @Test
    fun `listAppliances returns 200 with appliance list`() {
        val auth = employeeAuth()
        val response = listOf(
            applianceResponse(
                id = 1L,
                name = "Fridge 1",
                applianceType = TemperatureApplianceType.FRIDGE
            ),
            applianceResponse(
                id = 2L,
                name = "Freezer 1",
                applianceType = TemperatureApplianceType.FREEZER
            )
        )

        `when`(service.listAppliances(auth)).thenReturn(response)

        val result = controller.listAppliances(auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(2, result.body!!.size)
        assertEquals(1L, result.body!![0].id)
        assertEquals("Fridge 1", result.body!![0].name)
        assertEquals(TemperatureApplianceType.FREEZER, result.body!![1].applianceType)

        verify(service).listAppliances(auth)
    }

    @Test
    fun `createAppliance returns 201 with created appliance`() {
        val auth = managerAuth()
        val request = CreateTemperatureApplianceRequest(
            name = "  Fridge 1  ",
            applianceType = TemperatureApplianceType.FRIDGE,
            minTemperature = BigDecimal("0.00"),
            maxTemperature = BigDecimal("4.00")
        )
        val response = applianceResponse(
            id = 10L,
            name = "Fridge 1",
            applianceType = TemperatureApplianceType.FRIDGE,
            minTemperature = BigDecimal("0.00"),
            maxTemperature = BigDecimal("4.00")
        )

        `when`(service.createAppliance(request, auth)).thenReturn(response)

        val result = controller.createAppliance(request, auth)

        assertEquals(201, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(10L, result.body!!.id)
        assertEquals("Fridge 1", result.body!!.name)
        assertEquals(BigDecimal("4.00"), result.body!!.maxTemperature)

        verify(service).createAppliance(request, auth)
    }

    @Test
    fun `updateAppliance returns 200 with updated appliance`() {
        val auth = managerAuth()
        val request = UpdateTemperatureApplianceRequest(
            name = "Updated Fridge",
            minTemperature = BigDecimal("1.00"),
            maxTemperature = BigDecimal("5.00"),
            isActive = false
        )
        val response = applianceResponse(
            id = 11L,
            name = "Updated Fridge",
            applianceType = TemperatureApplianceType.FRIDGE,
            minTemperature = BigDecimal("1.00"),
            maxTemperature = BigDecimal("5.00"),
            isActive = false
        )

        `when`(service.updateAppliance(11L, request, auth)).thenReturn(response)

        val result = controller.updateAppliance(11L, request, auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(11L, result.body!!.id)
        assertEquals("Updated Fridge", result.body!!.name)
        assertEquals(false, result.body!!.isActive)

        verify(service).updateAppliance(11L, request, auth)
    }

    @Test
    fun `deleteAppliance returns 204 with empty body`() {
        val auth = managerAuth()

        val result = controller.deleteAppliance(12L, auth)

        assertEquals(204, result.statusCode.value())
        assertNull(result.body)

        verify(service).deleteAppliance(12L, auth)
    }

    @Test
    fun `listMeasurements without applianceId returns 200 with measurements`() {
        val auth = employeeAuth()
        val response = listOf(
            measurementResponse(
                id = 20L,
                applianceId = 1L,
                applianceName = "Fridge 1",
                status = TemperatureMeasurementStatus.OK
            ),
            measurementResponse(
                id = 21L,
                applianceId = 2L,
                applianceName = "Freezer 1",
                status = TemperatureMeasurementStatus.DEVIATION
            )
        )

        `when`(service.listMeasurements(auth, null)).thenReturn(response)

        val result = controller.listMeasurements(null, auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(2, result.body!!.size)
        assertEquals(20L, result.body!![0].id)
        assertEquals("Freezer 1", result.body!![1].applianceName)

        verify(service).listMeasurements(auth, null)
    }

    @Test
    fun `listMeasurements with applianceId returns 200 with filtered measurements`() {
        val auth = employeeAuth()
        val response = listOf(
            measurementResponse(
                id = 22L,
                applianceId = 3L,
                applianceName = "Fridge 3",
                status = TemperatureMeasurementStatus.OK
            )
        )

        `when`(service.listMeasurements(auth, 3L)).thenReturn(response)

        val result = controller.listMeasurements(3L, auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(1, result.body!!.size)
        assertEquals(22L, result.body!![0].id)
        assertEquals(3L, result.body!![0].applianceId)

        verify(service).listMeasurements(auth, 3L)
    }

    @Test
    fun `registerMeasurement returns 201 with created measurement`() {
        val auth = employeeAuth()
        val request = RegisterTemperatureMeasurementRequest(
            applianceId = 1L,
            temperature = BigDecimal("3.50"),
            note = "  Morning check  ",
            measuredAt = "2026-04-08T10:00:00Z"
        )
        val response = measurementResponse(
            id = 30L,
            applianceId = 1L,
            applianceName = "Fridge 1",
            temperature = BigDecimal("3.50"),
            note = "Morning check",
            status = TemperatureMeasurementStatus.OK
        )

        `when`(service.registerMeasurement(request, auth)).thenReturn(response)

        val result = controller.registerMeasurement(request, auth)

        assertEquals(201, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(30L, result.body!!.id)
        assertEquals(BigDecimal("3.50"), result.body!!.temperature)
        assertEquals(TemperatureMeasurementStatus.OK, result.body!!.status)

        verify(service).registerMeasurement(request, auth)
    }

    @Test
    fun `deleteMeasurements returns 200 with deleted count`() {
        val auth = managerAuth()
        val request = DeleteTemperatureMeasurementsRequest(ids = listOf(1L, 2L, 3L))

        `when`(service.deleteMeasurements(request.ids, auth)).thenReturn(3)

        val result = controller.deleteMeasurements(request, auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(3, result.body!!["deleted"])

        verify(service).deleteMeasurements(request.ids, auth)
    }

    private fun employeeAuth() = AuthenticatedUser(
        10L,
        1L,
        "EMPLOYEE"
    )

    private fun managerAuth() = AuthenticatedUser(
        20L,
        1L,
        "MANAGER"
    )

    private fun applianceResponse(
        id: Long,
        organizationId: Long = 1L,
        name: String,
        applianceType: TemperatureApplianceType,
        minTemperature: BigDecimal = BigDecimal("0.00"),
        maxTemperature: BigDecimal = BigDecimal("4.00"),
        isActive: Boolean = true,
        createdAt: String = "2026-04-08T10:00:00Z",
        updatedAt: String = "2026-04-08T10:00:00Z",
        lastMeasurement: TemperatureMeasurementResponse? = null
    ) = TemperatureApplianceResponse(
        id = id,
        organizationId = organizationId,
        name = name,
        applianceType = applianceType,
        minTemperature = minTemperature,
        maxTemperature = maxTemperature,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastMeasurement = lastMeasurement
    )

    private fun measurementResponse(
        id: Long,
        organizationId: Long = 1L,
        applianceId: Long,
        applianceName: String,
        applianceType: TemperatureApplianceType = TemperatureApplianceType.FRIDGE,
        measuredByUserId: Long = 10L,
        measuredByUserName: String = "Employee User",
        measuredAt: String = "2026-04-08T10:00:00Z",
        temperature: BigDecimal = BigDecimal("3.50"),
        note: String? = null,
        status: TemperatureMeasurementStatus,
        createdAt: String = "2026-04-08T10:00:00Z"
    ) = TemperatureMeasurementResponse(
        id = id,
        organizationId = organizationId,
        applianceId = applianceId,
        applianceName = applianceName,
        applianceType = applianceType,
        measuredByUserId = measuredByUserId,
        measuredByUserName = measuredByUserName,
        measuredAt = measuredAt,
        temperature = temperature,
        note = note,
        status = status,
        createdAt = createdAt
    )
}