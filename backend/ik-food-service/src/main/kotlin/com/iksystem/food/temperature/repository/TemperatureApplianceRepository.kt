package com.iksystem.food.temperature.repository

import com.iksystem.food.temperature.model.TemperatureAppliance
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Repository for temperature appliances.
 */
@Repository
interface TemperatureApplianceRepository : JpaRepository<TemperatureAppliance, Long> {

    /**
     * Finds all appliances for an organization ordered by name.
     */
    fun findAllByOrganizationIdOrderByNameAsc(organizationId: Long): List<TemperatureAppliance>

    /**
     * Finds an appliance by id and organization.
     */
    fun findByIdAndOrganizationId(id: Long, organizationId: Long): TemperatureAppliance?
}