package com.iksystem.alcohol.deviation.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.iksystem.alcohol.deviation.dto.AlcoholDeviationResponse
import com.iksystem.alcohol.deviation.dto.CreateAlcoholDeviationRequest
import com.iksystem.alcohol.deviation.dto.UpdateAlcoholDeviationRequest
import com.iksystem.alcohol.deviation.model.*
import com.iksystem.alcohol.deviation.service.AlcoholDeviationService
import com.iksystem.common.exception.NotFoundException
import com.iksystem.common.security.AuthenticatedUser
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(AlcoholDeviationController::class)
class AlcoholDeviationControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var service: AlcoholDeviationService

    private val baseUrl = "/api/v1/deviations/alcohol"

    private fun sampleResponse(id: Long = 1L) = AlcoholDeviationResponse(
        id = id,
        organizationId = 1L,
        reportedAt = "2024-01-15T10:00:00Z",
        reportedByUserId = 10L,
        reportedByUserName = "Test User",
        reportSource = AlcoholReportSource.EGENRAPPORT,
        deviationType = AlcoholDeviationType.BERUSET_PERSON_I_LOKALET,
        description = "Test description",
        immediateAction = null,
        causalAnalysis = null,
        causalExplanation = null,
        preventiveMeasures = null,
        preventiveDeadline = null,
        preventiveResponsibleUserId = null,
        preventiveResponsibleUserName = null,
        status = AlcoholDeviationStatus.OPEN,
        createdAt = "2024-01-15T10:00:00Z",
        updatedAt = "2024-01-15T10:00:00Z",
    )

    // GET (alcohol)

    @Test
    @WithMockUser
    fun `list returns 200 with deviations`() {
        val responses = listOf(sampleResponse(1L), sampleResponse(2L))
        whenever(service.list(any())).thenReturn(responses)

        mockMvc.perform(get(baseUrl))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[1].id").value(2))
    }

    @Test
    fun `list returns 401 when unauthenticated`() {
        mockMvc.perform(get(baseUrl))
            .andExpect(status().isUnauthorized)
    }

    // GET (alcohol id)

    @Test
    @WithMockUser
    fun `getById returns 200 with deviation`() {
        whenever(service.getById(eq(1L), any())).thenReturn(sampleResponse(1L))

        mockMvc.perform(get("$baseUrl/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.description").value("Test description"))
    }

    @Test
    @WithMockUser
    fun `getById returns 404 when not found`() {
        whenever(service.getById(eq(99L), any())).thenThrow(NotFoundException("Not found"))

        mockMvc.perform(get("$baseUrl/99"))
            .andExpect(status().isNotFound)
    }

    // Post

    @Test
    @WithMockUser
    fun `create returns 201 with created deviation`() {
        val request = CreateAlcoholDeviationRequest(
            reportSource = AlcoholReportSource.EGENRAPPORT,
            deviationType = AlcoholDeviationType.BERUSET_PERSON_I_LOKALET,
            description = "New deviation",
        )
        whenever(service.create(any(), any())).thenReturn(sampleResponse())

        mockMvc.perform(
            post(baseUrl)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
    }

    @Test
    @WithMockUser
    fun `create returns 400 when description is blank`() {
        val request = mapOf(
            "reportSource" to "EGENRAPPORT",
            "deviationType" to "BERUSET_PERSON_I_LOKALET",
            "description" to "",
        )

        mockMvc.perform(
            post(baseUrl)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser
    fun `create returns 400 when reportSource is missing`() {
        val request = mapOf(
            "deviationType" to "BERUSET_PERSON_I_LOKALET",
            "description" to "Valid description",
        )

        mockMvc.perform(
            post(baseUrl)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser
    fun `create returns 400 when deviationType is missing`() {
        val request = mapOf(
            "reportSource" to "EGENRAPPORT",
            "description" to "Valid description",
        )

        mockMvc.perform(
            post(baseUrl)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    // Patch alcohol by id

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `update returns 200 for admin`() {
        val request = UpdateAlcoholDeviationRequest(status = AlcoholDeviationStatus.CLOSED)
        whenever(service.update(eq(1L), any(), any())).thenReturn(sampleResponse())

        mockMvc.perform(
            patch("$baseUrl/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
    }

    @Test
    @WithMockUser(roles = ["MANAGER"])
    fun `update returns 200 for manager`() {
        val request = UpdateAlcoholDeviationRequest(status = AlcoholDeviationStatus.UNDER_TREATMENT)
        whenever(service.update(eq(1L), any(), any())).thenReturn(sampleResponse())

        mockMvc.perform(
            patch("$baseUrl/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
    }

    @Test
    @WithMockUser(roles = ["USER"])
    fun `update returns 403 for regular user`() {
        val request = UpdateAlcoholDeviationRequest(status = AlcoholDeviationStatus.CLOSED)

        mockMvc.perform(
            patch("$baseUrl/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `update returns 404 when deviation not found`() {
        whenever(service.update(eq(99L), any(), any())).thenThrow(NotFoundException("Not found"))

        mockMvc.perform(
            patch("$baseUrl/99")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(UpdateAlcoholDeviationRequest()))
        )
            .andExpect(status().isNotFound)
    }

    // Delete alcohol by id

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `delete returns 204 for admin`() {
        whenever(service.delete(eq(1L), any())).then { }

        mockMvc.perform(
            delete("$baseUrl/1").with(csrf())
        )
            .andExpect(status().isNoContent)
    }

    @Test
    @WithMockUser(roles = ["MANAGER"])
    fun `delete returns 204 for manager`() {
        whenever(service.delete(eq(1L), any())).then { }

        mockMvc.perform(
            delete("$baseUrl/1").with(csrf())
        )
            .andExpect(status().isNoContent)
    }

    @Test
    @WithMockUser(roles = ["USER"])
    fun `delete returns 403 for regular user`() {
        mockMvc.perform(
            delete("$baseUrl/1").with(csrf())
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `delete returns 404 when deviation not found`() {
        whenever(service.delete(eq(99L), any())).thenThrow(NotFoundException("Not found"))

        mockMvc.perform(
            delete("$baseUrl/99").with(csrf())
        )
            .andExpect(status().isNotFound)
    }
}