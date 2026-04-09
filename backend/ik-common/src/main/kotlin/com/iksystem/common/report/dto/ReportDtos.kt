package com.iksystem.common.report.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

/**
 * Request object for generating or previewing a report.
 *
 * Defines the reporting period, selected sections, and optional metadata
 * such as title and sign-off details.
 */
data class GenerateReportRequest(
    /** Start date of the reporting period (ISO string). */
    @field:NotBlank val periodFrom: String,

    /** End date of the reporting period (ISO string). */
    @field:NotBlank val periodTo: String,

    /** Optional custom title for the report. */
    val title: String? = null,

    /** Configuration specifying which sections to include in the report. */
    @field:NotNull val sections: ReportSectionsConfig,

    /** Optional name used for report sign-off. */
    val signOffName: String? = null,

    /** Optional title/role of the sign-off person. */
    val signOffTitle: String? = null,

    /** Optional comments included in the sign-off section. */
    val signOffComments: String? = null,
)

/**
 * Configuration object defining which sections should be included in a report.
 *
 * Each flag toggles inclusion of a specific data section.
 */
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

/**
 * Response used for previewing a report before generation.
 *
 * Contains structured data for all selected sections.
 */
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

/**
 * Metadata header for a report.
 */
data class ReportHeader(
    val organizationName: String,
    val orgNumber: String?,
    val periodFrom: String,
    val periodTo: String,
    val generatedDate: String,
    val generatedByName: String,
    val generatedByRole: String?,
)

/**
 * Aggregated compliance statistics for the reporting period.
 */
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

/**
 * Represents a summarized temperature log entry.
 */
data class TemperatureLogEntry(
    val location: String,
    val avgTemp: Double,
    val minTemp: Double,
    val maxTemp: Double,
    val deviationCount: Long,
    val status: String,
)

/**
 * Represents completion statistics for a checklist.
 */
data class ChecklistCompletion(
    val id: Long,
    val name: String,
    val frequency: String,
    val completionRate: Double,
    val completedCount: Long,
    val expectedCount: Long,
)

/**
 * HACCP section containing multiple checklist summaries.
 */
data class HaccpSection(
    val checklists: List<HaccpChecklistEntry>,
)

/**
 * Represents a HACCP checklist with completion data.
 */
data class HaccpChecklistEntry(
    val name: String,
    val description: String?,
    val frequency: String,
    val completionRate: Double,
    val items: List<HaccpChecklistItemEntry>,
)

/**
 * Represents an individual HACCP checklist item.
 */
data class HaccpChecklistItemEntry(
    val title: String,
    val description: String?,
    val completed: Boolean,
    val completedAt: String?,
)

/**
 * Represents a corrective action taken in response to an issue.
 */
data class CorrectiveActionEntry(
    val id: String,
    val date: String,
    val issue: String,
    val actionTaken: String?,
    val status: String,
)

/**
 * Represents a deviation entry (food or alcohol).
 */
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

/**
 * Represents age verification activity within a shift.
 */
data class AgeVerificationEntry(
    val date: String,
    val shift: String,
    val totalChecks: Int,
    val idRequested: Int,
    val refusals: Long,
)

/**
 * Represents training or certification information for a user.
 */
data class TrainingEntry(
    val name: String,
    val role: String?,
    val certification: String,
    val expires: String?,
    val status: String,
)

/**
 * Section containing license and regulatory information.
 */
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

/**
 * Represents sign-off information for a report.
 */
data class SignOffEntry(
    val name: String?,
    val title: String?,
    val date: String,
    val comments: String?,
)

/**
 * Response returned after generating or retrieving a report.
 *
 * Contains metadata and optional download information.
 */
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