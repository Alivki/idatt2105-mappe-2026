package com.iksystem.common.report.controller

import com.iksystem.common.report.dto.GenerateReportRequest
import com.iksystem.common.report.dto.GeneratedReportResponse
import com.iksystem.common.report.dto.ReportPreviewResponse
import com.iksystem.common.report.service.ReportService
import com.iksystem.common.security.AuthenticatedUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reports", description = "Internkontroll rapport generering og administrasjon")
@SecurityRequirement(name = "bearerAuth")
class ReportController(private val reportService: ReportService) {

    @Operation(summary = "Forhåndsvis rapport", description = "Returnerer JSON-data for forhåndsvisning av rapport i frontend")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Forhåndsvisningsdata returnert"),
        ApiResponse(responseCode = "400", description = "Ugyldig forespørsel"),
    )
    @PostMapping("/preview")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun preview(
        @Valid @RequestBody request: GenerateReportRequest,
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<ReportPreviewResponse> {
        return ResponseEntity.ok(reportService.preview(request, auth))
    }

    @Operation(summary = "Generer rapport", description = "Genererer PDF, lagrer i S3, og returnerer rapportmetadata med nedlastings-URL")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Rapport generert og lagret"),
        ApiResponse(responseCode = "400", description = "Ugyldig forespørsel"),
    )
    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun generate(
        @Valid @RequestBody request: GenerateReportRequest,
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<GeneratedReportResponse> {
        return ResponseEntity.status(HttpStatus.CREATED).body(reportService.generate(request, auth))
    }

    @Operation(summary = "List rapporter", description = "Returnerer alle genererte rapporter for organisasjonen")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Rapportliste returnert"),
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun list(
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<List<GeneratedReportResponse>> {
        return ResponseEntity.ok(reportService.list(auth))
    }

    @Operation(summary = "Hent rapport", description = "Returnerer rapportmetadata og nedlastings-URL")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Rapport funnet"),
        ApiResponse(responseCode = "404", description = "Rapport ikke funnet"),
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun getReport(
        @PathVariable id: Long,
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<GeneratedReportResponse> {
        return ResponseEntity.ok(reportService.getReport(id, auth))
    }

    @Operation(summary = "Last ned rapport", description = "Returnerer en forhåndssignert S3-URL for nedlasting av rapporten")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Nedlastings-URL returnert"),
        ApiResponse(responseCode = "404", description = "Rapport eller dokument ikke funnet"),
    )
    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun download(
        @PathVariable id: Long,
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<Map<String, String>> {
        val url = reportService.getDownloadUrl(id, auth)
        return ResponseEntity.ok(mapOf("url" to url))
    }

    @Operation(summary = "Slett rapport", description = "Sletter rapport og tilhørende dokument fra S3. Krever ADMIN-rolle.")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Rapport slettet"),
        ApiResponse(responseCode = "404", description = "Rapport ikke funnet"),
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun delete(
        @PathVariable id: Long,
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<Void> {
        reportService.delete(id, auth)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "Eksporter som JSON", description = "Returnerer rapportdata som JSON for systemintegrasjon")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "JSON-data returnert"),
    )
    @PostMapping("/export-json")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun exportJson(
        @Valid @RequestBody request: GenerateReportRequest,
        @AuthenticationPrincipal auth: AuthenticatedUser,
    ): ResponseEntity<ReportPreviewResponse> {
        return ResponseEntity.ok(reportService.exportJson(request, auth))
    }
}
