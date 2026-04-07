package com.iksystem.alcohol.alcoholpolicy.model

import com.iksystem.common.documents.model.Document
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(name = "alcohol_policies")
data class AlcoholPolicy (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "organization_id", nullable = false, unique = true)
    val organizationId: Long,

    @Column(name = "bevilling_number", nullable = true)
    val bevillingNumber: String? = null,

    @Column(name = "bevilling_valid_until", nullable = true)
    val bevillingValidUntil: LocalDate? = null,

    @Column(name = "styrer_name", nullable = true)
    val styrerName: String? = null,

    @Column(name = "stedfortreder_name", nullable = true)
    val stedfortrederName: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bevilling_document_id")
    val bevillingDocument: Document? = null,

    val kunnskapsproveCandidateName: String? = null,
    val kunnskapsproveBirthDate: LocalDate? = null,

    @Enumerated(EnumType.STRING)
    val kunnskapsproveType: KnowledgeTestType? = null,

    val kunnskapsproveMunicipality: String? = null,
    val kunnskapsprovePassedDate: LocalDate? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kunnskapsprove_document_id")
    val kunnskapsproveDocument: Document? = null,

    @Enumerated(EnumType.STRING)
    val ageCheckLimit: AgeCheckLimit = AgeCheckLimit.UNDER_25,

    @Column(name = "accepted_id_types")
    val acceptedIdTypes: String = "PASS,FORERKORT,BANKKORT,NASJONALT_ID",

    @Column(columnDefinition = "TEXT")
    val doubtRoutine: String? = null,

    @Column(columnDefinition = "TEXT")
    val intoxicationSigns: String? = null,

    @Column(name = "refusal_procedure", columnDefinition = "TEXT")
    val refusalProcedure: String? = null,

    var expiryNotified: Boolean = false,

    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)