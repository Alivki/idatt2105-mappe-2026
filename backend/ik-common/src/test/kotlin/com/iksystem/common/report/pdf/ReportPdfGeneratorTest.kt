package com.iksystem.common.report.pdf

import com.iksystem.common.report.dto.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ReportPdfGeneratorTest {

    @Test
    fun `generatePdf produces bytes for minimal report`() {
        val generator = ReportPdfGenerator()
        val data = ReportPreviewResponse(
            header = ReportHeader(
                organizationName = "Test Org AS",
                orgNumber = "123 456 789",
                periodFrom = "01.03.2026",
                periodTo = "31.03.2026",
                generatedDate = "01.04.2026",
                generatedByName = "Test User",
                generatedByRole = "Administrator",
            ),
            complianceSummary = ComplianceSummary(
                complianceRate = 93.1,
                totalTasks = 248,
                completedTasks = 239,
                overdueTasks = 9,
                openDeviations = 3,
                closedDeviations = 14,
                temperatureDeviations = 2,
                alcoholIncidents = 1,
            ),
            temperatureLogs = listOf(
                TemperatureLogEntry("Walk-in Cooler A", 3.1, 2.4, 4.8, 0, "Normal"),
                TemperatureLogEntry("Display Fridge", 4.9, 3.2, 8.1, 1, "Kritisk"),
            ),
            checklists = listOf(
                ChecklistCompletion(1, "Kitchen – Main", "DAILY", 100.0, 10, 10),
                ChecklistCompletion(2, "Bar Counter", "DAILY", 92.0, 23, 25),
            ),
            correctiveActions = listOf(
                CorrectiveActionEntry("CA-042", "08.03.2026", "Cooler exceeded 7°C", "Compressor serviced", "Lukket"),
            ),
            foodDeviations = listOf(
                DeviationEntry(1, "08.03.2026", "TEMPERATUR", "HIGH", "Temp too high", null, "Åpen", "Test User"),
            ),
            alcoholDeviations = listOf(
                DeviationEntry(17, "12.03.2026", "UNDERAGE", "–", "Underage attempt", "ID confiscated", "Lukket", "Test User"),
            ),
            ageVerificationLogs = listOf(
                AgeVerificationEntry("05.03.2026", "Kveld", 14, 14, 2),
            ),
            trainingOverview = listOf(
                TrainingEntry("Kari Nordmann", "Bartender", "Ansvarlig Vertskap", "15.09.2026", "Gyldig"),
                TrainingEntry("Ola Hansen", "Bartender", "Ansvarlig Vertskap", "02.04.2026", "Utløper snart"),
            ),
            licenseInfo = LicenseInfoSection(
                bevillingNumber = "SKJ-2024-0891",
                bevillingValidTo = "31.12.2028",
                holder = "Test Org AS",
                styrerName = "Erik Bakken",
                stedfortrederName = "Kari Nordmann",
                kunnskapsproveType = "SKJENKE",
                kunnskapsproveCandidateName = "Erik Bakken",
                kunnskapsproveBirthDate = "01.01.1985",
                kunnskapsprovePassedDate = "01.06.2024",
                kunnskapsproveMunicipality = "Trondheim",
            ),
            signOff = SignOffEntry(
                name = "Erik Bakken",
                title = "Daglig leder",
                date = "01.04.2026",
                comments = "All corrective actions for March have been addressed.",
            ),
            haccpChecklists = null,
        )

        val pdfBytes = generator.generatePdf(data)
        assertTrue(pdfBytes.isNotEmpty(), "PDF should not be empty")
        // PDF files start with %PDF
        val header = String(pdfBytes.take(5).toByteArray())
        assertTrue(header.startsWith("%PDF"), "Output should be a valid PDF, got: $header")
    }
}
