package com.iksystem.`ik-common`.user.repository

import com.iksystem.`ik-common`.user.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository: JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
    fun existsByEmail(email: String): Boolean
    fun findAllByOrganizationId(organizationId: Long): List<User>
    fun findByIdAndOrganizationId(id: Long, organizationId: Long): User?
}