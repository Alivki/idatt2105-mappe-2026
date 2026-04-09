package com.iksystem.common.report.controller

import com.iksystem.common.exception.NotFoundException
import com.iksystem.common.report.dto.*
import com.iksystem.common.report.service.ReportService
import com.iksystem.common.security.AuthenticatedUser
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.springframework.http.HttpStatus

class ReportControllerTest : FunSpec({

    val reportService = mockk<ReportService>()
    val controller = ReportController(reportService)

    val auth = AuthenticatedUser(userId = 1L, organizationId = 10L, role = "ADMIN")

    val request = GenerateReportRequest(
        periodFrom = "2026-03-01",
        periodTo = "2026-03-31",
        sections = ReportSectionsConfig(),
    )

    val previewResponse = ReportPreviewResponse(
        header = ReportHeader(
            organizationName = "Test Org AS",
            orgNumber = "123 456 789",
            periodFrom = "01.03.2026",
            periodTo = "31.03.2026",
            generatedDate = "01.04.2026",
            generatedByName = "Admin User",
            generatedByRole = "ADMIN",
        ),
    )

    val generatedResponse = GeneratedReportResponse(
        id = 1L,
        title = "Internkontroll Rapport 01.03.2026 – 31.03.2026",
        periodFrom = "01.03.2026",
        periodTo = "31.03.2026",
        generatedByUserName = "Admin User",
        generatedAt = "01.04.2026 10:00",
        documentId = 100L,
        downloadUrl = "https://s3.example.com/presigned",
    )

    beforeTest {
        clearMocks(reportService)
    }

    // ── preview ──

    test("preview returns 200 with preview data") {
        every { reportService.preview(request, auth) } returns previewResponse

        val result = controller.preview(request, auth)

        result.statusCode shouldBe HttpStatus.OK
        result.body!!.header.organizationName shouldBe "Test Org AS"
        result.body!!.header.periodFrom shouldBe "01.03.2026"
    }

    // ── generate ──

    test("generate returns 201 with report metadata") {
        every { reportService.generate(request, auth) } returns generatedResponse

        val result = controller.generate(request, auth)

        result.statusCode shouldBe HttpStatus.CREATED
        result.body!!.id shouldBe 1L
        result.body!!.title shouldBe "Internkontroll Rapport 01.03.2026 – 31.03.2026"
        result.body!!.downloadUrl shouldBe "https://s3.example.com/presigned"
    }

    // ── list ──

    test("list returns 200 with report list") {
        every { reportService.list(auth) } returns listOf(generatedResponse)

        val result = controller.list(auth)

        result.statusCode shouldBe HttpStatus.OK
        result.body!!.size shouldBe 1
        result.body!![0].id shouldBe 1L
    }

    test("list returns 200 with empty list when no reports") {
        every { reportService.list(auth) } returns emptyList()

        val result = controller.list(auth)

        result.statusCode shouldBe HttpStatus.OK
        result.body!!.size shouldBe 0
    }

    // ── getReport ──

    test("getReport returns 200 with report data") {
        every { reportService.getReport(1L, auth) } returns generatedResponse

        val result = controller.getReport(1L, auth)

        result.statusCode shouldBe HttpStatus.OK
        result.body!!.id shouldBe 1L
        result.body!!.downloadUrl shouldBe "https://s3.example.com/presigned"
    }

    test("getReport propagates NotFoundException") {
        every { reportService.getReport(99L, auth) } throws NotFoundException("Rapport ikke funnet")

        shouldThrow<NotFoundException> {
            controller.getReport(99L, auth)
        }
    }

    // ── download ──

    test("download returns 200 with presigned URL") {
        every { reportService.getDownloadUrl(1L, auth) } returns "https://s3.example.com/download"

        val result = controller.download(1L, auth)

        result.statusCode shouldBe HttpStatus.OK
        result.body!!["url"] shouldBe "https://s3.example.com/download"
    }

    test("download propagates NotFoundException when report not found") {
        every { reportService.getDownloadUrl(99L, auth) } throws NotFoundException("Rapport ikke funnet")

        shouldThrow<NotFoundException> {
            controller.download(99L, auth)
        }
    }

    // ── delete ──

    test("delete returns 204 no content") {
        every { reportService.delete(1L, auth) } just runs

        val result = controller.delete(1L, auth)

        result.statusCode shouldBe HttpStatus.NO_CONTENT
        verify { reportService.delete(1L, auth) }
    }

    test("delete propagates NotFoundException") {
        every { reportService.delete(99L, auth) } throws NotFoundException("Rapport ikke funnet")

        shouldThrow<NotFoundException> {
            controller.delete(99L, auth)
        }
    }

    // ── exportJson ──

    test("exportJson returns 200 with same data as preview") {
        every { reportService.exportJson(request, auth) } returns previewResponse

        val result = controller.exportJson(request, auth)

        result.statusCode shouldBe HttpStatus.OK
        result.body!!.header.organizationName shouldBe "Test Org AS"
    }
})
