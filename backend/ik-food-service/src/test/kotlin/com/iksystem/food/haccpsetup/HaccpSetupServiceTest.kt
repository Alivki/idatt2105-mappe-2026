package com.iksystem.food.haccpsetup.service

import com.iksystem.common.checklist.model.Checklist
import com.iksystem.common.checklist.model.ChecklistItem
import com.iksystem.common.checklist.repository.ChecklistItemRepository
import com.iksystem.common.checklist.repository.ChecklistRepository
import com.iksystem.common.security.AuthenticatedUser
import com.iksystem.food.haccpsetup.dto.FoodProcess
import com.iksystem.food.haccpsetup.dto.FoodType
import com.iksystem.food.haccpsetup.dto.HaccpSetupRequest
import com.iksystem.food.haccpsetup.dto.TempEquipment
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.any
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class HaccpSetupServiceTest {

    private lateinit var checklistRepository: ChecklistRepository
    private lateinit var checklistItemRepository: ChecklistItemRepository
    private lateinit var service: HaccpSetupService

    @BeforeEach
    fun setUp() {
        checklistRepository = mock(ChecklistRepository::class.java)
        checklistItemRepository = mock(ChecklistItemRepository::class.java)

        service = HaccpSetupService(
            checklistRepository = checklistRepository,
            checklistItemRepository = checklistItemRepository
        )
    }

    @Test
    fun `generateChecklists deletes old wizard checklists and creates matching templates`() {
        val auth = AuthenticatedUser(
            10L,
            1L,
            "MANAGER"
        )

        val request = haccpSetupRequest(
            foodTypes = setOf(FoodType.ALLERGEN_CONTAINING),
            processes = setOf(FoodProcess.COOKING_HEAT_TREATMENT),
            temperatureEquipment = setOf(TempEquipment.REFRIGERATORS),
            servesVulnerableGroups = true,
            handlesHighRiskProducts = true
        )

        var checklistId = 100L
        var itemId = 1000L

        `when`(checklistRepository.save(any(Checklist::class.java))).thenAnswer { invocation ->
            val checklist = invocation.arguments[0] as Checklist
            checklist.copy(id = checklistId++)
        }

        `when`(checklistItemRepository.save(any(ChecklistItem::class.java))).thenAnswer { invocation ->
            val item = invocation.arguments[0] as ChecklistItem
            item.copy(id = itemId++)
        }

        val result = service.generateChecklists(request, auth)

        verify(checklistRepository).deleteAllByOrganizationIdAndSource(
            1L,
            HaccpSetupService.HACCP_WIZARD_SOURCE
        )

        assertTrue(result.generatedChecklists.isNotEmpty())
        assertTrue(result.summary.totalChecklists > 0)
        assertTrue(result.summary.totalItems > 0)

        assertTrue(result.generatedChecklists.any { it.name == "Daglig renhold" })
        assertTrue(result.generatedChecklists.any { it.name == "Temperaturkontroll – Kjøleskap" })
        assertTrue(result.generatedChecklists.any { it.name == "Tilberedningskontroll" })
        assertTrue(result.generatedChecklists.any { it.name == "Allergenkontroll" })
        assertTrue(result.generatedChecklists.any { it.name == "Kritiske kontrollpunkter (CCP)" })

        assertTrue(result.generatedChecklists.all { it.source == HaccpSetupService.HACCP_WIZARD_SOURCE })
        assertTrue(result.generatedChecklists.all { it.status.name == "NOT_STARTED" })
    }

    @Test
    fun `generateChecklists creates only always on templates when request has minimal options`() {
        val auth = AuthenticatedUser(
            10L,
            1L,
            "ADMIN"
        )

        val request = haccpSetupRequest(
            foodTypes = setOf(FoodType.SHELF_STABLE_ONLY),
            processes = emptySet(),
            temperatureEquipment = setOf(TempEquipment.NONE),
            servesVulnerableGroups = false,
            handlesHighRiskProducts = false
        )

        var checklistId = 1L
        var itemId = 1L

        `when`(checklistRepository.save(any(Checklist::class.java))).thenAnswer { invocation ->
            val checklist = invocation.arguments[0] as Checklist
            checklist.copy(id = checklistId++)
        }

        `when`(checklistItemRepository.save(any(ChecklistItem::class.java))).thenAnswer { invocation ->
            val item = invocation.arguments[0] as ChecklistItem
            item.copy(id = itemId++)
        }

        val result = service.generateChecklists(request, auth)

        val names = result.generatedChecklists.map { it.name }

        assertTrue(names.contains("Daglig renhold"))
        assertTrue(names.contains("Personlig hygiene"))
        assertTrue(names.contains("Avfallshåndtering"))
        assertTrue(names.contains("Ukentlig renhold"))
        assertTrue(names.contains("Skadedyrkontroll"))
        assertTrue(names.contains("Årlig gjennomgang av IK-mat"))
        assertTrue(names.contains("Opplæringsplan"))

        assertTrue(names.none { it == "Temperaturkontroll – Kjøleskap" })
        assertTrue(names.none { it == "Temperaturkontroll – Frysere" })
        assertTrue(names.none { it == "Mottakskontroll" })
        assertTrue(names.none { it == "Tilberedningskontroll" })
        assertTrue(names.none { it == "Nedkjølingskontroll" })
        assertTrue(names.none { it == "Allergenkontroll" })
        assertTrue(names.none { it == "Kritiske kontrollpunkter (CCP)" })
        assertTrue(names.none { it == "Varmholdingskontroll" })
        assertTrue(names.none { it == "Vedlikehold av utstyr" })

        assertEquals(result.generatedChecklists.size, result.summary.totalChecklists)
        assertEquals(
            result.generatedChecklists.sumOf { it.itemCount },
            result.summary.totalItems
        )
    }

    @Test
    fun `generateChecklists saves generated checklists with wizard source and items with sort order`() {
        val auth = AuthenticatedUser(
            10L,
            1L,
            "MANAGER"
        )

        val request = haccpSetupRequest(
            foodTypes = setOf(FoodType.ALLERGEN_CONTAINING),
            processes = emptySet(),
            temperatureEquipment = setOf(TempEquipment.REFRIGERATORS),
            servesVulnerableGroups = false,
            handlesHighRiskProducts = false
        )

        var checklistId = 1L
        var itemId = 1L

        `when`(checklistRepository.save(any(Checklist::class.java))).thenAnswer { invocation ->
            val checklist = invocation.arguments[0] as Checklist
            checklist.copy(id = checklistId++)
        }

        `when`(checklistItemRepository.save(any(ChecklistItem::class.java))).thenAnswer { invocation ->
            val item = invocation.arguments[0] as ChecklistItem
            item.copy(id = itemId++)
        }

        service.generateChecklists(request, auth)

        val checklistCaptor = ArgumentCaptor.forClass(Checklist::class.java)
        verify(checklistRepository, atLeastOnce()).save(checklistCaptor.capture())

        assertTrue(checklistCaptor.allValues.all { it.organizationId == 1L })
        assertTrue(checklistCaptor.allValues.all { it.source == HaccpSetupService.HACCP_WIZARD_SOURCE })
        assertTrue(checklistCaptor.allValues.all { it.active })

        val itemCaptor = ArgumentCaptor.forClass(ChecklistItem::class.java)
        verify(checklistItemRepository, atLeastOnce()).save(itemCaptor.capture())

        assertTrue(itemCaptor.allValues.isNotEmpty())
        assertTrue(itemCaptor.allValues.all { it.title.isNotBlank() })
        assertTrue(itemCaptor.allValues.all { it.sortOrder >= 0 })
    }

    private fun haccpSetupRequest(
        foodTypes: Set<FoodType>,
        processes: Set<FoodProcess>,
        temperatureEquipment: Set<TempEquipment>,
        servesVulnerableGroups: Boolean,
        handlesHighRiskProducts: Boolean
    ): HaccpSetupRequest {
        val ctor = HaccpSetupRequest::class.primaryConstructor
            ?: error("HaccpSetupRequest must have a primary constructor")

        val args = ctor.parameters.associateWith { param ->
            when (param.name) {
                "foodTypes" -> foodTypes
                "processes" -> processes
                "temperatureEquipment" -> temperatureEquipment
                "servesVulnerableGroups" -> servesVulnerableGroups
                "handlesHighRiskProducts" -> handlesHighRiskProducts
                else -> defaultValueFor(param)
            }
        }

        return ctor.callBy(args)
    }

    private fun defaultValueFor(param: KParameter): Any {
        val classifier = param.type.classifier as? KClass<*>
            ?: error("No classifier for parameter ${param.name}")

        val javaClass = classifier.java

        return when {
            javaClass == Boolean::class.javaPrimitiveType || javaClass == java.lang.Boolean::class.java -> false
            javaClass == Int::class.javaPrimitiveType || javaClass == java.lang.Integer::class.java -> 0
            javaClass == Long::class.javaPrimitiveType || javaClass == java.lang.Long::class.java -> 0L
            javaClass == String::class.java -> ""
            javaClass.isEnum -> javaClass.enumConstants.first()
            Set::class.java.isAssignableFrom(javaClass) -> emptySet<Any>()
            List::class.java.isAssignableFrom(javaClass) -> emptyList<Any>()
            else -> error("Unhandled HaccpSetupRequest parameter: ${param.name}")
        }
    }
}