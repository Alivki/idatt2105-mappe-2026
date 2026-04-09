package com.iksystem.alcohol.alcoholpolicy.dto

import com.iksystem.alcohol.alcoholpolicy.model.AgeCheckLimit
import com.iksystem.alcohol.alcoholpolicy.model.IdType
import com.iksystem.alcohol.alcoholpolicy.model.KnowledgeTestType
import java.time.LocalDate

/**
 * Response DTO representing an organization's alcohol policy.
 *
 * Contains licensing information, responsible personnel,
 * knowledge test details, and operational procedures.
 */
data class AlcoholPolicyResponse(
    val id: Long,
    val bevillingNumber: String?,
    val bevillingValidUntil: LocalDate?,
    val styrerName: String?,
    val stedfortrederName: String?,
    val bevillingDocumentId: Long?,
    val kunnskapsproveCandidateName: String?,
    val kunnskapsproveBirthDate: LocalDate?,
    val kunnskapsproveType: KnowledgeTestType?,
    val kunnskapsproveMunicipality: String?,
    val kunnskapsprovePassedDate: LocalDate?,
    val kunnskapsproveDocumentId: Long?,
    val ageCheckLimit: AgeCheckLimit,
    val acceptedIdTypes: List<IdType>,
    val doubtRoutine: String?,
    val intoxicationSigns: String?,
    val refusalProcedure: String?
)

/**
 * Request DTO for creating or updating an alcohol policy.
 *
 * All fields are optional to support flexible creation and partial updates.
 */
data class CreateAlcoholPolicyRequest(
    val bevillingNumber: String?,
    val bevillingValidUntil: LocalDate?,
    val styrerName: String?,
    val stedfortrederName: String?,
    val bevillingDocumentId: Long?,
    val kunnskapsproveCandidateName: String?,
    val kunnskapsproveBirthDate: LocalDate?,
    val kunnskapsproveType: KnowledgeTestType?,
    val kunnskapsproveMunicipality: String?,
    val kunnskapsprovePassedDate: LocalDate?,
    val kunnskapsproveDocumentId: Long?,
    val ageCheckLimit: AgeCheckLimit?,
    val acceptedIdTypes: List<IdType>?,
    val doubtRoutine: String?,
    val intoxicationSigns: String?,
    val refusalProcedure: String?
)

/**
 * Alias for partial updates of an alcohol policy.
 *
 * Shares the same structure as [CreateAlcoholPolicyRequest].
 */
typealias UpdateAlcoholPolicyRequest = CreateAlcoholPolicyRequest