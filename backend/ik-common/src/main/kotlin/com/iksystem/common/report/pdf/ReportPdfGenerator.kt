package com.iksystem.common.report.pdf

import com.iksystem.common.report.dto.ReportPreviewResponse
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import java.io.ByteArrayOutputStream

@Component
class ReportPdfGenerator {

    private val log = LoggerFactory.getLogger(ReportPdfGenerator::class.java)

    private val templateEngine: TemplateEngine = TemplateEngine().apply {
        setTemplateResolver(ClassLoaderTemplateResolver().apply {
            prefix = "templates/"
            suffix = ".html"
            templateMode = TemplateMode.HTML
            characterEncoding = "UTF-8"
        })
    }

    fun generatePdf(data: ReportPreviewResponse): ByteArray {
        val context = Context().apply {
            setVariable("report", data)
            setVariable("header", data.header)
            setVariable("complianceSummary", data.complianceSummary)
            setVariable("temperatureLogs", data.temperatureLogs)
            setVariable("checklists", data.checklists)
            setVariable("haccpChecklists", data.haccpChecklists)
            setVariable("correctiveActions", data.correctiveActions)
            setVariable("foodDeviations", data.foodDeviations)
            setVariable("alcoholDeviations", data.alcoholDeviations)
            setVariable("ageVerificationLogs", data.ageVerificationLogs)
            setVariable("trainingOverview", data.trainingOverview)
            setVariable("licenseInfo", data.licenseInfo)
            setVariable("signOff", data.signOff)
        }

        log.info("Rendering report HTML template...")
        val html: String
        try {
            html = templateEngine.process("report", context)
        } catch (e: Exception) {
            log.error("Failed to render Thymeleaf template: {}", e.message, e)
            throw RuntimeException("Feil ved generering av rapport-HTML: ${e.message}", e)
        }

        log.info("Converting HTML to PDF ({} chars)...", html.length)
        try {
            val outputStream = ByteArrayOutputStream()
            val baseUri = ReportPdfGenerator::class.java.getResource("/templates/")?.toExternalForm() ?: ""
            PdfRendererBuilder()
                .useFastMode()
                .withHtmlContent(html, baseUri)
                .toStream(outputStream)
                .run()

            val bytes = outputStream.toByteArray()
            log.info("PDF generated successfully ({} bytes)", bytes.size)
            return bytes
        } catch (e: Exception) {
            log.error("Failed to convert HTML to PDF: {}", e.message, e)
            throw RuntimeException("Feil ved PDF-konvertering: ${e.message}", e)
        }
    }
}
