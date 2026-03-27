package com.iksystem.`ik-common`.auth.controller

import com.iksystem.`ik-common`.auth.dto.AuthResponse
import com.iksystem.`ik-common`.auth.dto.LoginRequest
import com.iksystem.`ik-common`.auth.dto.RefreshRequest
import com.iksystem.`ik-common`.auth.dto.RegisterRequest
import com.iksystem.`ik-common`.auth.service.AuthService
import com.iksystem.`ik-common`.security.AuthenticatedUser
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(private val authService: AuthService) {
    @PostMapping("/register")
    fun register(
        @Valid @RequestBody request: RegisterRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<AuthResponse> {
        val response = authService.register(
            request,
            ipAddress = httpRequest.remoteAddr,
            userAgent = httpRequest.getHeader("User-Agent"),
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<AuthResponse> {
        val response = authService.login(
            request,
            ipAddress = httpRequest.remoteAddr,
            userAgent = httpRequest.getHeader("User-Agent"),
        )
        return ResponseEntity.ok(response)
    }

    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody request: RefreshRequest): ResponseEntity<AuthResponse> {
        val response = authService.refresh(request)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/logout")
    fun logout(@AuthenticationPrincipal auth: AuthenticatedUser): ResponseEntity<Void> {
        authService.logout(auth.userId)
        return ResponseEntity.noContent().build()
    }
}