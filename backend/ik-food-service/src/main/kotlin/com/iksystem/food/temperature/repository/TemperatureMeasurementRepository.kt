package com.iksystem.food.temperature.repository

import com.iksystem.food.temperature.model.TemperatureMeasurement
import com.iksystem.food.temperature.model.TemperatureMeasurementStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Repository for temperature measurements.
 */
@Repository
interface TemperatureMeasurementRepository : JpaRepository<TemperatureMeasurement, Long> {

    /**
     * Finds all measurements for an organization ordered by newest first.
     */
    fun findAllByOrganizationIdOrderByMeasuredAtDesc(organizationId: Long): List<TemperatureMeasurement>

    /**
     * Finds all measurements for an appliance ordered by newest first.
     */
    fun findAllByOrganizationIdAndApplianceIdOrderByMeasuredAtDesc(organizationId: Long, applianceId: Long): List<TemperatureMeasurement>

    /**
     * Finds the latest measurement for an appliance.
     */
    fun findTopByOrganizationIdAndApplianceIdOrderByMeasuredAtDesc(organizationId: Long, applianceId: Long): TemperatureMeasurement?

    /**
     * Finds measurements by ids within an organization.
     */
    fun findAllByOrganizationIdAndIdIn(organizationId: Long, ids: List<Long>): List<TemperatureMeasurement>

    /**
     * Counts measurements by status for an organization.
     */
    fun countByOrganizationIdAndStatus(organizationId: Long, status: TemperatureMeasurementStatus): Long

    /**
     * Finds measurements within a time range.
     */
    fun findAllByOrganizationIdAndMeasuredAtBetween(organizationId: Long, from: java.time.Instant, to: java.time.Instant): List<TemperatureMeasurement>
}