package com.iksystem.common.notifications.schedulers

import com.iksystem.common.notifications.model.NotificationType
import com.iksystem.common.notifications.model.ReferenceType
import com.iksystem.common.notifications.service.NotificationsService
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Scheduler responsible for checking upcoming alcohol license (bevilling) expirations.
 *
 * Runs daily and:
 * - Identifies licenses expiring in 90 days
 * - Sends notifications to organization admins and managers
 * - Marks licenses as notified to prevent duplicate alerts
 */
@Component
class BevillingExpiryScheduler(
    private val jdbcTemplate: JdbcTemplate,
    private val notificationsService: NotificationsService,
) {
    private val log = LoggerFactory.getLogger(BevillingExpiryScheduler::class.java)
    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    /**
     * Scheduled task that runs every day at 08:00.
     *
     * Performs the following steps:
     * 1. Calculates the target expiry date (90 days from today)
     * 2. Queries for alcohol policies expiring on that date and not yet notified
     * 3. Sends notifications to admins/managers of each organization
     * 4. Updates records to mark them as notified
     */
    @Scheduled(cron = "0 0 8 * * *")
    fun checkExpiringBevillinger() {
        log.info("Running bevilling expiry check")

        val expiryDate = LocalDate.now().plusDays(90)

        val expiringPolicies = jdbcTemplate.queryForList(
            """
            SELECT id, organization_id, bevilling_number, bevilling_valid_until
            FROM alcohol_policies
            WHERE bevilling_valid_until = ? AND expiry_notified = false
            """,
            expiryDate
        )

        if (expiringPolicies.isEmpty()) {
            log.info("No expiring bevillinger found")
            return
        }

        log.info("Found {} expiring bevillinger", expiringPolicies.size)

        expiringPolicies.forEach { row ->
            val id = row["id"] as Long
            val organizationId = row["organization_id"] as Long
            val bevillingNumber = row["bevilling_number"] as? String ?: "ukjent"
            val validUntil = (row["bevilling_valid_until"] as java.sql.Date).toLocalDate()
            val formattedDate = validUntil.format(dateFormatter)

            val message = "Skjenkebevillingen (nr. $bevillingNumber) utløper $formattedDate. Husk å søke om fornyelse."

            notificationsService.sendToOrgAdminsAndManagers(
                organizationId = organizationId,
                type = NotificationType.BEVILLING_EXPIRY,
                title = "Bevillingen utløper snart",
                message = message,
                referenceType = ReferenceType.ALCOHOL_POLICY,
                referenceId = id,
            )

            jdbcTemplate.update(
                "UPDATE alcohol_policies SET expiry_notified = true WHERE id = ?",
                id
            )
        }
    }
}