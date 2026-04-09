package com.iksystem.food.temperature.controller

import com.iksystem.common.security.AuthenticatedUser
import com.iksystem.food.temperature.dto.CreateTemperatureApplianceRequest
import com.iksystem.food.temperature.dto.DeleteTemperatureMeasurementsRequest
import com.iksystem.food.temperature.dto.RegisterTemperatureMeasurementRequest
import com.iksystem.food.temperature.dto.TemperatureApplianceResponse
import com.iksystem.food.temperature.dto.TemperatureMeasurementResponse
import com.iksystem.food.temperature.dto.UpdateTemperatureApplianceRequest
import com.iksystem.food.temperature.service.TemperatureService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for temperature appliances and measurements.
 */
@Tag(name = "Temperature", description = "Temperature appliance and measurement management for IK-Mat")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/temperature")
class TemperatureController(
    private val service: TemperatureService,
) {

    /**
     * Lists all appliances.
     */
    @Operation(summary = "List temperature appliances", description = "Returns all temperature appliances for the active organization.")
    @ApiResponse(responseCode = "200", description = "Appliance list returned")
    @GetMapping("/appliances")
    fun listAppliances(
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<List<TemperatureApplianceResponse>> =
        ResponseEntity.ok(service.listAppliances(auth))

    /**
     * Creates a new appliance.
     */
    @Operation(summary = "Create temperature appliance")
    @ApiResponse(responseCode = "201", description = "Appliance created")
    @PostMapping("/appliances")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    fun createAppliance(
        @Valid @RequestBody request: CreateTemperatureApplianceRequest,
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<TemperatureApplianceResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(service.createAppliance(request, auth))

    /**
     * Updates an appliance.
     */
    @Operation(summary = "Update temperature appliance")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Appliance updated"),
        ApiResponse(responseCode = "404", description = "Appliance not found"),
    )
    @PatchMapping("/appliances/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    fun updateAppliance(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateTemperatureApplianceRequest,
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<TemperatureApplianceResponse> =
        ResponseEntity.ok(service.updateAppliance(id, request, auth))

    /**
     * Deletes an appliance.
     */
    @Operation(summary = "Delete temperature appliance")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Appliance deleted"),
        ApiResponse(responseCode = "404", description = "Appliance not found"),
    )
    @DeleteMapping("/appliances/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    fun deleteAppliance(
        @PathVariable id: Long,
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<Void> {
        service.deleteAppliance(id, auth)
        return ResponseEntity.noContent().build()
    }

    /**
     * Lists measurements.
     */
    @Operation(summary = "List temperature measurements", description = "Returns temperature measurements, optionally filtered by appliance.")
    @ApiResponse(responseCode = "200", description = "Measurement list returned")
    @GetMapping("/measurements")
    fun listMeasurements(
        @RequestParam(required = false) applianceId: Long?,
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<List<TemperatureMeasurementResponse>> =
        ResponseEntity.ok(service.listMeasurements(auth, applianceId))

    /**
     * Registers a measurement.
     */
    @Operation(summary = "Register temperature measurement")
    @ApiResponse(responseCode = "201", description = "Measurement registered")
    @PostMapping("/measurements")
    fun registerMeasurement(
        @Valid @RequestBody request: RegisterTemperatureMeasurementRequest,
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<TemperatureMeasurementResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(service.registerMeasurement(request, auth))

    /**
     * Deletes selected measurements.
     */
    @Operation(summary = "Delete selected measurements")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Measurements deleted"),
        ApiResponse(responseCode = "403", description = "Insufficient permissions"),
    )
    @DeleteMapping("/measurements")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    fun deleteMeasurements(
        @Valid @RequestBody request: DeleteTemperatureMeasurementsRequest,
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<Map<String, Int>> {
        val deleted = service.deleteMeasurements(request.ids, auth)
        return ResponseEntity.ok(mapOf("deleted" to deleted))
    }
}