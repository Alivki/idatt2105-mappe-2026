package com.iksystem.common.notifications.service

import com.iksystem.common.membership.model.Membership
import com.iksystem.common.membership.repository.MembershipRepository
import com.iksystem.common.notifications.model.Notification
import com.iksystem.common.notifications.model.NotificationType
import com.iksystem.common.notifications.model.ReferenceType
import com.iksystem.common.notifications.repository.NotificationsRepository
import com.iksystem.common.organization.model.Organization
import com.iksystem.common.resend.service.ResendService
import com.iksystem.common.user.model.Role
import com.iksystem.common.user.model.User
import com.iksystem.common.user.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.any
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import java.util.Optional

class NotificationsServiceTest {

    private lateinit var notificationsRepository: NotificationsRepository
    private lateinit var userRepository: UserRepository
    private lateinit var membershipRepository: MembershipRepository
    private lateinit var resendService: ResendService
    private lateinit var service: NotificationsService

    @BeforeEach
    fun setUp() {
        notificationsRepository = mock(NotificationsRepository::class.java)
        userRepository = mock(UserRepository::class.java)
        membershipRepository = mock(MembershipRepository::class.java)
        resendService = mock(ResendService::class.java)

        service = NotificationsService(
            notificationsRepository = notificationsRepository,
            userRepository = userRepository,
            membershipRepository = membershipRepository,
            resendService = resendService
        )
    }

    @Test
    fun `send saves notification and sends email when enabled`() {
        val recipient = user(id = 10L, email = "user@example.com")

        `when`(notificationsRepository.save(any(Notification::class.java)))
            .thenAnswer { invocation -> invocation.arguments[0] as Notification }
        `when`(userRepository.findById(10L)).thenReturn(Optional.of(recipient))

        service.send(
            organizationId = 1L,
            recipientUserId = 10L,
            type = NotificationType.SYSTEM_ALERT,
            title = "Test title",
            message = "Test message",
            referenceType = ReferenceType.DEVIATION,
            referenceId = 99L,
            sendEmail = true
        )

        val captor = ArgumentCaptor.forClass(Notification::class.java)
        verify(notificationsRepository, times(2)).save(captor.capture())

        assertEquals(1L, captor.allValues[0].organizationId)
        assertEquals(10L, captor.allValues[0].recipientUserId)
        assertEquals("Test title", captor.allValues[0].title)
        assertEquals("Test message", captor.allValues[0].message)
        assertEquals(NotificationType.SYSTEM_ALERT, captor.allValues[0].type)
        assertEquals(ReferenceType.DEVIATION, captor.allValues[0].referenceType)
        assertEquals(99L, captor.allValues[0].referenceId)

        assertTrue(captor.allValues[1].emailSent)

        verify(resendService).sendNotificationEmail("user@example.com", "Test title", "Test message")
    }

    @Test
    fun `send saves once and does not email when sendEmail is false`() {
        `when`(notificationsRepository.save(any(Notification::class.java)))
            .thenAnswer { invocation -> invocation.arguments[0] as Notification }

        service.send(
            organizationId = 1L,
            recipientUserId = 10L,
            type = NotificationType.SYSTEM_ALERT,
            title = "No email",
            message = "Body",
            sendEmail = false
        )

        verify(notificationsRepository, times(1)).save(any(Notification::class.java))
        verifyNoInteractions(userRepository)
        verifyNoInteractions(resendService)
    }

    @Test
    fun `send skips email when user has empty email`() {
        val recipient = user(id = 10L, email = "")

        `when`(notificationsRepository.save(any(Notification::class.java)))
            .thenAnswer { invocation -> invocation.arguments[0] as Notification }
        `when`(userRepository.findById(10L)).thenReturn(Optional.of(recipient))

        service.send(
            organizationId = 1L,
            recipientUserId = 10L,
            type = NotificationType.SYSTEM_ALERT,
            title = "No address",
            message = "Body",
            sendEmail = true
        )

        verify(notificationsRepository, times(1)).save(any(Notification::class.java))
        verify(userRepository).findById(10L)
        verifyNoInteractions(resendService)
    }

    @Test
    fun `send swallows email exception and does not mark emailSent`() {
        val recipient = user(id = 10L, email = "user@example.com")

        `when`(notificationsRepository.save(any(Notification::class.java)))
            .thenAnswer { invocation -> invocation.arguments[0] as Notification }
        `when`(userRepository.findById(10L)).thenReturn(Optional.of(recipient))
        doThrow(RuntimeException("mail fail")).`when`(resendService)
            .sendNotificationEmail("user@example.com", "Title", "Message")

        service.send(
            organizationId = 1L,
            recipientUserId = 10L,
            type = NotificationType.SYSTEM_ALERT,
            title = "Title",
            message = "Message",
            sendEmail = true
        )

        val captor = ArgumentCaptor.forClass(Notification::class.java)
        verify(notificationsRepository, times(1)).save(captor.capture())
        assertFalse(captor.value.emailSent)
    }

    @Test
    fun `sendToOrgAdminsAndManagers sends to admin and manager only`() {
        val orgId = 1L
        val admin = membership(user(id = 1L), orgId, Role.ADMIN)
        val manager = membership(user(id = 2L), orgId, Role.MANAGER)
        val employee = membership(user(id = 3L), orgId, Role.EMPLOYEE)

        `when`(membershipRepository.findAllByOrganizationId(orgId))
            .thenReturn(listOf(admin, manager, employee))
        `when`(notificationsRepository.save(any(Notification::class.java)))
            .thenAnswer { invocation -> invocation.arguments[0] as Notification }

        service.sendToOrgAdminsAndManagers(
            organizationId = orgId,
            type = NotificationType.DEVIATION_CREATED,
            title = "Deviation",
            message = "Created",
            sendEmail = false
        )

        val captor = ArgumentCaptor.forClass(Notification::class.java)
        verify(notificationsRepository, times(2)).save(captor.capture())

        val recipients = captor.allValues.map { it.recipientUserId }.sorted()
        assertEquals(listOf(1L, 2L), recipients)
    }

    @Test
    fun `sendToOrgAdmins sends only admins`() {
        val orgId = 1L
        val admin = membership(user(id = 1L), orgId, Role.ADMIN)
        val manager = membership(user(id = 2L), orgId, Role.MANAGER)

        `when`(membershipRepository.findAllByOrganizationId(orgId))
            .thenReturn(listOf(admin, manager))
        `when`(notificationsRepository.save(any(Notification::class.java)))
            .thenAnswer { invocation -> invocation.arguments[0] as Notification }

        service.sendToOrgAdmins(
            organizationId = orgId,
            type = NotificationType.SYSTEM_ALERT,
            title = "Admins",
            message = "Only admins",
            sendEmail = false
        )

        val captor = ArgumentCaptor.forClass(Notification::class.java)
        verify(notificationsRepository, times(1)).save(captor.capture())
        assertEquals(1L, captor.value.recipientUserId)
    }

    @Test
    fun `sendToOrgManagers sends only managers`() {
        val orgId = 1L
        val admin = membership(user(id = 1L), orgId, Role.ADMIN)
        val manager = membership(user(id = 2L), orgId, Role.MANAGER)

        `when`(membershipRepository.findAllByOrganizationId(orgId))
            .thenReturn(listOf(admin, manager))
        `when`(notificationsRepository.save(any(Notification::class.java)))
            .thenAnswer { invocation -> invocation.arguments[0] as Notification }

        service.sendToOrgManagers(
            organizationId = orgId,
            type = NotificationType.TRAINING_EXPIRY,
            title = "Managers",
            message = "Only managers",
            sendEmail = false
        )

        val captor = ArgumentCaptor.forClass(Notification::class.java)
        verify(notificationsRepository, times(1)).save(captor.capture())
        assertEquals(2L, captor.value.recipientUserId)
    }

    private fun user(
        id: Long = 10L,
        email: String = "user@example.com",
        password: String = "hashed",
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
        organizationId: Long,
        role: Role
    ) = Membership(
        id = 1L,
        user = user,
        organization = organization(id = organizationId),
        role = role
    )
}