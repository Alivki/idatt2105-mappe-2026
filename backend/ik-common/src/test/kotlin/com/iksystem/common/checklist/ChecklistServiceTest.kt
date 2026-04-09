package com.iksystem.common.checklist.service

import com.iksystem.common.checklist.dto.ChecklistStatus
import com.iksystem.common.checklist.dto.CreateChecklistItemRequest
import com.iksystem.common.checklist.dto.CreateChecklistRequest
import com.iksystem.common.checklist.dto.SetChecklistCompletionRequest
import com.iksystem.common.checklist.dto.UpdateChecklistItemRequest
import com.iksystem.common.checklist.dto.UpdateChecklistRequest
import com.iksystem.common.checklist.model.Checklist
import com.iksystem.common.checklist.model.ChecklistFrequency
import com.iksystem.common.checklist.model.ChecklistItem
import com.iksystem.common.checklist.repository.ChecklistCompletionRepository
import com.iksystem.common.checklist.repository.ChecklistItemRepository
import com.iksystem.common.checklist.repository.ChecklistRepository
import com.iksystem.common.user.repository.UserRepository
import com.iksystem.common.exception.BadRequestException
import com.iksystem.common.exception.ForbiddenException
import com.iksystem.common.exception.NotFoundException
import com.iksystem.common.security.AuthenticatedUser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.Instant

class ChecklistServiceTest {

    private lateinit var checklistRepository: ChecklistRepository
    private lateinit var checklistItemRepository: ChecklistItemRepository
    private lateinit var checklistCompletionRepository: ChecklistCompletionRepository
    private lateinit var userRepository: UserRepository
    private lateinit var service: ChecklistService

    @BeforeEach
    fun setUp() {
        checklistRepository = mock(ChecklistRepository::class.java)
        checklistItemRepository = mock(ChecklistItemRepository::class.java)
        checklistCompletionRepository = mock(ChecklistCompletionRepository::class.java)
        userRepository = mock(UserRepository::class.java)

        service = ChecklistService(
            checklistRepository,
            checklistItemRepository,
            checklistCompletionRepository,
            userRepository
        )
    }

    @Test
    fun `list maps checklist statuses correctly`() {
        val auth = managerAuth()

        val checklist1 = checklist(id = 1L, name = "Empty checklist")
        val checklist2 = checklist(id = 2L, name = "Not started checklist")
        val checklist3 = checklist(id = 3L, name = "In progress checklist")
        val checklist4 = checklist(id = 4L, name = "Completed checklist")

        `when`(checklistRepository.findAllByOrganizationIdOrderByCreatedAtDesc(1L)).thenReturn(
            listOf(checklist1, checklist2, checklist3, checklist4)
        )
        `when`(checklistItemRepository.findAllByChecklistIdOrderByIdAsc(1L)).thenReturn(emptyList())
        `when`(checklistItemRepository.findAllByChecklistIdOrderByIdAsc(2L)).thenReturn(
            listOf(
                item(id = 21L, checklist = checklist2, completed = false),
                item(id = 22L, checklist = checklist2, completed = false),
            )
        )
        `when`(checklistItemRepository.findAllByChecklistIdOrderByIdAsc(3L)).thenReturn(
            listOf(
                item(id = 31L, checklist = checklist3, completed = true),
                item(id = 32L, checklist = checklist3, completed = false),
            )
        )
        `when`(checklistItemRepository.findAllByChecklistIdOrderByIdAsc(4L)).thenReturn(
            listOf(
                item(id = 41L, checklist = checklist4, completed = true),
                item(id = 42L, checklist = checklist4, completed = true),
            )
        )

        val result = service.list(auth)

        assertEquals(4, result.size)

        assertEquals(ChecklistStatus.NOT_STARTED, result[0].status)
        assertEquals(0, result[0].itemCount)
        assertEquals(0, result[0].completedItemCount)

        assertEquals(ChecklistStatus.NOT_STARTED, result[1].status)
        assertEquals(2, result[1].itemCount)
        assertEquals(0, result[1].completedItemCount)

        assertEquals(ChecklistStatus.IN_PROGRESS, result[2].status)
        assertEquals(2, result[2].itemCount)
        assertEquals(1, result[2].completedItemCount)

        assertEquals(ChecklistStatus.COMPLETED, result[3].status)
        assertEquals(2, result[3].itemCount)
        assertEquals(2, result[3].completedItemCount)
    }

    @Test
    fun `stats returns active checklists and total items`() {
        `when`(checklistRepository.countActiveOpenByOrganizationId(1L)).thenReturn(3L)
        `when`(checklistItemRepository.countAllByOrganizationId(1L)).thenReturn(12L)

        val result = service.stats(managerAuth())

        assertEquals(3L, result.activeChecklists)
        assertEquals(12L, result.totalChecklistItems)
    }

    @Test
    fun `createChecklist trims fields and returns empty items`() {
        val request = CreateChecklistRequest(
            name = "  Opening checklist  ",
            description = "  Daily startup tasks  ",
            frequency = ChecklistFrequency.DAILY
        )

        `when`(checklistRepository.save(any(Checklist::class.java))).thenAnswer { invocation ->
            val saved = invocation.arguments[0] as Checklist
            saved.copy(id = 99L)
        }

        val result = service.createChecklist(request, managerAuth())

        assertEquals(99L, result.id)
        assertEquals("Opening checklist", result.name)
        assertEquals("Daily startup tasks", result.description)
        assertEquals(ChecklistFrequency.DAILY, result.frequency)
        assertEquals(0, result.itemCount)
        assertEquals(ChecklistStatus.NOT_STARTED, result.status)

        val captor = ArgumentCaptor.forClass(Checklist::class.java)
        verify(checklistRepository).save(captor.capture())
        assertEquals("Opening checklist", captor.value.name)
        assertEquals("Daily startup tasks", captor.value.description)
        assertEquals(1L, captor.value.organizationId)
    }

    @Test
    fun `updateChecklist throws when no fields are provided`() {
        `when`(checklistRepository.findByIdAndOrganizationId(5L, 1L)).thenReturn(checklist(id = 5L))

        assertThrows(BadRequestException::class.java) {
            service.updateChecklist(5L, UpdateChecklistRequest(), managerAuth())
        }

        verify(checklistRepository, never()).save(any(Checklist::class.java))
    }

    @Test
    fun `updateChecklist updates trimmed fields and returns items`() {
        val existing = checklist(
            id = 5L,
            name = "Old name",
            description = "Old description",
            frequency = ChecklistFrequency.WEEKLY,
            active = true
        )
        val request = UpdateChecklistRequest(
            name = "  Updated name  ",
            description = "  Updated description  ",
            frequency = ChecklistFrequency.MONTHLY,
            active = false
        )
        val updatedItem = item(id = 51L, checklist = existing, completed = true)

        `when`(checklistRepository.findByIdAndOrganizationId(5L, 1L)).thenReturn(existing)
        `when`(checklistRepository.save(any(Checklist::class.java))).thenAnswer { it.arguments[0] }
        `when`(checklistItemRepository.findAllByChecklistIdOrderByIdAsc(5L)).thenReturn(listOf(updatedItem))

        val result = service.updateChecklist(5L, request, managerAuth())

        assertEquals("Updated name", result.name)
        assertEquals("Updated description", result.description)
        assertEquals(ChecklistFrequency.MONTHLY, result.frequency)
        assertEquals(false, result.active)
        assertEquals(1, result.itemCount)
        assertEquals(1, result.completedItemCount)
        assertEquals(ChecklistStatus.COMPLETED, result.status)
    }

    @Test
    fun `deleteChecklist deletes existing checklist`() {
        val checklist = checklist(id = 7L)
        `when`(checklistRepository.findByIdAndOrganizationId(7L, 1L)).thenReturn(checklist)

        service.deleteChecklist(7L, managerAuth())

        verify(checklistRepository).delete(checklist)
    }

    @Test
    fun `createItem trims fields and sets completedAt when completed true`() {
        val checklist = checklist(id = 8L)
        val request = CreateChecklistItemRequest(
            title = "  Check fridge temps  ",
            description = "  Record morning values  ",
            completed = true
        )

        `when`(checklistRepository.findByIdAndOrganizationId(8L, 1L)).thenReturn(checklist)
        `when`(checklistItemRepository.save(any(ChecklistItem::class.java))).thenAnswer { invocation ->
            val saved = invocation.arguments[0] as ChecklistItem
            saved.copy(id = 88L)
        }

        val result = service.createItem(8L, request, managerAuth())

        assertEquals(88L, result.id)
        assertEquals("Check fridge temps", result.title)
        assertEquals("Record morning values", result.description)
        assertTrue(result.completed)
        assertNotNull(result.completedAt)
    }

    @Test
    fun `updateItem throws when employee tries to edit title or description`() {
        val checklist = checklist(id = 9L)
        val request = UpdateChecklistItemRequest(
            title = "Changed title",
            description = null,
            completed = null
        )

        `when`(checklistRepository.findByIdAndOrganizationId(9L, 1L)).thenReturn(checklist)

        assertThrows(ForbiddenException::class.java) {
            service.updateItem(9L, 90L, request, employeeAuth())
        }
    }

    @Test
    fun `updateItem throws when no fields are provided`() {
        val checklist = checklist(id = 9L)
        `when`(checklistRepository.findByIdAndOrganizationId(9L, 1L)).thenReturn(checklist)

        assertThrows(BadRequestException::class.java) {
            service.updateItem(9L, 90L, UpdateChecklistItemRequest(), managerAuth())
        }
    }

    @Test
    fun `updateItem can toggle completion for employee and clear completedAt`() {
        val checklist = checklist(id = 10L)
        val existingItem = item(
            id = 100L,
            checklist = checklist,
            title = "Existing title",
            description = "Existing desc",
            completed = true,
            completedAt = Instant.parse("2026-01-01T10:00:00Z")
        )
        val request = UpdateChecklistItemRequest(
            completed = false
        )

        `when`(checklistRepository.findByIdAndOrganizationId(10L, 1L)).thenReturn(checklist)
        `when`(checklistItemRepository.findByIdAndChecklistId(100L, 10L)).thenReturn(existingItem)
        `when`(checklistItemRepository.save(any(ChecklistItem::class.java))).thenAnswer { it.arguments[0] }

        val result = service.updateItem(10L, 100L, request, employeeAuth())

        assertEquals("Existing title", result.title)
        assertEquals("Existing desc", result.description)
        assertEquals(false, result.completed)
        assertNull(result.completedAt)
    }

    @Test
    fun `updateItem lets manager update text fields and set completedAt`() {
        val checklist = checklist(id = 11L)
        val existingItem = item(
            id = 110L,
            checklist = checklist,
            title = "Old title",
            description = "Old desc",
            completed = false,
            completedAt = null
        )
        val request = UpdateChecklistItemRequest(
            title = "  New title  ",
            description = "  New desc  ",
            completed = true
        )

        `when`(checklistRepository.findByIdAndOrganizationId(11L, 1L)).thenReturn(checklist)
        `when`(checklistItemRepository.findByIdAndChecklistId(110L, 11L)).thenReturn(existingItem)
        `when`(checklistItemRepository.save(any(ChecklistItem::class.java))).thenAnswer { it.arguments[0] }

        val result = service.updateItem(11L, 110L, request, managerAuth())

        assertEquals("New title", result.title)
        assertEquals("New desc", result.description)
        assertEquals(true, result.completed)
        assertNotNull(result.completedAt)
    }

    @Test
    fun `deleteItem deletes existing item`() {
        val checklist = checklist(id = 12L)
        val item = item(id = 120L, checklist = checklist)

        `when`(checklistRepository.findByIdAndOrganizationId(12L, 1L)).thenReturn(checklist)
        `when`(checklistItemRepository.findByIdAndChecklistId(120L, 12L)).thenReturn(item)

        service.deleteItem(12L, 120L, managerAuth())

        verify(checklistItemRepository).delete(item)
    }

    @Test
    fun `setChecklistCompletion updates all items and returns refreshed checklist`() {
        val checklist = checklist(id = 13L, name = "Close checklist")
        val items = listOf(
            item(id = 131L, checklist = checklist, completed = true, completedAt = Instant.parse("2026-01-01T10:00:00Z")),
            item(id = 132L, checklist = checklist, completed = true, completedAt = Instant.parse("2026-01-01T10:01:00Z")),
        )

        `when`(checklistRepository.findByIdAndOrganizationId(13L, 1L)).thenReturn(checklist)
        `when`(checklistItemRepository.findAllByChecklistIdOrderByIdAsc(13L)).thenReturn(items)

        val result = service.setChecklistCompletion(
            13L,
            SetChecklistCompletionRequest(completed = true),
            employeeAuth()
        )

        assertEquals(2, result.itemCount)
        assertEquals(2, result.completedItemCount)
        assertEquals(ChecklistStatus.COMPLETED, result.status)
    }

    @Test
    fun `missing checklist throws not found`() {
        `when`(checklistRepository.findByIdAndOrganizationId(404L, 1L)).thenReturn(null)

        assertThrows(NotFoundException::class.java) {
            service.deleteChecklist(404L, managerAuth())
        }
    }

    private fun managerAuth() = AuthenticatedUser(
        10L,
        1L,
        "MANAGER"
    )

    private fun employeeAuth() = AuthenticatedUser(
        20L,
        1L,
        "EMPLOYEE"
    )

    private fun checklist(
        id: Long = 1L,
        name: String = "Checklist",
        description: String? = "Description",
        frequency: ChecklistFrequency = ChecklistFrequency.DAILY,
        active: Boolean = true,
        source: String = "MANUAL"
    ) = Checklist(
        id = id,
        organizationId = 1L,
        name = name,
        description = description,
        frequency = frequency,
        active = active,
        source = source
    )

    private fun item(
        id: Long = 1L,
        checklist: Checklist,
        title: String = "Item",
        description: String? = "Item description",
        completed: Boolean = false,
        completedAt: Instant? = null
    ) = ChecklistItem(
        id = id,
        checklist = checklist,
        title = title,
        description = description,
        sortOrder = 0,
        completed = completed,
        completedAt = completedAt
    )

}