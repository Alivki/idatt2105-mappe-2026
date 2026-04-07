package com.iksystem.alcohol.alcoholpolicy.dto

import com.iksystem.alcohol.alcoholpolicy.model.AgeCheckLimit
import com.iksystem.alcohol.alcoholpolicy.model.IdType
import com.iksystem.alcohol.alcoholpolicy.model.KnowledgeTestType
import java.time.LocalDate

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

typealias UpdateAlcoholPolicyRequest = CreateAlcoholPolicyRequest