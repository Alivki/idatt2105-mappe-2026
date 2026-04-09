package com.iksystem.common.report.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.iksystem.common.checklist.model.Checklist
import com.iksystem.common.checklist.model.ChecklistFrequency
import com.iksystem.common.checklist.model.ChecklistItem
import com.iksystem.common.checklist.repository.ChecklistCompletionRepository
import com.iksystem.common.checklist.repository.ChecklistItemRepository
import com.iksystem.common.checklist.repository.ChecklistRepository
import com.iksystem.common.documents.model.Document
import com.iksystem.common.documents.service.DocumentsService
import com.iksystem.common.exception.NotFoundException
import com.iksystem.common.membership.model.Membership
import com.iksystem.common.membership.repository.MembershipRepository
import com.iksystem.common.organization.model.Organization
import com.iksystem.common.organization.repository.OrganizationRepository
import com.iksystem.common.report.dto.GenerateReportRequest
import com.iksystem.common.report.dto.ReportSectionsConfig
import com.iksystem.common.report.model.GeneratedReport
import com.iksystem.common.report.pdf.ReportPdfGenerator
import com.iksystem.common.report.repository.*
import com.iksystem.common.security.AuthenticatedUser
import com.iksystem.common.training.model.TrainingLog
import com.iksystem.common.training.model.TrainingStatus
import com.iksystem.common.training.repository.TrainingRepository
import com.iksystem.common.user.model.Role
import com.iksystem.common.user.model.User
import com.iksystem.common.user.repository.UserRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.*
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.*

class ReportServiceTest : FunSpec({

    val generatedReportRepo = mockk<GeneratedReportRepository>()
    val organizationRepo = mockk<OrganizationRepository>()
    val userRepo = mockk<UserRepository>()
    val membershipRepo = mockk<MembershipRepository>()
    val checklistRepo = mockk<ChecklistRepository>()
    val checklistItemRepo = mockk<ChecklistItemRepository>()
    val checklistCompletionRepo = mockk<ChecklistCompletionRepository>()
    val trainingRepo = mockk<TrainingRepository>()
    val reportDataRepo = mockk<ReportDataRepository>()
    val documentsService = mockk<DocumentsService>()
    val pdfGenerator = mockk<ReportPdfGenerator>()
    val objectMapper = ObjectMapper()

    val service = ReportService(
        generatedReportRepository = generatedReportRepo,
        organizationRepository = organizationRepo,
        userRepository = userRepo,
        membershipRepository = membershipRepo,
        checklistRepository = checklistRepo,
        checklistItemRepository = checklistItemRepo,
        checklistCompletionRepository = checklistCompletionRepo,
        trainingRepository = trainingRepo,
        reportDataRepo = reportDataRepo,
        documentsService = documentsService,
        pdfGenerator = pdfGenerator,
        objectMapper = objectMapper,
    )

    val orgId = 10L
    val userId = 1L
    val auth = AuthenticatedUser(userId = userId, organizationId = orgId, role = "ADMIN")
    val org = Organization(id = orgId, name = "Test Restaurant AS", orgNumber = "123 456 789")
    val user = User(id = userId, email = "admin@test.com", password = "hashed", fullName = "Admin User", phoneNumber = "+47123")
    val membership = Membership(
        id = 1L,
        user = user,
        organization = org,
        role = Role.ADMIN,
    )

    val minimalRequest = GenerateReportRequest(
        periodFrom = "2026-03-01",
        periodTo = "2026-03-31",
        sections = ReportSectionsConfig(),
    )

    beforeTest {
        clearMocks(
            generatedReportRepo, organizationRepo, userRepo, membershipRepo,
            checklistRepo, checklistItemRepo, checklistCompletionRepo, trainingRepo, reportDataRepo,
            documentsService, pdfGenerator,
        )
        every { organizationRepo.findById(orgId) } returns Optional.of(org)
        every { userRepo.findById(userId) } returns Optional.of(user)
        every { membershipRepo.findByUserIdAndOrganizationId(userId, orgId) } returns membership
    }

    test("preview returns header with org info and user info") {
        every { checklistItemRepo.countAllByOrganizationId(orgId) } returns 50L
        every { checklistItemRepo.countCompletedByOrganizationId(orgId) } returns 45L
        every { reportDataRepo.countFoodDeviationsByStatus(orgId, any(), any(), "OPEN") } returns 1L
        every { reportDataRepo.countFoodDeviationsByStatus(orgId, any(), any(), "CLOSED") } returns 2L
        every { reportDataRepo.countAlcoholDeviationsByStatus(orgId, any(), any(), "OPEN") } returns 0L
        every { reportDataRepo.countAlcoholDeviationsByStatus(orgId, any(), any(), "CLOSED") } returns 1L
        every { reportDataRepo.countTempDeviations(orgId, any(), any()) } returns 3L

        val result = service.preview(minimalRequest, auth)

        result.header.organizationName shouldBe "Test Restaurant AS"
        result.header.orgNumber shouldBe "123 456 789"
        result.header.periodFrom shouldBe "01.03.2026"
        result.header.periodTo shouldBe "31.03.2026"
        result.header.generatedByName shouldBe "Admin User"
        result.header.generatedByRole shouldBe "ADMIN"
    }

    test("preview includes compliance summary when enabled") {
        every { checklistItemRepo.countAllByOrganizationId(orgId) } returns 100L
        every { checklistItemRepo.countCompletedByOrganizationId(orgId) } returns 90L
        every { reportDataRepo.countFoodDeviationsByStatus(orgId, any(), any(), "OPEN") } returns 2L
        every { reportDataRepo.countFoodDeviationsByStatus(orgId, any(), any(), "CLOSED") } returns 5L
        every { reportDataRepo.countAlcoholDeviationsByStatus(orgId, any(), any(), "OPEN") } returns 1L
        every { reportDataRepo.countAlcoholDeviationsByStatus(orgId, any(), any(), "CLOSED") } returns 3L
        every { reportDataRepo.countTempDeviations(orgId, any(), any()) } returns 4L

        val result = service.preview(minimalRequest, auth)

        val summary = result.complianceSummary
        summary.shouldNotBeNull()
        summary.complianceRate shouldBe 90.0
        summary.totalTasks shouldBe 100L
        summary.completedTasks shouldBe 90L
        summary.overdueTasks shouldBe 10L
        summary.openDeviations shouldBe 3L
        summary.closedDeviations shouldBe 8L
        summary.temperatureDeviations shouldBe 4L
        summary.alcoholIncidents shouldBe 4L
    }

    test("preview excludes sections when disabled") {
        val request = GenerateReportRequest(
            periodFrom = "2026-03-01",
            periodTo = "2026-03-31",
            sections = ReportSectionsConfig(
                includeComplianceSummary = false,
                includeTemperatureLogs = false,
                includeChecklists = false,
                includeFoodDeviations = false,
            ),
        )

        val result = service.preview(request, auth)

        result.complianceSummary.shouldBeNull()
        result.temperatureLogs.shouldBeNull()
        result.checklists.shouldBeNull()
        result.foodDeviations.shouldBeNull()
        result.alcoholDeviations.shouldBeNull()
        result.ageVerificationLogs.shouldBeNull()
        result.trainingOverview.shouldBeNull()
        result.licenseInfo.shouldBeNull()
        result.signOff.shouldBeNull()
    }

    test("preview includes temperature logs when enabled") {
        val request = minimalRequest.copy(
            sections = ReportSectionsConfig(
                includeComplianceSummary = false,
                includeTemperatureLogs = true,
            ),
        )

        every { reportDataRepo.findActiveAppliances(orgId) } returns listOf(
            TempApplianceData(1L, "Walk-in Cooler", "FRIDGE", BigDecimal("0"), BigDecimal("5")),
            TempApplianceData(2L, "Display Fridge", "FRIDGE", BigDecimal("2"), BigDecimal("8")),
        )
        every { reportDataRepo.findMeasurements(orgId, any(), any()) } returns listOf(
            TempMeasurementData(1L, 1L, Instant.parse("2026-03-10T08:00:00Z"), BigDecimal("3.0"), "OK"),
            TempMeasurementData(2L, 1L, Instant.parse("2026-03-10T12:00:00Z"), BigDecimal("4.0"), "OK"),
            TempMeasurementData(3L, 2L, Instant.parse("2026-03-10T08:00:00Z"), BigDecimal("7.5"), "DEVIATION"),
        )

        val result = service.preview(request, auth)

        val logs = result.temperatureLogs
        logs.shouldNotBeNull()
        logs shouldHaveSize 2
        logs[0].location shouldBe "Walk-in Cooler"
        logs[0].avgTemp shouldBe 3.5
        logs[0].minTemp shouldBe 3.0
        logs[0].maxTemp shouldBe 4.0
        logs[0].deviationCount shouldBe 0L
        logs[0].status shouldBe "Normal"

        logs[1].location shouldBe "Display Fridge"
        logs[1].deviationCount shouldBe 1L
        logs[1].status shouldBe "Advarsel"
    }

    test("preview includes checklists with period-based completion rate") {
        val request = minimalRequest.copy(
            sections = ReportSectionsConfig(
                includeComplianceSummary = false,
                includeChecklists = true,
            ),
        )

        val checklist = Checklist(
            id = 1L,
            organizationId = orgId,
            name = "Kitchen Daily",
            frequency = ChecklistFrequency.DAILY,
            active = true,
        )

        every { checklistRepo.findAllByOrganizationIdOrderByCreatedAtDesc(orgId) } returns listOf(checklist)
        every { checklistCompletionRepo.countByChecklistIdAndCompletedAtBetween(1L, any(), any()) } returns 28L

        val result = service.preview(request, auth)

        val checklists = result.checklists
        checklists.shouldNotBeNull()
        checklists shouldHaveSize 1
        checklists[0].name shouldBe "Kitchen Daily"
        checklists[0].completedCount shouldBe 28L
        checklists[0].expectedCount shouldBe 31L
        checklists[0].completionRate shouldBe 90.3
    }

    test("preview includes food deviations when enabled") {
        val request = minimalRequest.copy(
            sections = ReportSectionsConfig(
                includeComplianceSummary = false,
                includeFoodDeviations = true,
            ),
        )

        every { reportDataRepo.findFoodDeviations(orgId, any(), any()) } returns listOf(
            FoodDeviationData(
                id = 1L,
                reportedAt = Instant.parse("2026-03-08T10:00:00Z"),
                reportedByName = "Admin User",
                deviationType = "TEMPERATUR",
                severity = "HIGH",
                description = "Temp too high in cooler",
                immediateAction = "Compressor serviced",
                status = "CLOSED",
            ),
        )

        val result = service.preview(request, auth)

        val deviations = result.foodDeviations
        deviations.shouldNotBeNull()
        deviations shouldHaveSize 1
        deviations[0].type shouldBe "TEMPERATUR"
        deviations[0].severity shouldBe "HIGH"
        deviations[0].status shouldBe "Lukket"
        deviations[0].reportedBy shouldBe "Admin User"
    }

    test("preview includes alcohol deviations when enabled") {
        val request = minimalRequest.copy(
            sections = ReportSectionsConfig(
                includeComplianceSummary = false,
                includeAlcoholDeviations = true,
            ),
        )

        every { reportDataRepo.findAlcoholDeviations(orgId, any(), any()) } returns listOf(
            AlcoholDeviationData(
                id = 17L,
                reportedAt = Instant.parse("2026-03-12T20:00:00Z"),
                reportedByName = "Admin User",
                deviationType = "UNDERAGE",
                description = "Underage attempt",
                immediateAction = "ID confiscated",
                status = "CLOSED",
            ),
        )

        val result = service.preview(request, auth)

        val deviations = result.alcoholDeviations
        deviations.shouldNotBeNull()
        deviations shouldHaveSize 1
        deviations[0].id shouldBe 17L
        deviations[0].type shouldBe "UNDERAGE"
        deviations[0].severity shouldBe "–"
        deviations[0].status shouldBe "Lukket"
    }

    test("preview includes sign-off when enabled") {
        val request = minimalRequest.copy(
            sections = ReportSectionsConfig(
                includeComplianceSummary = false,
                includeSignOff = true,
            ),
            signOffName = "Erik Bakken",
            signOffTitle = "Daglig leder",
            signOffComments = "All actions addressed.",
        )

        val result = service.preview(request, auth)

        val signOff = result.signOff
        signOff.shouldNotBeNull()
        signOff.name shouldBe "Erik Bakken"
        signOff.title shouldBe "Daglig leder"
        signOff.comments shouldBe "All actions addressed."
    }

    test("preview includes license info when enabled") {
        val request = minimalRequest.copy(
            sections = ReportSectionsConfig(
                includeComplianceSummary = false,
                includeLicenseInfo = true,
            ),
        )

        every { reportDataRepo.findAlcoholPolicy(orgId) } returns AlcoholPolicyData(
            bevillingNumber = "SKJ-2024-0891",
            bevillingValidUntil = LocalDate.of(2028, 12, 31),
            styrerName = "Erik Bakken",
            stedfortrederName = "Kari Nordmann",
            kunnskapsproveType = "SKJENKE",
            kunnskapsproveCandidateName = "Erik Bakken",
            kunnskapsproveBirthDate = LocalDate.of(1985, 1, 1),
            kunnskapsprovePassedDate = LocalDate.of(2024, 6, 1),
            kunnskapsproveMunicipality = "Trondheim",
        )

        val result = service.preview(request, auth)

        val license = result.licenseInfo
        license.shouldNotBeNull()
        license.bevillingNumber shouldBe "SKJ-2024-0891"
        license.bevillingValidTo shouldBe "31.12.2028"
        license.holder shouldBe "Test Restaurant AS"
        license.styrerName shouldBe "Erik Bakken"
        license.stedfortrederName shouldBe "Kari Nordmann"
        license.kunnskapsproveType shouldBe "SKJENKE"
        license.kunnskapsproveCandidateName shouldBe "Erik Bakken"
        license.kunnskapsproveBirthDate shouldBe "01.01.1985"
        license.kunnskapsprovePassedDate shouldBe "01.06.2024"
        license.kunnskapsproveMunicipality shouldBe "Trondheim"
    }

    test("preview includes training overview when enabled") {
        val request = minimalRequest.copy(
            sections = ReportSectionsConfig(
                includeComplianceSummary = false,
                includeTrainingOverview = true,
            ),
        )

        val employee = User(id = 2L, email = "kari@test.com", password = "hashed", fullName = "Kari Nordmann", phoneNumber = "+47124")
        val trainingLog = TrainingLog(
            id = 1L,
            organizationId = orgId,
            employeeUser = employee,
            loggedByUser = user,
            title = "Ansvarlig Vertskap",
            status = TrainingStatus.COMPLETED,
            expiresAt = Instant.parse("2026-09-15T00:00:00Z"),
        )

        every { trainingRepo.findAllByOrganizationIdOrderByCreatedAtDesc(orgId) } returns listOf(trainingLog)
        every { membershipRepo.findAllByOrganizationId(orgId) } returns listOf(
            Membership(id = 2L, user = employee, organization = org, role = Role.EMPLOYEE),
        )

        val result = service.preview(request, auth)

        val training = result.trainingOverview
        training.shouldNotBeNull()
        training shouldHaveSize 1
        training[0].name shouldBe "Kari Nordmann"
        training[0].role shouldBe "EMPLOYEE"
        training[0].certification shouldBe "Ansvarlig Vertskap"
        training[0].status shouldBe "Gyldig"
    }

    test("preview includes corrective actions from food deviations with immediate action") {
        val request = minimalRequest.copy(
            sections = ReportSectionsConfig(
                includeComplianceSummary = false,
                includeCorrectiveActions = true,
            ),
        )

        every { reportDataRepo.findFoodDeviations(orgId, any(), any()) } returns listOf(
            FoodDeviationData(
                id = 42L,
                reportedAt = Instant.parse("2026-03-08T10:00:00Z"),
                reportedByName = "Admin User",
                deviationType = "TEMPERATUR",
                severity = "HIGH",
                description = "Cooler exceeded 7°C",
                immediateAction = "Compressor serviced",
                status = "CLOSED",
            ),
            FoodDeviationData(
                id = 43L,
                reportedAt = Instant.parse("2026-03-09T10:00:00Z"),
                reportedByName = "Admin User",
                deviationType = "RENHOLD",
                severity = "LOW",
                description = "Minor cleaning issue",
                immediateAction = null,
                status = "OPEN",
            ),
        )

        val result = service.preview(request, auth)

        val actions = result.correctiveActions
        actions.shouldNotBeNull()
        actions shouldHaveSize 1
        actions[0].id shouldBe "CA-042"
        actions[0].issue shouldBe "Cooler exceeded 7°C"
        actions[0].actionTaken shouldBe "Compressor serviced"
        actions[0].status shouldBe "Lukket"
    }

    test("preview includes age verification when enabled") {
        val request = minimalRequest.copy(
            sections = ReportSectionsConfig(
                includeComplianceSummary = false,
                includeAgeVerification = true,
            ),
        )

        every { reportDataRepo.findAgeVerificationShifts(orgId, any(), any()) } returns listOf(
            AgeVerificationShiftData(1L, LocalDate.of(2026, 3, 5), "Admin User", 14, "COMPLETED"),
        )
        every { reportDataRepo.countAlcoholDeviationsByDate(orgId, LocalDate.of(2026, 3, 5)) } returns 2L

        val result = service.preview(request, auth)

        val ageVer = result.ageVerificationLogs
        ageVer.shouldNotBeNull()
        ageVer shouldHaveSize 1
        ageVer[0].date shouldBe "05.03.2026"
        ageVer[0].totalChecks shouldBe 14
        ageVer[0].refusals shouldBe 2L
    }

    test("preview includes HACCP checklists with period-based completion rate") {
        val request = minimalRequest.copy(
            sections = ReportSectionsConfig(
                includeComplianceSummary = false,
                includeHaccpChecklists = true,
            ),
        )

        val haccpChecklist = Checklist(
            id = 5L, organizationId = orgId, name = "Daglig rengjøring",
            frequency = ChecklistFrequency.DAILY, source = "HACCP_WIZARD",
        )

        every { checklistRepo.findAllByOrganizationIdAndSource(orgId, "HACCP_WIZARD") } returns listOf(haccpChecklist)
        every { checklistCompletionRepo.countByChecklistIdAndCompletedAtBetween(5L, any(), any()) } returns 20L
        every { checklistItemRepo.findAllByChecklistIdOrderByIdAsc(5L) } returns listOf(
            ChecklistItem(id = 10L, checklist = haccpChecklist, title = "Rengjør arbeidsflater", completed = true, completedAt = Instant.parse("2026-03-10T08:00:00Z")),
            ChecklistItem(id = 11L, checklist = haccpChecklist, title = "Desinfiser kniver", completed = false),
        )

        val result = service.preview(request, auth)

        val haccp = result.haccpChecklists
        haccp.shouldNotBeNull()
        haccp.checklists shouldHaveSize 1
        haccp.checklists[0].name shouldBe "Daglig rengjøring"
        haccp.checklists[0].completionRate shouldBe 64.5
        haccp.checklists[0].items shouldHaveSize 2
        haccp.checklists[0].items[0].completed shouldBe true
        haccp.checklists[0].items[1].completed shouldBe false
    }

    test("preview throws NotFoundException when organization not found") {
        every { organizationRepo.findById(orgId) } returns Optional.empty()

        shouldThrow<NotFoundException> {
            service.preview(minimalRequest, auth)
        }
    }

    test("preview throws NotFoundException when user not found") {
        every { userRepo.findById(userId) } returns Optional.empty()

        shouldThrow<NotFoundException> {
            service.preview(minimalRequest, auth)
        }
    }

    test("generate creates PDF, uploads to S3, and saves report") {
        every { checklistItemRepo.countAllByOrganizationId(orgId) } returns 10L
        every { checklistItemRepo.countCompletedByOrganizationId(orgId) } returns 10L
        every { reportDataRepo.countFoodDeviationsByStatus(orgId, any(), any(), any()) } returns 0L
        every { reportDataRepo.countAlcoholDeviationsByStatus(orgId, any(), any(), any()) } returns 0L
        every { reportDataRepo.countTempDeviations(orgId, any(), any()) } returns 0L

        val pdfBytes = "fake-pdf".toByteArray()
        every { pdfGenerator.generatePdf(any()) } returns pdfBytes

        val document = Document(
            id = 100L, organizationId = orgId, s3Key = "reports/test.pdf",
            fileName = "rapport_2026-03-01_2026-03-31.pdf",
            contentType = "application/pdf", uploadedByUser = user,
        )
        every { documentsService.uploadBytes(pdfBytes, any(), "application/pdf", "reports", auth) } returns document
        every { documentsService.getFileUrl(100L, orgId) } returns "https://s3.example.com/presigned"

        val savedReport = GeneratedReport(
            id = 1L, organizationId = orgId,
            title = "Internkontroll Rapport 01.03.2026 – 31.03.2026",
            periodFrom = LocalDate.of(2026, 3, 1),
            periodTo = LocalDate.of(2026, 3, 31),
            generatedByUser = user, document = document,
            config = "{}",
        )
        every { generatedReportRepo.save(any()) } returns savedReport

        val result = service.generate(minimalRequest, auth)

        result.id shouldBe 1L
        result.title shouldBe "Internkontroll Rapport 01.03.2026 – 31.03.2026"
        result.periodFrom shouldBe "01.03.2026"
        result.periodTo shouldBe "31.03.2026"
        result.generatedByUserName shouldBe "Admin User"
        result.downloadUrl shouldBe "https://s3.example.com/presigned"
        result.documentId shouldBe 100L

        verify { pdfGenerator.generatePdf(any()) }
        verify { documentsService.uploadBytes(pdfBytes, any(), "application/pdf", "reports", auth) }
        verify { generatedReportRepo.save(any()) }
    }

    test("generate uses custom title when provided") {
        every { checklistItemRepo.countAllByOrganizationId(orgId) } returns 10L
        every { checklistItemRepo.countCompletedByOrganizationId(orgId) } returns 10L
        every { reportDataRepo.countFoodDeviationsByStatus(orgId, any(), any(), any()) } returns 0L
        every { reportDataRepo.countAlcoholDeviationsByStatus(orgId, any(), any(), any()) } returns 0L
        every { reportDataRepo.countTempDeviations(orgId, any(), any()) } returns 0L
        every { pdfGenerator.generatePdf(any()) } returns "pdf".toByteArray()

        val document = Document(
            id = 101L, organizationId = orgId, s3Key = "reports/test.pdf",
            fileName = "rapport.pdf", contentType = "application/pdf", uploadedByUser = user,
        )
        every { documentsService.uploadBytes(any(), any(), any(), any(), any()) } returns document
        every { documentsService.getFileUrl(101L, orgId) } returns "https://s3.example.com/url"
        every { generatedReportRepo.save(any()) } answers {
            firstArg<GeneratedReport>().copy(id = 2L)
        }

        val request = minimalRequest.copy(title = "Mars Rapport 2026")
        val result = service.generate(request, auth)

        result.title shouldBe "Mars Rapport 2026"
    }

    test("list returns all reports for the organization") {
        val report1 = GeneratedReport(
            id = 1L, organizationId = orgId, title = "Report 1",
            periodFrom = LocalDate.of(2026, 3, 1), periodTo = LocalDate.of(2026, 3, 31),
            generatedByUser = user, config = "{}",
        )
        val report2 = GeneratedReport(
            id = 2L, organizationId = orgId, title = "Report 2",
            periodFrom = LocalDate.of(2026, 2, 1), periodTo = LocalDate.of(2026, 2, 28),
            generatedByUser = user, config = "{}",
        )

        every { generatedReportRepo.findAllByOrganizationIdOrderByGeneratedAtDesc(orgId) } returns listOf(report1, report2)

        val result = service.list(auth)

        result shouldHaveSize 2
        result[0].title shouldBe "Report 1"
        result[1].title shouldBe "Report 2"
    }

    test("getReport returns report with download URL") {
        val document = Document(
            id = 100L, organizationId = orgId, s3Key = "reports/test.pdf",
            fileName = "rapport.pdf", contentType = "application/pdf", uploadedByUser = user,
        )
        val report = GeneratedReport(
            id = 1L, organizationId = orgId, title = "Report 1",
            periodFrom = LocalDate.of(2026, 3, 1), periodTo = LocalDate.of(2026, 3, 31),
            generatedByUser = user, document = document, config = "{}",
        )

        every { generatedReportRepo.findByIdAndOrganizationId(1L, orgId) } returns report
        every { documentsService.getFileUrl(100L, orgId) } returns "https://s3.example.com/presigned"

        val result = service.getReport(1L, auth)

        result.id shouldBe 1L
        result.downloadUrl shouldBe "https://s3.example.com/presigned"
        result.documentId shouldBe 100L
    }

    test("getReport throws NotFoundException when not found") {
        every { generatedReportRepo.findByIdAndOrganizationId(99L, orgId) } returns null

        shouldThrow<NotFoundException> {
            service.getReport(99L, auth)
        }
    }

    test("getDownloadUrl returns presigned URL") {
        val document = Document(
            id = 100L, organizationId = orgId, s3Key = "reports/test.pdf",
            fileName = "rapport.pdf", contentType = "application/pdf", uploadedByUser = user,
        )
        val report = GeneratedReport(
            id = 1L, organizationId = orgId, title = "Report",
            periodFrom = LocalDate.of(2026, 3, 1), periodTo = LocalDate.of(2026, 3, 31),
            generatedByUser = user, document = document, config = "{}",
        )

        every { generatedReportRepo.findByIdAndOrganizationId(1L, orgId) } returns report
        every { documentsService.getFileUrl(100L, orgId) } returns "https://s3.example.com/download"

        val url = service.getDownloadUrl(1L, auth)

        url shouldBe "https://s3.example.com/download"
    }

    test("getDownloadUrl throws NotFoundException when report not found") {
        every { generatedReportRepo.findByIdAndOrganizationId(99L, orgId) } returns null

        shouldThrow<NotFoundException> {
            service.getDownloadUrl(99L, auth)
        }
    }

    test("getDownloadUrl throws NotFoundException when document is null") {
        val report = GeneratedReport(
            id = 1L, organizationId = orgId, title = "Report",
            periodFrom = LocalDate.of(2026, 3, 1), periodTo = LocalDate.of(2026, 3, 31),
            generatedByUser = user, document = null, config = "{}",
        )

        every { generatedReportRepo.findByIdAndOrganizationId(1L, orgId) } returns report

        shouldThrow<NotFoundException> {
            service.getDownloadUrl(1L, auth)
        }
    }

    test("delete removes report and associated document") {
        val document = Document(
            id = 100L, organizationId = orgId, s3Key = "reports/test.pdf",
            fileName = "rapport.pdf", contentType = "application/pdf", uploadedByUser = user,
        )
        val report = GeneratedReport(
            id = 1L, organizationId = orgId, title = "Report",
            periodFrom = LocalDate.of(2026, 3, 1), periodTo = LocalDate.of(2026, 3, 31),
            generatedByUser = user, document = document, config = "{}",
        )

        every { generatedReportRepo.findByIdAndOrganizationId(1L, orgId) } returns report
        every { documentsService.deleteFile(100L, orgId) } just runs
        every { generatedReportRepo.delete(report) } just runs

        service.delete(1L, auth)

        verify { documentsService.deleteFile(100L, orgId) }
        verify { generatedReportRepo.delete(report) }
    }

    test("delete removes report without document") {
        val report = GeneratedReport(
            id = 1L, organizationId = orgId, title = "Report",
            periodFrom = LocalDate.of(2026, 3, 1), periodTo = LocalDate.of(2026, 3, 31),
            generatedByUser = user, document = null, config = "{}",
        )

        every { generatedReportRepo.findByIdAndOrganizationId(1L, orgId) } returns report
        every { generatedReportRepo.delete(report) } just runs

        service.delete(1L, auth)

        verify(exactly = 0) { documentsService.deleteFile(any(), any()) }
        verify { generatedReportRepo.delete(report) }
    }

    test("delete throws NotFoundException when report not found") {
        every { generatedReportRepo.findByIdAndOrganizationId(99L, orgId) } returns null

        shouldThrow<NotFoundException> {
            service.delete(99L, auth)
        }
    }

    test("exportJson returns same data as preview") {
        every { checklistItemRepo.countAllByOrganizationId(orgId) } returns 10L
        every { checklistItemRepo.countCompletedByOrganizationId(orgId) } returns 10L
        every { reportDataRepo.countFoodDeviationsByStatus(orgId, any(), any(), any()) } returns 0L
        every { reportDataRepo.countAlcoholDeviationsByStatus(orgId, any(), any(), any()) } returns 0L
        every { reportDataRepo.countTempDeviations(orgId, any(), any()) } returns 0L

        val result = service.exportJson(minimalRequest, auth)

        result.header.organizationName shouldBe "Test Restaurant AS"
        result.complianceSummary.shouldNotBeNull()
    }
})
