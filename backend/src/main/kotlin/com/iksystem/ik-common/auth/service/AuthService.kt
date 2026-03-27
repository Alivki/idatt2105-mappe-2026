package com.iksystem.`ik-common`.auth.service

import com.ik.ikcommon.exception.ConflictException
import com.iksystem.`ik-common`.auth.dto.AuthResponse
import com.iksystem.`ik-common`.auth.dto.RegisterRequest
import com.iksystem.`ik-common`.security.JwtService
import jakarta.transaction.Transactional
import org.aspectj.weaver.loadtime.Agent
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder

class AuthService(
    private val userRepository: UserRepository,
    private val organizationRepository: OrganizationRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val sessionRepository: SessionRepository,
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${jwt.access-token-expiration}") private val accessTokenExpiration: Long,
    @Value("\${jwt.refresh-token-expiration}") private val refreshTokenExpiration: Long,
) {

}