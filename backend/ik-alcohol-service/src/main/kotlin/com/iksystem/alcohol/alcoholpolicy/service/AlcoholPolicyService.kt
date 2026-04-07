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

@Service
class AlcoholPolicyService(
    private val alcoholPolicyRepository: AlcoholPolicyRepository,
    private val documentRepository: DocumentRepository,
) {
    @Transactional(readOnly = true)
    fun existsForOrg(auth: AuthenticatedUser): Boolean =
        alcoholPolicyRepository.findByOrganizationId(auth.requireOrganizationId()) != null

    @Transactional(readOnly = true)
    fun getByOrg(auth: AuthenticatedUser): AlcoholPolicyResponse {
        val policy = alcoholPolicyRepository.findByOrganizationId(auth.requireOrganizationId())
            ?: throw NotFoundException("No alcohol policy found for organization")
        return policy.toResponse()
    }

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