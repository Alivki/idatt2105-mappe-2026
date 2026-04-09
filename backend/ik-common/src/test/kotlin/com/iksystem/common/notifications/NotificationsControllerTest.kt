package com.iksystem.common.notifications.controller

import com.iksystem.common.notifications.model.Notification
import com.iksystem.common.notifications.model.NotificationType
import com.iksystem.common.notifications.model.ReferenceType
import com.iksystem.common.notifications.repository.NotificationsRepository
import com.iksystem.common.security.AuthenticatedUser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.any
import org.mockito.Mockito.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.Instant
import java.util.Optional

class NotificationsControllerTest {

    private lateinit var notificationsRepository: NotificationsRepository
    private lateinit var controller: NotificationsController

    @BeforeEach
    fun setUp() {
        notificationsRepository = mock(NotificationsRepository::class.java)
        controller = NotificationsController(notificationsRepository)
    }

    @Test
    fun `list returns paged notifications response`() {
        val auth = auth()
        val page = PageImpl(
            listOf(
                notification(id = 1L, title = "A", isRead = false),
                notification(id = 2L, title = "B", isRead = true)
            )
        )

        `when`(
            notificationsRepository.findAllByRecipientUserIdAndOrganizationIdOrderByCreatedAtDesc(
                10L,
                1L,
                PageRequest.of(0, 20)
            )
        ).thenReturn(page)

        val result = controller.list(auth, 0, 20)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(2, result.body!!.content.size)
        assertEquals("A", result.body!!.content[0].title)
        assertFalse(result.body!!.content[0].isRead)
        assertEquals("B", result.body!!.content[1].title)
        assertTrue(result.body!!.content[1].isRead)
    }

    @Test
    fun `getUnreadCount returns count`() {
        val auth = auth()
        `when`(notificationsRepository.countByRecipientUserIdAndOrganizationIdAndIsReadFalse(10L, 1L))
            .thenReturn(5L)

        val result = controller.getUnreadCount(auth)

        assertEquals(200, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(5L, result.body!!.count)
    }

    @Test
    fun `markRead updates own notification in same org`() {
        val auth = auth()
        val notif = notification(
            id = 3L,
            organizationId = 1L,
            recipientUserId = 10L,
            isRead = false,
            readAt = null
        )

        `when`(notificationsRepository.findById(3L)).thenReturn(Optional.of(notif))
        `when`(notificationsRepository.save(any(Notification::class.java))).thenAnswer { it.arguments[0] }

        val result = controller.markRead(3L, auth)

        assertEquals(204, result.statusCode.value())
        assertNull(result.body)

        val captor = ArgumentCaptor.forClass(Notification::class.java)
        verify(notificationsRepository).save(captor.capture())
        assertTrue(captor.value.isRead)
        assertNotNull(captor.value.readAt)
    }

    @Test
    fun `markRead ignores notification for another user`() {
        val auth = auth()
        val notif = notification(
            id = 4L,
            organizationId = 1L,
            recipientUserId = 999L,
            isRead = false
        )

        `when`(notificationsRepository.findById(4L)).thenReturn(Optional.of(notif))

        val result = controller.markRead(4L, auth)

        assertEquals(204, result.statusCode.value())
        verify(notificationsRepository, never()).save(any(Notification::class.java))
    }

    @Test
    fun `markRead ignores notification for another org`() {
        val auth = auth()
        val notif = notification(
            id = 5L,
            organizationId = 99L,
            recipientUserId = 10L,
            isRead = false
        )

        `when`(notificationsRepository.findById(5L)).thenReturn(Optional.of(notif))

        val result = controller.markRead(5L, auth)

        assertEquals(204, result.statusCode.value())
        verify(notificationsRepository, never()).save(any(Notification::class.java))
    }

    @Test
    fun `markRead ignores missing notification`() {
        val auth = auth()
        `when`(notificationsRepository.findById(404L)).thenReturn(Optional.empty())

        val result = controller.markRead(404L, auth)

        assertEquals(204, result.statusCode.value())
        verify(notificationsRepository, never()).save(any(Notification::class.java))
    }

    @Test
    fun `markAllRead delegates and returns no content`() {
        val auth = auth()

        val result = controller.markAllRead(auth)

        assertEquals(204, result.statusCode.value())
        assertNull(result.body)
        verify(notificationsRepository).markAllAsRead(10L, 1L)
    }

    private fun auth() = AuthenticatedUser(
        10L,
        1L,
        "EMPLOYEE"
    )

    private fun notification(
        id: Long = 1L,
        organizationId: Long = 1L,
        recipientUserId: Long = 10L,
        title: String = "Title",
        message: String = "Message",
        type: NotificationType = NotificationType.SYSTEM_ALERT,
        referenceType: ReferenceType? = ReferenceType.DEVIATION,
        referenceId: Long? = 99L,
        isRead: Boolean = false,
        readAt: Instant? = null,
        emailSent: Boolean = false,
        createdAt: Instant = Instant.parse("2026-01-01T10:00:00Z")
    ) = Notification(
        id = id,
        organizationId = organizationId,
        recipientUserId = recipientUserId,
        title = title,
        message = message,
        type = type,
        referenceType = referenceType,
        referenceId = referenceId,
        isRead = isRead,
        readAt = readAt,
        emailSent = emailSent,
        createdAt = createdAt
    )
}