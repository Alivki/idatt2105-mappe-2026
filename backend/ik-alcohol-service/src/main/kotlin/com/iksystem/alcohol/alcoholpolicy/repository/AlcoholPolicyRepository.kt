package com.iksystem.alcohol.alcoholpolicy.repository

import com.iksystem.alcohol.alcoholpolicy.model.AlcoholPolicy
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AlcoholPolicyRepository : JpaRepository<AlcoholPolicy, Long> {
    fun findByOrganizationId(organizationId: Long): AlcoholPolicy?
}