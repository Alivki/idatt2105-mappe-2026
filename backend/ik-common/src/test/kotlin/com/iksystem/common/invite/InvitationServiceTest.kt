package com.iksystem.common.invite.service

import com.iksystem.common.auth.dto.AuthResponse
import com.iksystem.common.exception.BadRequestException
import com.iksystem.common.invite.dto.InviteUserRequest
import com.iksystem.common.invite.model.Invitation
import com.iksystem.common.invite.repository.InvitationRepository
import com.iksystem.common.membership.model.Membership
import com.iksystem.common.membership.repository.MembershipRepository
import com.iksystem.common.organization.model.Organization
import com.iksystem.common.organization.repository.OrganizationRepository
import com.iksystem.common.resend.service.ResendService
import com.iksystem.common.security.AuthenticatedUser
import com.iksystem.common.security.JwtService
import com.iksystem.common.session.model.Session
import com.iksystem.common.session.repository.SessionRepository
import com.iksystem.common.token.model.RefreshToken
import com.iksystem.common.token.repository.RefreshTokenRepository
import com.iksystem.common.user.model.Role
import com.iksystem.common.user.model.User
import com.iksystem.common.user.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Optional

class InvitationServiceTest {

    private lateinit var invitationRepository: InvitationRepository
    private lateinit var userRepository: UserRepository
    private lateinit var membershipRepository: MembershipRepository
    private lateinit var organizationRepository: OrganizationRepository
    private lateinit var resendService: ResendService
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var jwtService: JwtService
    private lateinit var refreshTokenRepository: RefreshTokenRepository
    private lateinit var sessionRepository: SessionRepository
    private lateinit var service: InvitationService

    @BeforeEach
    fun setUp() {
        invitationRepository = mock(InvitationRepository::class.java)
        userRepository = mock(UserRepository::class.java)
        membershipRepository = mock(MembershipRepository::class.java)
        organizationRepository = mock(OrganizationRepository::class.java)
        resendService = mock(ResendService::class.java)
        passwordEncoder = mock(PasswordEncoder::class.java)
        jwtService = mock(JwtService::class.java)
        refreshTokenRepository = mock(RefreshTokenRepository::class.java)
        sessionRepository = mock(SessionRepository::class.java)

        service = InvitationService(
            invitationRepository = invitationRepository,
            userRepository = userRepository,
            membershipRepository = membershipRepository,
            organizationRepository = organizationRepository,
            resendService = resendService,
            passwordEncoder = passwordEncoder,
            jwtService = jwtService,
            refreshTokenRepository = refreshTokenRepository,
            sessionRepository = sessionRepository,
            accessTokenExpiration = 900000L,
            refreshTokenExpiration = 604800000L,
        )
    }

    @Test
    fun `createInvite normalizes email saves invitation and sends email`() {
        val auth = managerAuth()
        val request = InviteUserRequest(
            email = "  TEST@Example.com  ",
            role = "EMPLOYEE"
        )
        val org = organization()

        `when`(userRepository.findByEmail("test@example.com")).thenReturn(null)
        `when`(invitationRepository.existsByEmailAndOrganizationIdAndAcceptedAtIsNull("test@example.com", 1L))
            .thenReturn(false)
        `when`(organizationRepository.findById(1L)).thenReturn(Optional.of(org))
        `when`(invitationRepository.save(any(Invitation::class.java))).thenAnswer { it.arguments[0] }

        service.createInvite(request, auth)

        val captor = ArgumentCaptor.forClass(Invitation::class.java)
        verify(invitationRepository).save(captor.capture())

        assertEquals(1L, captor.value.organizationId)
        assertEquals("test@example.com", captor.value.email)
        assertEquals("EMPLOYEE", captor.value.role)
        assertEquals(10L, captor.value.invitedById)
        assertNotNull(captor.value.token)
        assertTrue(captor.value.expiresAt.isAfter(Instant.now().plus(6, ChronoUnit.DAYS)))

        verify(resendService).sendInviteEmail("test@example.com", "Test Org", captor.value.token)
    }

    @Test
    fun `createInvite throws when existing user is already member`() {
        val auth = managerAuth()
        val existingUser = user(id = 99L, email = "member@example.com")

        `when`(userRepository.findByEmail("member@example.com")).thenReturn(existingUser)
        `when`(membershipRepository.existsByUserIdAndOrganizationId(99L, 1L)).thenReturn(true)

        assertThrows<BadRequestException> {
            service.createInvite(
                InviteUserRequest(email = "member@example.com", role = "EMPLOYEE"),
                auth
            )
        }

        verify(invitationRepository, never()).save(any(Invitation::class.java))
    }

    @Test
    fun `createInvite throws when pending invitation already exists`() {
        val auth = managerAuth()

        `when`(userRepository.findByEmail("pending@example.com")).thenReturn(null)
        `when`(invitationRepository.existsByEmailAndOrganizationIdAndAcceptedAtIsNull("pending@example.com", 1L))
            .thenReturn(true)

        assertThrows<BadRequestException> {
            service.createInvite(
                InviteUserRequest(email = "pending@example.com", role = "EMPLOYEE"),
                auth
            )
        }
    }

    @Test
    fun `getValidInvite returns public invite details`() {
        val invite = invitation(
            email = "user@example.com",
            organizationId = 1L,
            expiresAt = Instant.now().plus(1, ChronoUnit.DAYS),
            acceptedAt = null
        )
        val org = organization(name = "Org Name")

        `when`(invitationRepository.findByToken("valid-token")).thenReturn(invite)
        `when`(organizationRepository.findById(1L)).thenReturn(Optional.of(org))
        `when`(userRepository.findByEmail("user@example.com")).thenReturn(user(email = "user@example.com"))

        val result = service.getValidInvite("valid-token")

        assertEquals("user@example.com", result.email)
        assertEquals("Org Name", result.organizationName)
        assertTrue(result.existingUser)
    }

    @Test
    fun `getValidInvite throws when token is invalid`() {
        `when`(invitationRepository.findByToken("missing")).thenReturn(null)

        assertThrows<BadRequestException> {
            service.getValidInvite("missing")
        }
    }

    @Test
    fun `getValidInvite throws when invitation expired`() {
        val invite = invitation(expiresAt = Instant.now().minusSeconds(10))

        `when`(invitationRepository.findByToken("expired")).thenReturn(invite)

        assertThrows<BadRequestException> {
            service.getValidInvite("expired")
        }
    }

    @Test
    fun `acceptInvitation creates new user membership tokens and marks invite accepted`() {
        val invite = invitation(
            email = "new@example.com",
            role = "MANAGER",
            organizationId = 1L,
            expiresAt = Instant.now().plus(1, ChronoUnit.DAYS),
            acceptedAt = null
        )
        val org = organization(id = 1L, name = "Test Org")
        val savedUser = user(id = 55L, email = "new@example.com", fullName = "New User")
        val membership = membership(user = savedUser, organization = org, role = Role.MANAGER)

        `when`(invitationRepository.findByToken("token")).thenReturn(invite)
        `when`(userRepository.findByEmail("new@example.com")).thenReturn(null)
        `when`(passwordEncoder.encode("secret123")).thenReturn("encoded-secret")
        `when`(userRepository.save(any(User::class.java))).thenReturn(savedUser)
        `when`(organizationRepository.findById(1L)).thenReturn(Optional.of(org))
        `when`(membershipRepository.save(any(Membership::class.java))).thenReturn(membership)
        `when`(invitationRepository.save(any(Invitation::class.java))).thenAnswer { it.arguments[0] }
        `when`(jwtService.generateAccessToken(savedUser, membership)).thenReturn("access-token")
        `when`(refreshTokenRepository.save(any(RefreshToken::class.java))).thenAnswer { it.arguments[0] }
        `when`(sessionRepository.save(any(Session::class.java))).thenAnswer { it.arguments[0] }

        val result = service.acceptInvitation(
            token = "token",
            password = "secret123",
            fullName = "New User",
            phoneNumber = "+4712345678"
        )

        assertEquals("access-token", result.accessToken)
        assertNotNull(result.refreshToken)
        assertEquals(900L, result.expiresIn)
        assertEquals(1L, result.organizationId)
        assertEquals("MANAGER", result.role)
        assertEquals("new@example.com", result.user.email)
        assertTrue(invite.acceptedAt != null)

        val userCaptor = ArgumentCaptor.forClass(User::class.java)
        verify(userRepository).save(userCaptor.capture())
        assertEquals("new@example.com", userCaptor.value.email)
        assertEquals("encoded-secret", userCaptor.value.password)
        assertEquals("New User", userCaptor.value.fullName)
        assertEquals("+4712345678", userCaptor.value.phoneNumber)
        assertTrue(userCaptor.value.active)
    }

    @Test
    fun `acceptInvitation throws for new user when required fields missing`() {
        val invite = invitation(
            email = "new@example.com",
            expiresAt = Instant.now().plus(1, ChronoUnit.DAYS)
        )

        `when`(invitationRepository.findByToken("token")).thenReturn(invite)
        `when`(userRepository.findByEmail("new@example.com")).thenReturn(null)

        assertThrows<BadRequestException> {
            service.acceptInvitation("token", null, null, null)
        }
    }

    @Test
    fun `acceptInvitation for existing user validates password and reuses account`() {
        val invite = invitation(
            email = "existing@example.com",
            role = "EMPLOYEE",
            organizationId = 1L,
            expiresAt = Instant.now().plus(1, ChronoUnit.DAYS)
        )
        val existingUser = user(id = 77L, email = "existing@example.com", password = "stored-hash")
        val org = organization(id = 1L)
        val membership = membership(user = existingUser, organization = org, role = Role.EMPLOYEE)

        `when`(invitationRepository.findByToken("token")).thenReturn(invite)
        `when`(userRepository.findByEmail("existing@example.com")).thenReturn(existingUser)
        `when`(passwordEncoder.matches("correct-password", "stored-hash")).thenReturn(true)
        `when`(organizationRepository.findById(1L)).thenReturn(Optional.of(org))
        `when`(membershipRepository.save(any(Membership::class.java))).thenReturn(membership)
        `when`(invitationRepository.save(any(Invitation::class.java))).thenAnswer { it.arguments[0] }
        `when`(jwtService.generateAccessToken(existingUser, membership)).thenReturn("existing-access")
        `when`(refreshTokenRepository.save(any(RefreshToken::class.java))).thenAnswer { it.arguments[0] }
        `when`(sessionRepository.save(any(Session::class.java))).thenAnswer { it.arguments[0] }

        val result = service.acceptInvitation(
            token = "token",
            password = "correct-password",
            fullName = null,
            phoneNumber = null
        )

        assertEquals("existing-access", result.accessToken)
        assertEquals("EMPLOYEE", result.role)
        verify(userRepository, never()).save(any(User::class.java))
    }

    @Test
    fun `acceptInvitation throws for existing user with wrong password`() {
        val invite = invitation(
            email = "existing@example.com",
            expiresAt = Instant.now().plus(1, ChronoUnit.DAYS)
        )
        val existingUser = user(id = 77L, email = "existing@example.com", password = "stored-hash")

        `when`(invitationRepository.findByToken("token")).thenReturn(invite)
        `when`(userRepository.findByEmail("existing@example.com")).thenReturn(existingUser)
        `when`(passwordEncoder.matches("wrong-password", "stored-hash")).thenReturn(false)

        assertThrows<BadRequestException> {
            service.acceptInvitation("token", "wrong-password", null, null)
        }
    }

    @Test
    fun `acceptInvitation throws when invitation already accepted`() {
        val invite = invitation(
            email = "done@example.com",
            expiresAt = Instant.now().plus(1, ChronoUnit.DAYS),
            acceptedAt = Instant.now()
        )

        `when`(invitationRepository.findByToken("token")).thenReturn(invite)

        assertThrows<BadRequestException> {
            service.acceptInvitation("token", "password123", "Name", "123")
        }
    }

    private fun managerAuth() = AuthenticatedUser(
        10L,
        1L,
        "MANAGER"
    )

    private fun invitation(
        id: Long = 1L,
        organizationId: Long = 1L,
        email: String = "user@example.com",
        role: String = "EMPLOYEE",
        token: String = "token-value",
        invitedById: Long = 10L,
        expiresAt: Instant = Instant.now().plus(1, ChronoUnit.DAYS),
        acceptedAt: Instant? = null
    ) = Invitation(
        id = id,
        organizationId = organizationId,
        email = email,
        role = role,
        token = token,
        invitedById = invitedById,
        expiresAt = expiresAt,
        acceptedAt = acceptedAt
    )

    private fun user(
        id: Long = 10L,
        email: String = "user@example.com",
        password: String = "hashed-password",
        fullName: String = "Test User",
        phoneNumber: String = "+4712345678",
        active: Boolean = true
    ) = User(
        id = id,
        email = email,
        password = password,
        fullName = fullName,
        phoneNumber = phoneNumber,
        active = active
    )

    private fun organization(
        id: Long = 1L,
        name: String = "Test Org"
    ) = Organization(
        id = id,
        name = name
    )

    private fun membership(
        user: User,
        organization: Organization,
        role: Role
    ) = Membership(
        id = 1L,
        user = user,
        organization = organization,
        role = role
    )
}