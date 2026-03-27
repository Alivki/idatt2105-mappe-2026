package com.iksystem.`ik-common`.security

class AuthenticatedUser (
    val userId: Long,
    val organizationId: Long,
    val role: String
)