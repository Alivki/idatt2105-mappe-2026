package com.iksystem.food.haccpsetup.controller

import com.iksystem.common.checklist.dto.ChecklistItemResponse
import com.iksystem.common.checklist.dto.ChecklistResponse
import com.iksystem.common.checklist.dto.ChecklistStatus
import com.iksystem.common.checklist.model.ChecklistFrequency
import com.iksystem.common.security.AuthenticatedUser
import com.iksystem.food.haccpsetup.dto.FoodProcess
import com.iksystem.food.haccpsetup.dto.FoodType
import com.iksystem.food.haccpsetup.dto.GenerationSummary
import com.iksystem.food.haccpsetup.dto.HaccpSetupRequest
import com.iksystem.food.haccpsetup.dto.HaccpSetupResponse
import com.iksystem.food.haccpsetup.dto.TempEquipment
import com.iksystem.food.haccpsetup.service.HaccpSetupService
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class HaccpSetupControllerTest {

    private lateinit var haccpSetupService: HaccpSetupService
    private lateinit var controller: HaccpSetupController

    @BeforeEach
    fun setUp() {
        haccpSetupService = mock(HaccpSetupService::class.java)
        controller = HaccpSetupController(haccpSetupService)
    }

    @Test
    fun `generate returns 201 with setup response`() {
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

        val response = HaccpSetupResponse(
            generatedChecklists = listOf(
                checklistResponse(
                    id = 1L,
                    name = "Daglig renhold",
                    frequency = ChecklistFrequency.DAILY,
                    itemCount = 2
                ),
                checklistResponse(
                    id = 2L,
                    name = "Temperaturkontroll – Kjøleskap",
                    frequency = ChecklistFrequency.DAILY,
                    itemCount = 3
                )
            ),
            summary = GenerationSummary(
                totalChecklists = 2,
                totalItems = 5
            )
        )

        `when`(haccpSetupService.generateChecklists(request, auth)).thenReturn(response)

        val result = controller.generate(request, auth)

        assertEquals(201, result.statusCode.value())
        assertNotNull(result.body)
        assertEquals(2, result.body!!.generatedChecklists.size)
        assertEquals(2, result.body!!.summary.totalChecklists)
        assertEquals(5, result.body!!.summary.totalItems)
        assertEquals("Daglig renhold", result.body!!.generatedChecklists[0].name)

        verify(haccpSetupService).generateChecklists(request, auth)
    }

    private fun checklistResponse(
        id: Long,
        name: String,
        frequency: ChecklistFrequency,
        itemCount: Int
    ) = ChecklistResponse(
        id = id,
        name = name,
        description = "Description",
        frequency = frequency,
        active = true,
        source = "HACCP_WIZARD",
        itemCount = itemCount,
        completedItemCount = 0,
        status = ChecklistStatus.NOT_STARTED,
        items = listOf(
            ChecklistItemResponse(
                id = 1L,
                title = "Item",
                description = "Desc",
                completed = false,
                completedAt = null
            )
        )
    )

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