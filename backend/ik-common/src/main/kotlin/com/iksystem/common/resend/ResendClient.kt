package com.iksystem.common.resend

import com.iksystem.common.config.ResendConfig
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.http.*

/**
 * Client for sending emails via the Resend API.
 */
@Component
class ResendClient(private val props: ResendConfig) {
    private val log = LoggerFactory.getLogger(ResendClient::class.java)
    private val restTemplate = RestTemplate()

    /**
     * Sends an email with given recipient, subject, and HTML content.
     * Skips sending if devMode is enabled.
     */
    fun sendEmail(to: String, subject: String, html: String) {
        if (props.devMode) {
            log.debug("DEV MODE: Skipping email to {} with subject: {}", to, subject)
            return
        }

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(props.apiKey)

        val body = mapOf(
            "from" to props.fromEmail,
            "to" to listOf(to),
            "subject" to subject,
            "html" to html
        )

        val request = HttpEntity(body, headers)

        restTemplate.exchange(
            "https://api.resend.com/emails",
            HttpMethod.POST,
            request,
            String::class.java
        )
    }
}