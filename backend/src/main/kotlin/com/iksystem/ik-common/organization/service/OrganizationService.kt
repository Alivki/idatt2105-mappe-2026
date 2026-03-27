package com.iksystem.`ik-common`.organization.service

import com.ik.ikcommon.exception.ConflictException
import com.ik.ikcommon.exception.NotFoundException
import com.iksystem.`ik-common`.organization.dto.CreateOrganizationRequest
import com.iksystem.`ik-common`.organization.dto.OrganizationResponse
import com.iksystem.`ik-common`.organization.model.Organization
import com.iksystem.`ik-common`.organization.repository.OrganizationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrganizationService(private val organizationRepository: OrganizationRepository) {
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

    @Transactional(readOnly = true)
    fun getById(id: Long): OrganizationResponse {
        val org = organizationRepository.findById(id)
            .orElseThrow { NotFoundException("Organization not found") }

        return org.toResponse()
    }

    @Transactional(readOnly = true)
    fun listAll(): List<OrganizationResponse> =
        organizationRepository.findAll().map { it.toResponse() }
}

fun Organization.toResponse() = OrganizationResponse(
    id = id,
    name = name,
    orgNumber = orgNumber,
)