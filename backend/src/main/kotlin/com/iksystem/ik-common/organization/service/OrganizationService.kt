package com.iksystem.`ik-common`.organization.service

import com.ik.ikcommon.exception.ConflictException
import com.ik.ikcommon.exception.NotFoundException
import com.iksystem.`ik-common`.membership.model.Membership
import com.iksystem.`ik-common`.membership.repository.MembershipRepository
import com.iksystem.`ik-common`.organization.dto.CreateOrganizationRequest
import com.iksystem.`ik-common`.organization.dto.OrganizationResponse
import com.iksystem.`ik-common`.organization.model.Organization
import com.iksystem.`ik-common`.organization.repository.OrganizationRepository
import com.iksystem.`ik-common`.user.model.Role
import com.iksystem.`ik-common`.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service layer for organization management.
 *
 * When a user creates an organization, they automatically become its ADMIN.
 */
@Service
class OrganizationService(
    private val organizationRepository: OrganizationRepository,
    private val membershipRepository: MembershipRepository,
    private val userRepository: UserRepository,
) {

    /**
     * Creates a new organization and makes the calling user its ADMIN.
     */
    @Transactional
    fun create(request: CreateOrganizationRequest, callerUserId: Long): OrganizationResponse {
        if (organizationRepository.existsByName(request.name)) {
            throw ConflictException("Organization name already exists")
        }

        val org = organizationRepository.save(
            Organization(name = request.name, orgNumber = request.orgNumber)
        )

        val user = userRepository.findById(callerUserId)
            .orElseThrow { NotFoundException("User not found") }

        membershipRepository.save(
            Membership(user = user, organization = org, role = Role.ADMIN)
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

    @Transactional
    fun delete(id: Long) {
        if (!organizationRepository.existsById(id)) {
            throw NotFoundException("Organization not found")
        }
        organizationRepository.deleteById(id)
    }
}

fun Organization.toResponse() = OrganizationResponse(
    id = id,
    name = name,
    orgNumber = orgNumber,
)
