package com.iksystem.`ik-common`.organization.service

import com.ik.ikcommon.exception.ConflictException
import com.ik.ikcommon.exception.NotFoundException
import com.iksystem.`ik-common`.organization.dto.CreateOrganizationRequest
import com.iksystem.`ik-common`.organization.dto.OrganizationResponse
import com.iksystem.`ik-common`.organization.model.Organization
import com.iksystem.`ik-common`.organization.repository.OrganizationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service layer for organization management.
 *
 * Handles creation, lookup, and listing of organizations while
 * enforcing uniqueness constraints on the organization name.
 *
 * @property organizationRepository JPA repository used for persistence operations.
 */
@Service
class OrganizationService(private val organizationRepository: OrganizationRepository) {

    /**
     * Creates a new organization.
     *
     * @param request DTO containing the desired name and optional org number.
     * @return An [OrganizationResponse] representing the newly created organization.
     * @throws ConflictException if an organization with the same name already exists.
     */
    @Transactional
    fun create(request: CreateOrganizationRequest): OrganizationResponse {
        if (organizationRepository.existsByName(request.name)) {
            throw ConflictException("Organization name already exists")
        }

        val org = organizationRepository.save(
            Organization(name = request.name, orgNumber = request.orgNumber)
        )

        return org.toResponse()
    }

    /**
     * Retrieves a single organization by its primary key.
     *
     * @param id The organization ID.
     * @return An [OrganizationResponse] for the matching organization.
     * @throws NotFoundException if no organization with the given [id] exists.
     */
    @Transactional(readOnly = true)
    fun getById(id: Long): OrganizationResponse {
        val org = organizationRepository.findById(id)
            .orElseThrow { NotFoundException("Organization not found") }

        return org.toResponse()
    }

    /**
     * Lists all organizations in the system.
     *
     * @return A list of [OrganizationResponse] DTOs.
     */
    @Transactional(readOnly = true)
    fun listAll(): List<OrganizationResponse> =
        organizationRepository.findAll().map { it.toResponse() }
}

/**
 * Extension function that maps an [Organization] entity to an [OrganizationResponse] DTO.
 */
fun Organization.toResponse() = OrganizationResponse(
    id = id,
    name = name,
    orgNumber = orgNumber,
)