package com.iksystem.alcohol.alcoholpolicy

import com.iksystem.alcohol.alcoholpolicy.controller.AlcoholPolicyController
import com.iksystem.alcohol.alcoholpolicy.dto.AlcoholPolicyResponse
import com.iksystem.alcohol.alcoholpolicy.dto.CreateAlcoholPolicyRequest
import com.iksystem.alcohol.alcoholpolicy.model.AgeCheckLimit
import com.iksystem.alcohol.alcoholpolicy.model.IdType
import com.iksystem.alcohol.alcoholpolicy.model.KnowledgeTestType
import com.iksystem.alcohol.alcoholpolicy.service.AlcoholPolicyService
import com.iksystem.common.security.AuthenticatedUser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.LocalDate

class AlcoholPolicyControllerTest {

    private lateinit var alcoholPolicyService: AlcoholPolicyService
    private lateinit var controller: AlcoholPolicyController

    @BeforeEach
    fun setUp() {
        alcoholPolicyService = mock(AlcoholPolicyService::class.java)
        controller = AlcoholPolicyController(alcoholPolicyService)
    }

    @Test
    fun `exists returns 200 with true`() {
        val auth = auth()

        `when`(alcoholPolicyService.existsForOrg(auth)).thenReturn(true)

        val result = controller.exists(auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(true, result.body!!["exists"])

        verify(alcoholPolicyService).existsForOrg(auth)
    }

    @Test
    fun `exists returns 200 with false`() {
        val auth = auth()

        `when`(alcoholPolicyService.existsForOrg(auth)).thenReturn(false)

        val result = controller.exists(auth)

        assertEquals(200, result.statusCode.value())
        assertEquals(false, result.body!!["exists"])
    }

    @Test
    fun `get returns 200 with policy`() {
        val auth = auth()
        val response = response()

        `when`(alcoholPolicyService.getByOrg(auth)).thenReturn(response)

        val result = controller.get(auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(1L, result.body!!.id)
        assertEquals(AgeCheckLimit.UNDER_25, result.body!!.ageCheckLimit)
        assertEquals(listOf(IdType.PASS, IdType.BANKKORT), result.body!!.acceptedIdTypes)

        verify(alcoholPolicyService).getByOrg(auth)
    }

    @Test
    fun `upsert returns 200 with updated policy`() {
        val auth = auth()
        val request = CreateAlcoholPolicyRequest(
            bevillingNumber = "BEV-1",
            bevillingValidUntil = LocalDate.parse("2027-01-01"),
            styrerName = "Anna",
            stedfortrederName = "Ben",
            bevillingDocumentId = null,
            kunnskapsproveCandidateName = "Chris",
            kunnskapsproveBirthDate = LocalDate.parse("1990-01-01"),
            kunnskapsproveType = KnowledgeTestType.SKJENKE,
            kunnskapsproveMunicipality = "Oslo",
            kunnskapsprovePassedDate = LocalDate.parse("2024-01-01"),
            kunnskapsproveDocumentId = null,
            ageCheckLimit = AgeCheckLimit.UNDER_23,
            acceptedIdTypes = listOf(IdType.PASS, IdType.NASJONALT_ID),
            doubtRoutine = "Check again",
            intoxicationSigns = "Signs",
            refusalProcedure = "Refuse"
        )
        val response = response(ageCheckLimit = AgeCheckLimit.UNDER_23)

        `when`(alcoholPolicyService.upsert(request, auth)).thenReturn(response)

        val result = controller.upsert(request, auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(1L, result.body!!.id)
        assertEquals(AgeCheckLimit.UNDER_23, result.body!!.ageCheckLimit)

        verify(alcoholPolicyService).upsert(request, auth)
    }

    @Test
    fun `patch returns 200 with updated policy`() {
        val auth = auth()
        val request = CreateAlcoholPolicyRequest(
            bevillingNumber = null,
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
        val response = response()

        `when`(alcoholPolicyService.upsert(request, auth)).thenReturn(response)

        val result = controller.patch(request, auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(1L, result.body!!.id)

        verify(alcoholPolicyService).upsert(request, auth)
    }

    private fun auth() = AuthenticatedUser(
        10L,
        1L,
        "MANAGER"
    )

    private fun response(
        ageCheckLimit: AgeCheckLimit = AgeCheckLimit.UNDER_25
    ) = AlcoholPolicyResponse(
        id = 1L,
        bevillingNumber = "BEV-1",
        bevillingValidUntil = LocalDate.parse("2027-01-01"),
        styrerName = "Anna",
        stedfortrederName = "Ben",
        bevillingDocumentId = null,
        kunnskapsproveCandidateName = "Chris",
        kunnskapsproveBirthDate = LocalDate.parse("1990-01-01"),
        kunnskapsproveType = KnowledgeTestType.SKJENKE,
        kunnskapsproveMunicipality = "Oslo",
        kunnskapsprovePassedDate = LocalDate.parse("2024-01-01"),
        kunnskapsproveDocumentId = null,
        ageCheckLimit = ageCheckLimit,
        acceptedIdTypes = listOf(IdType.PASS, IdType.BANKKORT),
        doubtRoutine = "Check again",
        intoxicationSigns = "Signs",
        refusalProcedure = "Refuse"
    )
}