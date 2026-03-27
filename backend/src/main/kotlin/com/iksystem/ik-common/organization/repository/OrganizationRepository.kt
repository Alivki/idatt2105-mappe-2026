package com.iksystem.`ik-common`.organization.repository

import com.iksystem.`ik-common`.organization.model.Organization
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrganizationRepository : JpaRepository<Organization, Long> {
    fun findByName(name: String): Organization?
    fun existsByName(name: String): Boolean
}