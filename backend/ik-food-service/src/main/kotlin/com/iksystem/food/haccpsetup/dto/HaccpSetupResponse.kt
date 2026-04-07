package com.iksystem.food.haccpsetup.dto

import com.iksystem.common.checklist.dto.ChecklistResponse

data class HaccpSetupResponse(
    val generatedChecklists: List<ChecklistResponse>,
    val summary: GenerationSummary,
)

data class GenerationSummary(
    val totalChecklists: Int,
    val totalItems: Int,
)
