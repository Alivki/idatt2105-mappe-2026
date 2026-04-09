package com.iksystem.alcohol.alcoholpolicy.service

import com.iksystem.alcohol.alcoholpolicy.dto.AlcoholPolicyResponse
import com.iksystem.alcohol.alcoholpolicy.dto.CreateAlcoholPolicyRequest
import com.iksystem.alcohol.alcoholpolicy.model.AgeCheckLimit
import com.iksystem.alcohol.alcoholpolicy.model.AlcoholPolicy
import com.iksystem.alcohol.alcoholpolicy.model.IdType
import com.iksystem.alcohol.alcoholpolicy.repository.AlcoholPolicyRepository
import com.iksystem.common.documents.repository.DocumentRepository
import com.iksystem.common.exception.BadRequestException
import com.iksystem.common.exception.NotFoundException
import com.iksystem.common.security.AuthenticatedUser
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service
import java.time.Instant

/**
 * Service responsible for managing alcohol policies at the organization level.
 *
 * Provides operations to check policy existence, retrieve the current policy,
 * and create or update a policy for the authenticated user's organization.
 *
 * Each organization may have at most one [AlcoholPolicy]. All operations are scoped
 * to the organization derived from the [AuthenticatedUser].
 *
 * @property alcoholPolicyRepository Repository for persisting and querying [AlcoholPolicy] entities.
 * @property documentRepository Repository used to resolve and validate referenced documents.
 */
@Service
class AlcoholPolicyService(
    private val alcoholPolicyRepository: AlcoholPolicyRepository,
    private val documentRepository: DocumentRepository,
) {
    /**
     * Checks whether an alcohol policy exists for the authenticated user's organization.
     *
     * @param auth The authenticated user, used to resolve the organization ID.
     * @return `true` if a policy exists for the organization, `false` otherwise.
     */
    @Transactional(readOnly = true)
    fun existsForOrg(auth: AuthenticatedUser): Boolean =
        alcoholPolicyRepository.findByOrganizationId(auth.requireOrganizationId()) != null

    /**
     * Retrieves the alcohol policy for the authenticated user's organization.
     *
     * @param auth The authenticated user, used to resolve the organization ID.
     * @return The [AlcoholPolicyResponse] representing the organization's current policy.
     * @throws NotFoundException if no alcohol policy exists for the organization.
     */
    @Transactional(readOnly = true)
    fun getByOrg(auth: AuthenticatedUser): AlcoholPolicyResponse {
        val policy = alcoholPolicyRepository.findByOrganizationId(auth.requireOrganizationId())
            ?: throw NotFoundException("No alcohol policy found for organization")
        return policy.toResponse()
    }

    /**
     * Creates or updates the alcohol policy for the authenticated user's organization.
     *
     * If a policy already exists for the organization, it is updated with the values
     * from [request]. If no policy exists, a new one is created. Fields not present
     * in the request fall back to the existing policy's values where applicable, or
     * to sensible defaults otherwise.
     *
     * Referenced documents (bevilling and kunnskapsprøve) are validated to ensure
     * they belong to the organization before being associated with the policy.
     *
     * @param request The request payload containing the policy fields to create or update.
     * @param auth The authenticated user, used to resolve the organization ID.
     * @return The [AlcoholPolicyResponse] reflecting the saved state of the policy.
     * @throws BadRequestException if a referenced document ID does not exist or does not
     * belong to the organization.
     */
    @Transactional
    fun upsert(request: CreateAlcoholPolicyRequest, auth: AuthenticatedUser): AlcoholPolicyResponse {
        val orgId = auth.requireOrganizationId()
        val existing = alcoholPolicyRepository.findByOrganizationId(orgId)

        val bevillingDoc = request.bevillingDocumentId?.let {
            documentRepository.findByIdAndOrganizationId(it, orgId)
                ?: throw BadRequestException("Bevilling document not found")
        }

        val kunnskapDoc = request.kunnskapsproveDocumentId?.let {
            documentRepository.findByIdAndOrganizationId(it, orgId)
                ?: throw BadRequestException("Kunnskapsprove document not found")
        }

        val policy = (existing ?: AlcoholPolicy(organizationId = orgId)).copy(
            bevillingNumber = request.bevillingNumber,
            bevillingValidUntil = request.bevillingValidUntil,
            styrerName = request.styrerName,
            stedfortrederName = request.stedfortrederName,
            bevillingDocument = bevillingDoc,
            kunnskapsproveCandidateName = request.kunnskapsproveCandidateName,
            kunnskapsproveBirthDate = request.kunnskapsproveBirthDate,
            kunnskapsproveType = request.kunnskapsproveType,
            kunnskapsproveMunicipality = request.kunnskapsproveMunicipality,
            kunnskapsprovePassedDate = request.kunnskapsprovePassedDate,
            kunnskapsproveDocument = kunnskapDoc,
            ageCheckLimit = request.ageCheckLimit ?: existing?.ageCheckLimit ?: AgeCheckLimit.UNDER_25,
            acceptedIdTypes = request.acceptedIdTypes?.joinToString(",") ?: existing?.acceptedIdTypes ?: "PASS,FORERKORT,BANKKORT,NASJONALT_ID",
            doubtRoutine = request.doubtRoutine,
            intoxicationSigns = request.intoxicationSigns,
            refusalProcedure = request.refusalProcedure,
            updatedAt = Instant.now()
        )

        return alcoholPolicyRepository.save(policy).toResponse()
    }

    /**
     * Maps an [AlcoholPolicy] entity to an [AlcoholPolicyResponse] DTO.
     *
     * The comma-separated [AlcoholPolicy.acceptedIdTypes] string is parsed and
     * converted to a list of [IdType] enum values, filtering out any blank entries.
     */
    private fun AlcoholPolicy.toResponse() = AlcoholPolicyResponse(
        id = id,
        bevillingNumber = bevillingNumber,
        bevillingValidUntil = bevillingValidUntil,
        styrerName = styrerName,
        stedfortrederName = stedfortrederName,
        bevillingDocumentId = bevillingDocument?.id,
        kunnskapsproveCandidateName = kunnskapsproveCandidateName,
        kunnskapsproveBirthDate = kunnskapsproveBirthDate,
        kunnskapsproveType = kunnskapsproveType,
        kunnskapsproveMunicipality = kunnskapsproveMunicipality,
        kunnskapsprovePassedDate = kunnskapsprovePassedDate,
        kunnskapsproveDocumentId = kunnskapsproveDocument?.id,
        ageCheckLimit = ageCheckLimit,
        acceptedIdTypes = acceptedIdTypes.split(",").filter { it.isNotBlank() }.map { IdType.valueOf(it) },
        doubtRoutine = doubtRoutine,
        intoxicationSigns = intoxicationSigns,
        refusalProcedure = refusalProcedure
    )
}