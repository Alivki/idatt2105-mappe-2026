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

/**
 * REST controller for report generation and management.
 *
 * Provides endpoints for:
 * - Previewing reports (JSON)
 * - Generating PDF reports
 * - Listing and retrieving reports
 * - Downloading reports via pre-signed URLs
 * - Deleting reports
 * - Exporting report data as JSON
 *
 * All endpoints are secured and scoped to the authenticated user's organization.
 */
@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reports", description = "Internkontroll rapport generering og administrasjon")
@SecurityRequirement(name = "bearerAuth")
class ReportController(private val reportService: ReportService) {

    /**
     * Generates a preview of a report.
     *
     * Returns structured JSON data used for frontend preview before generating the final PDF.
     *
     * @param request Report generation parameters
     * @param auth Authenticated user
     * @return Report preview data
     */
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

    /**
     * Generates a report as a PDF.
     *
     * The report is stored in S3 and metadata (including download URL) is returned.
     *
     * @param request Report generation parameters
     * @param auth Authenticated user
     * @return Generated report metadata
     */
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

    /**
     * Retrieves all generated reports for the organization.
     *
     * @param auth Authenticated user
     * @return List of reports
     */
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

    /**
     * Retrieves a specific report by ID.
     *
     * @param id Report ID
     * @param auth Authenticated user
     * @return Report metadata
     */
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

    /**
     * Retrieves a pre-signed download URL for a report.
     *
     * @param id Report ID
     * @param auth Authenticated user
     * @return Map containing the download URL
     */
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

    /**
     * Deletes a report and its associated document from storage.
     *
     * Requires ADMIN role.
     *
     * @param id Report ID
     * @param auth Authenticated user
     * @return No content response
     */
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

    /**
     * Exports report data as JSON.
     *
     * Intended for system integrations or external processing.
     *
     * @param request Report generation parameters
     * @param auth Authenticated user
     * @return Report data in JSON format
     */
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