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

/**
 * Represents an alcohol policy for an organization, containing all information required
 * to document and enforce legal alcohol sales compliance in Norway.
 *
 * This includes bevilling (liquor licence) details, knowledge test (kunnskapsprøve) results,
 * age verification settings, and operational routines for staff.
 *
 * Each organization may have at most one alcohol policy, enforced by the unique constraint
 * on [organizationId].
 *
 * @property id Auto-generated primary key.
 * @property organizationId The ID of the organization this policy belongs to. Must be unique.
 * @property bevillingNumber The liquor licence number issued by the municipality, if available.
 * @property bevillingValidUntil The expiry date of the liquor licence, if set.
 * @property styrerName The full name of the designated licence manager (styrer).
 * @property stedfortrederName The full name of the deputy licence manager (stedfortreder), if appointed.
 * @property bevillingDocument An optional reference to the uploaded bevilling document.
 * @property kunnskapsproveCandidateName The full name of the candidate who took the knowledge test.
 * @property kunnskapsproveBirthDate The birth date of the knowledge test candidate.
 * @property kunnskapsproveType The type of knowledge test taken. See [KnowledgeTestType].
 * @property kunnskapsproveMunicipality The municipality in which the knowledge test was conducted.
 * @property kunnskapsprovePassedDate The date on which the knowledge test was passed.
 * @property kunnskapsproveDocument An optional reference to the uploaded knowledge test certificate.
 * @property ageCheckLimit The age threshold used when checking customer ID. Defaults to [AgeCheckLimit.UNDER_25].
 * @property acceptedIdTypes A comma-separated list of accepted identification types (e.g. `PASS,FORERKORT`).
 * @property doubtRoutine A description of the routine to follow when staff are uncertain about a customer's age or sobriety.
 * @property intoxicationSigns A description of the signs staff should look for when assessing customer intoxication.
 * @property refusalProcedure A description of the procedure to follow when refusing a sale.
 * @property expiryNotified Whether a notification has been sent regarding the upcoming or past expiry of the bevilling.
 * @property createdAt The timestamp when this policy was first created.
 * @property updatedAt The timestamp when this policy was last updated.
 */
@Entity
@Table(name = "alcohol_policies")
data class AlcoholPolicy(
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