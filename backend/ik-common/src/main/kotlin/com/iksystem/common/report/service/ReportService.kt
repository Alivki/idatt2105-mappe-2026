package com.iksystem.common.report.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.iksystem.common.checklist.model.ChecklistFrequency
import com.iksystem.common.checklist.repository.ChecklistCompletionRepository
import com.iksystem.common.checklist.repository.ChecklistItemRepository
import com.iksystem.common.checklist.repository.ChecklistRepository
import com.iksystem.common.documents.service.DocumentsService
import com.iksystem.common.exception.NotFoundException
import com.iksystem.common.membership.repository.MembershipRepository
import com.iksystem.common.organization.repository.OrganizationRepository
import com.iksystem.common.report.dto.*
import com.iksystem.common.report.model.GeneratedReport
import com.iksystem.common.report.pdf.ReportPdfGenerator
import com.iksystem.common.report.repository.GeneratedReportRepository
import com.iksystem.common.report.repository.ReportDataRepository
import com.iksystem.common.security.AuthenticatedUser
import com.iksystem.common.training.repository.TrainingRepository
import com.iksystem.common.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Service
class ReportService(
    private val generatedReportRepository: GeneratedReportRepository,
    private val organizationRepository: OrganizationRepository,
    private val userRepository: UserRepository,
    private val membershipRepository: MembershipRepository,
    private val checklistRepository: ChecklistRepository,
    private val checklistItemRepository: ChecklistItemRepository,
    private val checklistCompletionRepository: ChecklistCompletionRepository,
    private val trainingRepository: TrainingRepository,
    private val reportDataRepo: ReportDataRepository,
    private val documentsService: DocumentsService,
    private val pdfGenerator: ReportPdfGenerator,
    private val objectMapper: ObjectMapper,
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private val zone = ZoneId.of("Europe/Oslo")

    @Transactional(readOnly = true)
    fun preview(request: GenerateReportRequest, auth: AuthenticatedUser): ReportPreviewResponse {
        val orgId = auth.requireOrganizationId()
        val periodFrom = LocalDate.parse(request.periodFrom)
        val periodTo = LocalDate.parse(request.periodTo)
        val fromInstant = periodFrom.atStartOfDay(zone).toInstant()
        val toInstant = periodTo.plusDays(1).atStartOfDay(zone).toInstant()

        val sections = request.sections

        return ReportPreviewResponse(
            header = buildHeader(orgId, periodFrom, periodTo, auth),
            complianceSummary = if (sections.includeComplianceSummary)
                buildComplianceSummary(orgId, fromInstant, toInstant) else null,
            temperatureLogs = if (sections.includeTemperatureLogs)
                buildTemperatureLogs(orgId, fromInstant, toInstant) else null,
            checklists = if (sections.includeChecklists)
                buildChecklists(orgId, sections.selectedChecklistIds, fromInstant, toInstant, periodFrom, periodTo) else null,
            haccpChecklists = if (sections.includeHaccpChecklists)
                buildHaccpChecklists(orgId, fromInstant, toInstant, periodFrom, periodTo) else null,
            correctiveActions = if (sections.includeCorrectiveActions)
                buildCorrectiveActions(orgId, fromInstant, toInstant) else null,
            foodDeviations = if (sections.includeFoodDeviations)
                buildFoodDeviations(orgId, fromInstant, toInstant) else null,
            alcoholDeviations = if (sections.includeAlcoholDeviations)
                buildAlcoholDeviations(orgId, fromInstant, toInstant) else null,
            ageVerificationLogs = if (sections.includeAgeVerification)
                buildAgeVerification(orgId, periodFrom, periodTo) else null,
            trainingOverview = if (sections.includeTrainingOverview)
                buildTrainingOverview(orgId) else null,
            licenseInfo = if (sections.includeLicenseInfo)
                buildLicenseInfo(orgId) else null,
            signOff = if (sections.includeSignOff)
                SignOffEntry(
                    name = request.signOffName,
                    title = request.signOffTitle,
                    date = LocalDate.now(zone).format(dateFormatter),
                    comments = request.signOffComments,
                ) else null,
        )
    }

    @Transactional
    fun generate(request: GenerateReportRequest, auth: AuthenticatedUser): GeneratedReportResponse {
        val orgId = auth.requireOrganizationId()
        val previewData = preview(request, auth)

        val pdfBytes = pdfGenerator.generatePdf(previewData)

        val periodFrom = LocalDate.parse(request.periodFrom)
        val periodTo = LocalDate.parse(request.periodTo)
        val title = request.title
            ?: "Internkontroll Rapport ${periodFrom.format(dateFormatter)} – ${periodTo.format(dateFormatter)}"
        val fileName = "rapport_${request.periodFrom}_${request.periodTo}.pdf"

        val document = documentsService.uploadBytes(
            bytes = pdfBytes,
            fileName = fileName,
            contentType = "application/pdf",
            folder = "reports",
            auth = auth,
        )

        val user = userRepository.findById(auth.userId)
            .orElseThrow { NotFoundException("Bruker ikke funnet") }

        val report = generatedReportRepository.save(
            GeneratedReport(
                organizationId = orgId,
                title = title,
                periodFrom = periodFrom,
                periodTo = periodTo,
                generatedByUser = user,
                document = document,
                config = objectMapper.writeValueAsString(request.sections),
            )
        )

        val downloadUrl = documentsService.getFileUrl(document.id, orgId)

        return report.toResponse(downloadUrl)
    }

    @Transactional(readOnly = true)
    fun list(auth: AuthenticatedUser): List<GeneratedReportResponse> {
        val orgId = auth.requireOrganizationId()
        return generatedReportRepository.findAllByOrganizationIdOrderByGeneratedAtDesc(orgId)
            .map { it.toResponse(null) }
    }

    @Transactional(readOnly = true)
    fun getReport(id: Long, auth: AuthenticatedUser): GeneratedReportResponse {
        val orgId = auth.requireOrganizationId()
        val report = generatedReportRepository.findByIdAndOrganizationId(id, orgId)
            ?: throw NotFoundException("Rapport ikke funnet")

        val downloadUrl = report.document?.let {
            documentsService.getFileUrl(it.id, orgId)
        }

        return report.toResponse(downloadUrl)
    }

    @Transactional(readOnly = true)
    fun getDownloadUrl(id: Long, auth: AuthenticatedUser): String {
        val orgId = auth.requireOrganizationId()
        val report = generatedReportRepository.findByIdAndOrganizationId(id, orgId)
            ?: throw NotFoundException("Rapport ikke funnet")

        val doc = report.document ?: throw NotFoundException("Dokument ikke funnet for denne rapporten")
        return documentsService.getFileUrl(doc.id, orgId)
    }

    @Transactional
    fun delete(id: Long, auth: AuthenticatedUser) {
        val orgId = auth.requireOrganizationId()
        val report = generatedReportRepository.findByIdAndOrganizationId(id, orgId)
            ?: throw NotFoundException("Rapport ikke funnet")

        report.document?.let { doc ->
            documentsService.deleteFile(doc.id, orgId)
        }

        generatedReportRepository.delete(report)
    }

    @Transactional(readOnly = true)
    fun exportJson(request: GenerateReportRequest, auth: AuthenticatedUser): ReportPreviewResponse {
        return preview(request, auth)
    }

    private fun buildHeader(
        orgId: Long,
        periodFrom: LocalDate,
        periodTo: LocalDate,
        auth: AuthenticatedUser,
    ): ReportHeader {
        val org = organizationRepository.findById(orgId)
            .orElseThrow { NotFoundException("Organisasjon ikke funnet") }
        val user = userRepository.findById(auth.userId)
            .orElseThrow { NotFoundException("Bruker ikke funnet") }
        val membership = membershipRepository.findByUserIdAndOrganizationId(auth.userId, orgId)

        return ReportHeader(
            organizationName = org.name,
            orgNumber = org.orgNumber,
            periodFrom = periodFrom.format(dateFormatter),
            periodTo = periodTo.format(dateFormatter),
            generatedDate = LocalDate.now(zone).format(dateFormatter),
            generatedByName = user.fullName,
            generatedByRole = membership?.role?.name,
        )
    }

    private fun buildComplianceSummary(orgId: Long, from: Instant, to: Instant): ComplianceSummary {
        val totalItems = checklistItemRepository.countAllByOrganizationId(orgId)
        val completedItems = checklistItemRepository.countCompletedByOrganizationId(orgId)
        val overdue = totalItems - completedItems

        val openFoodDev = reportDataRepo.countFoodDeviationsByStatus(orgId, from, to, "OPEN")
        val closedFoodDev = reportDataRepo.countFoodDeviationsByStatus(orgId, from, to, "CLOSED")
        val openAlcDev = reportDataRepo.countAlcoholDeviationsByStatus(orgId, from, to, "OPEN")
        val closedAlcDev = reportDataRepo.countAlcoholDeviationsByStatus(orgId, from, to, "CLOSED")

        val tempDeviations = reportDataRepo.countTempDeviations(orgId, from, to)

        val complianceRate = if (totalItems > 0) (completedItems.toDouble() / totalItems * 100) else 100.0

        return ComplianceSummary(
            complianceRate = Math.round(complianceRate * 10) / 10.0,
            totalTasks = totalItems,
            completedTasks = completedItems,
            overdueTasks = overdue,
            openDeviations = openFoodDev + openAlcDev,
            closedDeviations = closedFoodDev + closedAlcDev,
            temperatureDeviations = tempDeviations,
            alcoholIncidents = openAlcDev + closedAlcDev,
        )
    }

    private fun buildTemperatureLogs(orgId: Long, from: Instant, to: Instant): List<TemperatureLogEntry> {
        val appliances = reportDataRepo.findActiveAppliances(orgId)
        val measurements = reportDataRepo.findMeasurements(orgId, from, to)

        return appliances.map { appliance ->
            val appMeasurements = measurements.filter { it.applianceId == appliance.id }
            val temps = appMeasurements.map { it.temperature.toDouble() }
            val deviationCount = appMeasurements.count { it.status == "DEVIATION" }.toLong()

            val status = when {
                deviationCount == 0L && temps.isNotEmpty() -> "Normal"
                deviationCount in 1..2 -> "Advarsel"
                deviationCount > 2 -> "Kritisk"
                else -> "Ingen data"
            }

            TemperatureLogEntry(
                location = appliance.name,
                avgTemp = if (temps.isNotEmpty()) Math.round(temps.average() * 10) / 10.0 else 0.0,
                minTemp = if (temps.isNotEmpty()) Math.round(temps.min() * 10) / 10.0 else 0.0,
                maxTemp = if (temps.isNotEmpty()) Math.round(temps.max() * 10) / 10.0 else 0.0,
                deviationCount = deviationCount,
                status = status,
            )
        }
    }

    private fun buildChecklists(
        orgId: Long,
        selectedIds: List<Long>?,
        from: Instant,
        to: Instant,
        periodFrom: LocalDate,
        periodTo: LocalDate,
    ): List<ChecklistCompletion> {
        val checklists = if (selectedIds != null) {
            selectedIds.mapNotNull { checklistRepository.findByIdAndOrganizationId(it, orgId) }
        } else {
            checklistRepository.findAllByOrganizationIdOrderByCreatedAtDesc(orgId)
                .filter { it.active && it.source != "HACCP_WIZARD" }
        }

        return checklists.map { checklist ->
            val completedCount = checklistCompletionRepository.countByChecklistIdAndCompletedAtBetween(
                checklist.id, from, to
            )
            val expectedCount = calculateExpectedCompletions(checklist.frequency, periodFrom, periodTo)
            val rate = if (expectedCount > 0) (completedCount.toDouble() / expectedCount * 100) else 0.0

            ChecklistCompletion(
                id = checklist.id,
                name = checklist.name,
                frequency = checklist.frequency.name,
                completionRate = Math.round(rate * 10.0) / 10.0,
                completedCount = completedCount,
                expectedCount = expectedCount,
            )
        }
    }

    private fun buildHaccpChecklists(
        orgId: Long,
        from: Instant,
        to: Instant,
        periodFrom: LocalDate,
        periodTo: LocalDate,
    ): HaccpSection {
        val haccpChecklists = checklistRepository.findAllByOrganizationIdAndSource(orgId, "HACCP_WIZARD")

        val entries = haccpChecklists.map { checklist ->
            val completedCount = checklistCompletionRepository.countByChecklistIdAndCompletedAtBetween(
                checklist.id, from, to
            )
            val expectedCount = calculateExpectedCompletions(checklist.frequency, periodFrom, periodTo)
            val rate = if (expectedCount > 0) (completedCount.toDouble() / expectedCount * 100) else 0.0

            val items = checklistItemRepository.findAllByChecklistIdOrderByIdAsc(checklist.id)

            HaccpChecklistEntry(
                name = checklist.name,
                description = checklist.description,
                frequency = checklist.frequency.name,
                completionRate = Math.round(rate * 10.0) / 10.0,
                items = items.map { item ->
                    HaccpChecklistItemEntry(
                        title = item.title,
                        description = item.description,
                        completed = item.completed,
                        completedAt = item.completedAt?.atZone(zone)?.format(dateFormatter),
                    )
                },
            )
        }

        return HaccpSection(checklists = entries)
    }

    private fun calculateExpectedCompletions(frequency: ChecklistFrequency, from: LocalDate, to: LocalDate): Long {
        val days = ChronoUnit.DAYS.between(from, to.plusDays(1))
        return when (frequency) {
            ChecklistFrequency.DAILY -> days
            ChecklistFrequency.WEEKLY -> (days + 6) / 7  // number of weeks (ceiling)
            ChecklistFrequency.MONTHLY -> {
                var count = 0L
                var current = from.withDayOfMonth(1)
                val end = to.withDayOfMonth(1)
                while (!current.isAfter(end)) {
                    count++
                    current = current.plusMonths(1)
                }
                count
            }
            ChecklistFrequency.YEARLY -> {
                var count = 0L
                var year = from.year
                while (year <= to.year) {
                    count++
                    year++
                }
                count
            }
        }
    }

    private fun buildCorrectiveActions(orgId: Long, from: Instant, to: Instant): List<CorrectiveActionEntry> {
        val deviations = reportDataRepo.findFoodDeviations(orgId, from, to)
            .filter { it.immediateAction != null }

        return deviations.map { dev ->
            CorrectiveActionEntry(
                id = "CA-${String.format("%03d", dev.id)}",
                date = dev.reportedAt.atZone(zone).format(dateFormatter),
                issue = dev.description,
                actionTaken = dev.immediateAction,
                status = formatStatus(dev.status),
            )
        }
    }

    private fun buildFoodDeviations(orgId: Long, from: Instant, to: Instant): List<DeviationEntry> {
        return reportDataRepo.findFoodDeviations(orgId, from, to).map { dev ->
            DeviationEntry(
                id = dev.id,
                date = dev.reportedAt.atZone(zone).format(dateFormatter),
                type = dev.deviationType,
                severity = dev.severity,
                description = dev.description,
                immediateAction = dev.immediateAction,
                status = formatStatus(dev.status),
                reportedBy = dev.reportedByName,
            )
        }
    }

    private fun buildAlcoholDeviations(orgId: Long, from: Instant, to: Instant): List<DeviationEntry> {
        return reportDataRepo.findAlcoholDeviations(orgId, from, to).map { dev ->
            DeviationEntry(
                id = dev.id,
                date = dev.reportedAt.atZone(zone).format(dateFormatter),
                type = dev.deviationType,
                severity = "–",
                description = dev.description,
                immediateAction = dev.immediateAction,
                status = formatStatus(dev.status),
                reportedBy = dev.reportedByName,
            )
        }
    }

    private fun buildAgeVerification(orgId: Long, from: LocalDate, to: LocalDate): List<AgeVerificationEntry> {
        val shifts = reportDataRepo.findAgeVerificationShifts(orgId, from, to)

        return shifts.groupBy { it.shiftDate }.map { (date, dayShifts) ->
            val totalChecks = dayShifts.sumOf { it.idsCheckedCount }
            val dayDeviations = reportDataRepo.countAlcoholDeviationsByDate(orgId, date)

            AgeVerificationEntry(
                date = date.format(dateFormatter),
                shift = if (dayShifts.size == 1) "Kveld" else "${dayShifts.size} skift",
                totalChecks = totalChecks,
                idRequested = totalChecks,
                refusals = dayDeviations,
            )
        }
    }

    private fun buildTrainingOverview(orgId: Long): List<TrainingEntry> {
        val logs = trainingRepository.findAllByOrganizationIdOrderByCreatedAtDesc(orgId)
        val memberships = membershipRepository.findAllByOrganizationId(orgId)
        val roleMap = memberships.associate { it.user.id to it.role.name }

        return logs.map { log ->
            TrainingEntry(
                name = log.employeeUser.fullName,
                role = roleMap[log.employeeUser.id],
                certification = log.title,
                expires = log.expiresAt?.atZone(zone)?.format(dateFormatter),
                status = formatTrainingStatus(log.status.name),
            )
        }
    }

    private fun buildLicenseInfo(orgId: Long): LicenseInfoSection {
        val policy = reportDataRepo.findAlcoholPolicy(orgId)
        val org = organizationRepository.findById(orgId).orElse(null)

        return LicenseInfoSection(
            bevillingNumber = policy?.bevillingNumber,
            bevillingValidTo = policy?.bevillingValidUntil?.format(dateFormatter),
            holder = org?.name,
            styrerName = policy?.styrerName,
            stedfortrederName = policy?.stedfortrederName,
            kunnskapsproveType = policy?.kunnskapsproveType,
            kunnskapsproveCandidateName = policy?.kunnskapsproveCandidateName,
            kunnskapsproveBirthDate = policy?.kunnskapsproveBirthDate?.format(dateFormatter),
            kunnskapsprovePassedDate = policy?.kunnskapsprovePassedDate?.format(dateFormatter),
            kunnskapsproveMunicipality = policy?.kunnskapsproveMunicipality,
        )
    }

    // ── Helpers ──

    private fun formatStatus(status: String): String = when (status) {
        "OPEN" -> "Åpen"
        "UNDER_TREATMENT" -> "Under behandling"
        "CLOSED" -> "Lukket"
        else -> status
    }

    private fun formatTrainingStatus(status: String): String = when (status) {
        "COMPLETED" -> "Gyldig"
        "EXPIRES_SOON" -> "Utløper snart"
        "EXPIRED" -> "Utløpt"
        "NOT_COMPLETED" -> "Ikke fullført"
        else -> status
    }

    private fun GeneratedReport.toResponse(downloadUrl: String?): GeneratedReportResponse {
        return GeneratedReportResponse(
            id = id,
            title = title,
            periodFrom = periodFrom.format(dateFormatter),
            periodTo = periodTo.format(dateFormatter),
            generatedByUserName = generatedByUser.fullName,
            generatedAt = generatedAt.atZone(zone).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
            documentId = document?.id,
            downloadUrl = downloadUrl,
        )
    }
}
