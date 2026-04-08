package com.iksystem.common.report.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull


data class GenerateReportRequest(
    @field:NotBlank val periodFrom: String,
    @field:NotBlank val periodTo: String,
    val title: String? = null,
    @field:NotNull val sections: ReportSectionsConfig,
    val signOffName: String? = null,
    val signOffTitle: String? = null,
    val signOffComments: String? = null,
)

data class ReportSectionsConfig(
    val includeComplianceSummary: Boolean = true,
    val includeTemperatureLogs: Boolean = false,
    val includeChecklists: Boolean = false,
    val selectedChecklistIds: List<Long>? = null,
    val includeHaccpChecklists: Boolean = false,
    val includeCorrectiveActions: Boolean = false,
    val includeFoodDeviations: Boolean = false,
    val includeAlcoholDeviations: Boolean = false,
    val includeAgeVerification: Boolean = false,
    val includeTrainingOverview: Boolean = false,
    val includeLicenseInfo: Boolean = false,
    val includeSignOff: Boolean = false,
)


data class ReportPreviewResponse(
    val header: ReportHeader,
    val complianceSummary: ComplianceSummary? = null,
    val temperatureLogs: List<TemperatureLogEntry>? = null,
    val checklists: List<ChecklistCompletion>? = null,
    val haccpChecklists: HaccpSection? = null,
    val correctiveActions: List<CorrectiveActionEntry>? = null,
    val foodDeviations: List<DeviationEntry>? = null,
    val alcoholDeviations: List<DeviationEntry>? = null,
    val ageVerificationLogs: List<AgeVerificationEntry>? = null,
    val trainingOverview: List<TrainingEntry>? = null,
    val licenseInfo: LicenseInfoSection? = null,
    val signOff: SignOffEntry? = null,
)

data class ReportHeader(
    val organizationName: String,
    val orgNumber: String?,
    val periodFrom: String,
    val periodTo: String,
    val generatedDate: String,
    val generatedByName: String,
    val generatedByRole: String?,
)

data class ComplianceSummary(
    val complianceRate: Double,
    val totalTasks: Long,
    val completedTasks: Long,
    val overdueTasks: Long,
    val openDeviations: Long,
    val closedDeviations: Long,
    val temperatureDeviations: Long,
    val alcoholIncidents: Long,
)

data class TemperatureLogEntry(
    val location: String,
    val avgTemp: Double,
    val minTemp: Double,
    val maxTemp: Double,
    val deviationCount: Long,
    val status: String,
)

data class ChecklistCompletion(
    val id: Long,
    val name: String,
    val frequency: String,
    val completionRate: Double,
    val completedCount: Long,
    val expectedCount: Long,
)

data class HaccpSection(
    val checklists: List<HaccpChecklistEntry>,
)

data class HaccpChecklistEntry(
    val name: String,
    val description: String?,
    val frequency: String,
    val completionRate: Double,
    val items: List<HaccpChecklistItemEntry>,
)

data class HaccpChecklistItemEntry(
    val title: String,
    val description: String?,
    val completed: Boolean,
    val completedAt: String?,
)

data class CorrectiveActionEntry(
    val id: String,
    val date: String,
    val issue: String,
    val actionTaken: String?,
    val status: String,
)

data class DeviationEntry(
    val id: Long,
    val date: String,
    val type: String,
    val severity: String,
    val description: String,
    val immediateAction: String?,
    val status: String,
    val reportedBy: String,
)

data class AgeVerificationEntry(
    val date: String,
    val shift: String,
    val totalChecks: Int,
    val idRequested: Int,
    val refusals: Long,
)

data class TrainingEntry(
    val name: String,
    val role: String?,
    val certification: String,
    val expires: String?,
    val status: String,
)

data class LicenseInfoSection(
    val bevillingNumber: String?,
    val bevillingValidTo: String?,
    val holder: String?,
    val styrerName: String?,
    val stedfortrederName: String?,
    val kunnskapsproveType: String?,
    val kunnskapsproveCandidateName: String?,
    val kunnskapsproveBirthDate: String?,
    val kunnskapsprovePassedDate: String?,
    val kunnskapsproveMunicipality: String?,
)

data class SignOffEntry(
    val name: String?,
    val title: String?,
    val date: String,
    val comments: String?,
)

data class GeneratedReportResponse(
    val id: Long,
    val title: String,
    val periodFrom: String,
    val periodTo: String,
    val generatedByUserName: String,
    val generatedAt: String,
    val documentId: Long?,
    val downloadUrl: String?,
)
