package com.iksystem.`ik-common`.organization.controller

import com.iksystem.`ik-common`.organization.dto.CreateOrganizationRequest
import com.iksystem.`ik-common`.organization.dto.OrganizationResponse
import com.iksystem.`ik-common`.organization.service.OrganizationService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller exposing organization endpoints under `/api/v1/organizations`.
 *
 * @property organizationService Service that handles organization business logic.
 */
@RestController
@RequestMapping("/api/v1/organizations")
class OrganizationController(private val organizationService: OrganizationService) {

    /**
     * Creates a new organization.
     *
     * @param request Validated [CreateOrganizationRequest] payload.
     * @return `201 Created` with the [OrganizationResponse] body.
     */
    @PostMapping
    fun create(@Valid @RequestBody request: CreateOrganizationRequest): ResponseEntity<OrganizationResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(organizationService.create(request))

    /**
     * Retrieves a single organization by its ID.
     *
     * @param id Path variable — the organization's primary key.
     * @return `200 OK` with the [OrganizationResponse] body.
     */
    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<OrganizationResponse> =
        ResponseEntity.ok(organizationService.getById(id))

    /**
     * Lists all organizations.
     *
     * @return `200 OK` with a list of [OrganizationResponse] DTOs.
     */
    @GetMapping
    fun listAll(): ResponseEntity<List<OrganizationResponse>> =
        ResponseEntity.ok(organizationService.listAll())
}