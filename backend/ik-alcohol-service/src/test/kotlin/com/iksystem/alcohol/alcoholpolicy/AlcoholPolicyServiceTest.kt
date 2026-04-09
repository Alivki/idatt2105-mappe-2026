package com.iksystem.alcohol.alcoholpolicy

import com.iksystem.alcohol.alcoholpolicy.dto.CreateAlcoholPolicyRequest
import com.iksystem.alcohol.alcoholpolicy.model.AgeCheckLimit
import com.iksystem.alcohol.alcoholpolicy.model.AlcoholPolicy
import com.iksystem.alcohol.alcoholpolicy.model.IdType
import com.iksystem.alcohol.alcoholpolicy.model.KnowledgeTestType
import com.iksystem.alcohol.alcoholpolicy.repository.AlcoholPolicyRepository
import com.iksystem.alcohol.alcoholpolicy.service.AlcoholPolicyService
import com.iksystem.common.documents.repository.DocumentRepository
import com.iksystem.common.exception.BadRequestException
import com.iksystem.common.exception.NotFoundException
import com.iksystem.common.security.AuthenticatedUser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.any
import org.mockito.Mockito.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.Instant
import java.time.LocalDate

class AlcoholPolicyServiceTest {

    private lateinit var alcoholPolicyRepository: AlcoholPolicyRepository
    private lateinit var documentRepository: DocumentRepository
    private lateinit var service: AlcoholPolicyService

    @BeforeEach
    fun setUp() {
        alcoholPolicyRepository = mock(AlcoholPolicyRepository::class.java)
        documentRepository = mock(DocumentRepository::class.java)
        service = AlcoholPolicyService(alcoholPolicyRepository, documentRepository)
    }

    @Test
    fun `existsForOrg returns true when policy exists`() {
        `when`(alcoholPolicyRepository.findByOrganizationId(1L)).thenReturn(policy())

        val result = service.existsForOrg(auth())

        assertTrue(result)
    }

    @Test
    fun `existsForOrg returns false when policy does not exist`() {
        `when`(alcoholPolicyRepository.findByOrganizationId(1L)).thenReturn(null)

        val result = service.existsForOrg(auth())

        assertFalse(result)
    }

    @Test
    fun `getByOrg returns mapped response`() {
        `when`(alcoholPolicyRepository.findByOrganizationId(1L)).thenReturn(
            policy(
                id = 7L,
                bevillingNumber = "ABC123",
                styrerName = "Manager Name",
                ageCheckLimit = AgeCheckLimit.UNDER_23,
                acceptedIdTypes = "PASS,BANKKORT",
                doubtRoutine = "Ask colleague",
                refusalProcedure = "Refuse service"
            )
        )

        val result = service.getByOrg(auth())

        assertEquals(7L, result.id)
        assertEquals("ABC123", result.bevillingNumber)
        assertEquals("Manager Name", result.styrerName)
        assertEquals(AgeCheckLimit.UNDER_23, result.ageCheckLimit)
        assertEquals(listOf(IdType.PASS, IdType.BANKKORT), result.acceptedIdTypes)
        assertEquals("Ask colleague", result.doubtRoutine)
        assertEquals("Refuse service", result.refusalProcedure)
    }

    @Test
    fun `getByOrg throws when policy is missing`() {
        `when`(alcoholPolicyRepository.findByOrganizationId(1L)).thenReturn(null)

        assertThrows<NotFoundException> {
            service.getByOrg(auth())
        }
    }

    @Test
    fun `upsert creates new policy with defaults when optional values are missing`() {
        val request = CreateAlcoholPolicyRequest(
            bevillingNumber = null,
            bevillingValidUntil = null,
            styrerName = null,
            stedfortrederName = null,
            bevillingDocumentId = null,
            kunnskapsproveCandidateName = null,
            kunnskapsproveBirthDate = null,
            kunnskapsproveType = null,
            kunnskapsproveMunicipality = null,
            kunnskapsprovePassedDate = null,
            kunnskapsproveDocumentId = null,
            ageCheckLimit = null,
            acceptedIdTypes = null,
            doubtRoutine = null,
            intoxicationSigns = null,
            refusalProcedure = null
        )

        `when`(alcoholPolicyRepository.findByOrganizationId(1L)).thenReturn(null)
        `when`(alcoholPolicyRepository.save(any(AlcoholPolicy::class.java))).thenAnswer { it.arguments[0] }

        val result = service.upsert(request, auth())

        assertEquals(AgeCheckLimit.UNDER_25, result.ageCheckLimit)
        assertEquals(
            listOf(IdType.PASS, IdType.FORERKORT, IdType.BANKKORT, IdType.NASJONALT_ID),
            result.acceptedIdTypes
        )
        assertNull(result.bevillingNumber)
        assertNull(result.doubtRoutine)

        verify(documentRepository, never()).findByIdAndOrganizationId(any(Long::class.java), any(Long::class.java))
    }

    @Test
    fun `upsert creates new policy with explicit values`() {
        val request = CreateAlcoholPolicyRequest(
            bevillingNumber = "BEV-99",
            bevillingValidUntil = LocalDate.parse("2027-06-01"),
            styrerName = "Anna",
            stedfortrederName = "Ben",
            bevillingDocumentId = null,
            kunnskapsproveCandidateName = "Chris",
            kunnskapsproveBirthDate = LocalDate.parse("1990-01-01"),
            kunnskapsproveType = KnowledgeTestType.SKJENKE,
            kunnskapsproveMunicipality = "Oslo",
            kunnskapsprovePassedDate = LocalDate.parse("2024-01-10"),
            kunnskapsproveDocumentId = null,
            ageCheckLimit = AgeCheckLimit.UNDER_23,
            acceptedIdTypes = listOf(IdType.PASS, IdType.NASJONALT_ID),
            doubtRoutine = "Double check",
            intoxicationSigns = "Slurred speech",
            refusalProcedure = "Refuse politely"
        )

        `when`(alcoholPolicyRepository.findByOrganizationId(1L)).thenReturn(null)
        `when`(alcoholPolicyRepository.save(any(AlcoholPolicy::class.java))).thenAnswer { invocation ->
            val saved = invocation.arguments[0] as AlcoholPolicy
            saved.copy(id = 88L)
        }

        val result = service.upsert(request, auth())

        assertEquals(88L, result.id)
        assertEquals("BEV-99", result.bevillingNumber)
        assertEquals(LocalDate.parse("2027-06-01"), result.bevillingValidUntil)
        assertEquals("Anna", result.styrerName)
        assertEquals("Ben", result.stedfortrederName)
        assertEquals("Chris", result.kunnskapsproveCandidateName)
        assertEquals(KnowledgeTestType.SKJENKE, result.kunnskapsproveType)
        assertEquals(AgeCheckLimit.UNDER_23, result.ageCheckLimit)
        assertEquals(listOf(IdType.PASS, IdType.NASJONALT_ID), result.acceptedIdTypes)
        assertEquals("Double check", result.doubtRoutine)
        assertEquals("Slurred speech", result.intoxicationSigns)
        assertEquals("Refuse politely", result.refusalProcedure)
    }

    @Test
    fun `upsert updates existing policy and keeps existing fallback values when request leaves them null`() {
        val existing = policy(
            id = 5L,
            bevillingNumber = "OLD",
            ageCheckLimit = AgeCheckLimit.UNDER_23,
            acceptedIdTypes = "PASS,NASJONALT_ID",
            doubtRoutine = "Existing doubt",
            intoxicationSigns = "Existing signs"
        )

        val request = CreateAlcoholPolicyRequest(
            bevillingNumber = "NEW",
            bevillingValidUntil = null,
            styrerName = "Updated manager",
            stedfortrederName = null,
            bevillingDocumentId = null,
            kunnskapsproveCandidateName = null,
            kunnskapsproveBirthDate = null,
            kunnskapsproveType = null,
            kunnskapsproveMunicipality = null,
            kunnskapsprovePassedDate = null,
            kunnskapsproveDocumentId = null,
            ageCheckLimit = null,
            acceptedIdTypes = null,
            doubtRoutine = null,
            intoxicationSigns = null,
            refusalProcedure = "Updated refusal"
        )

        `when`(alcoholPolicyRepository.findByOrganizationId(1L)).thenReturn(existing)
        `when`(alcoholPolicyRepository.save(any(AlcoholPolicy::class.java))).thenAnswer { it.arguments[0] }

        val result = service.upsert(request, auth())

        assertEquals(5L, result.id)
        assertEquals("NEW", result.bevillingNumber)
        assertEquals("Updated manager", result.styrerName)
        assertEquals(AgeCheckLimit.UNDER_23, result.ageCheckLimit)
        assertEquals(listOf(IdType.PASS, IdType.NASJONALT_ID), result.acceptedIdTypes)
        assertNull(result.doubtRoutine)
        assertNull(result.intoxicationSigns)
        assertEquals("Updated refusal", result.refusalProcedure)
    }

    @Test
    fun `upsert saves organization id from auth when creating`() {
        val request = CreateAlcoholPolicyRequest(
            bevillingNumber = "ORG-1",
            bevillingValidUntil = null,
            styrerName = null,
            stedfortrederName = null,
            bevillingDocumentId = null,
            kunnskapsproveCandidateName = null,
            kunnskapsproveBirthDate = null,
            kunnskapsproveType = null,
            kunnskapsproveMunicipality = null,
            kunnskapsprovePassedDate = null,
            kunnskapsproveDocumentId = null,
            ageCheckLimit = null,
            acceptedIdTypes = null,
            doubtRoutine = null,
            intoxicationSigns = null,
            refusalProcedure = null
        )

        `when`(alcoholPolicyRepository.findByOrganizationId(1L)).thenReturn(null)
        `when`(alcoholPolicyRepository.save(any(AlcoholPolicy::class.java))).thenAnswer { it.arguments[0] }

        service.upsert(request, auth())

        val captor = ArgumentCaptor.forClass(AlcoholPolicy::class.java)
        verify(alcoholPolicyRepository).save(captor.capture())

        assertEquals(1L, captor.value.organizationId)
        assertEquals("ORG-1", captor.value.bevillingNumber)
    }

    @Test
    fun `upsert throws when bevilling document is missing`() {
        val request = CreateAlcoholPolicyRequest(
            bevillingNumber = null,
            bevillingValidUntil = null,
            styrerName = null,
            stedfortrederName = null,
            bevillingDocumentId = 123L,
            kunnskapsproveCandidateName = null,
            kunnskapsproveBirthDate = null,
            kunnskapsproveType = null,
            kunnskapsproveMunicipality = null,
            kunnskapsprovePassedDate = null,
            kunnskapsproveDocumentId = null,
            ageCheckLimit = null,
            acceptedIdTypes = null,
            doubtRoutine = null,
            intoxicationSigns = null,
            refusalProcedure = null
        )

        `when`(alcoholPolicyRepository.findByOrganizationId(1L)).thenReturn(null)
        `when`(documentRepository.findByIdAndOrganizationId(123L, 1L)).thenReturn(null)

        assertThrows<BadRequestException> {
            service.upsert(request, auth())
        }
    }

    @Test
    fun `upsert throws when knowledge test document is missing`() {
        val request = CreateAlcoholPolicyRequest(
            bevillingNumber = null,
            bevillingValidUntil = null,
            styrerName = null,
            stedfortrederName = null,
            bevillingDocumentId = null,
            kunnskapsproveCandidateName = null,
            kunnskapsproveBirthDate = null,
            kunnskapsproveType = null,
            kunnskapsproveMunicipality = null,
            kunnskapsprovePassedDate = null,
            kunnskapsproveDocumentId = 456L,
            ageCheckLimit = null,
            acceptedIdTypes = null,
            doubtRoutine = null,
            intoxicationSigns = null,
            refusalProcedure = null
        )

        `when`(alcoholPolicyRepository.findByOrganizationId(1L)).thenReturn(null)
        `when`(documentRepository.findByIdAndOrganizationId(456L, 1L)).thenReturn(null)

        assertThrows<BadRequestException> {
            service.upsert(request, auth())
        }
    }

    private fun auth() = AuthenticatedUser(
        10L,
        1L,
        "MANAGER"
    )

    private fun policy(
        id: Long = 1L,
        organizationId: Long = 1L,
        bevillingNumber: String? = null,
        bevillingValidUntil: LocalDate? = null,
        styrerName: String? = null,
        stedfortrederName: String? = null,
        kunnskapsproveCandidateName: String? = null,
        kunnskapsproveBirthDate: LocalDate? = null,
        kunnskapsproveType: KnowledgeTestType? = null,
        kunnskapsproveMunicipality: String? = null,
        kunnskapsprovePassedDate: LocalDate? = null,
        ageCheckLimit: AgeCheckLimit = AgeCheckLimit.UNDER_25,
        acceptedIdTypes: String = "PASS,FORERKORT,BANKKORT,NASJONALT_ID",
        doubtRoutine: String? = null,
        intoxicationSigns: String? = null,
        refusalProcedure: String? = null,
        createdAt: Instant = Instant.parse("2026-01-01T10:00:00Z"),
        updatedAt: Instant = Instant.parse("2026-01-01T10:00:00Z"),
    ) = AlcoholPolicy(
        id = id,
        organizationId = organizationId,
        bevillingNumber = bevillingNumber,
        bevillingValidUntil = bevillingValidUntil,
        styrerName = styrerName,
        stedfortrederName = stedfortrederName,
        bevillingDocument = null,
        kunnskapsproveCandidateName = kunnskapsproveCandidateName,
        kunnskapsproveBirthDate = kunnskapsproveBirthDate,
        kunnskapsproveType = kunnskapsproveType,
        kunnskapsproveMunicipality = kunnskapsproveMunicipality,
        kunnskapsprovePassedDate = kunnskapsprovePassedDate,
        kunnskapsproveDocument = null,
        ageCheckLimit = ageCheckLimit,
        acceptedIdTypes = acceptedIdTypes,
        doubtRoutine = doubtRoutine,
        intoxicationSigns = intoxicationSigns,
        refusalProcedure = refusalProcedure,
        expiryNotified = false,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}