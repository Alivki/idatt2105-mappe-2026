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

@RestController
@RequestMapping("/api/v1/organizations")
class OrganizationController(private val organizationService: OrganizationService) {
    @PostMapping
    fun create(@Valid @RequestBody request: CreateOrganizationRequest): ResponseEntity<OrganizationResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(organizationService.create(request))

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<OrganizationResponse> =
        ResponseEntity.ok(organizationService.getById(id))

    @GetMapping
    fun listAll(): ResponseEntity<List<OrganizationResponse>> =
        ResponseEntity.ok(organizationService.listAll())
}