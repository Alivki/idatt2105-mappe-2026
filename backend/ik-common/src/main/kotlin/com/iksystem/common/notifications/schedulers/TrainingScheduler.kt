package com.iksystem.common.notifications.schedulers

import com.iksystem.common.notifications.model.NotificationType
import com.iksystem.common.notifications.model.ReferenceType
import com.iksystem.common.notifications.service.NotificationsService
import com.iksystem.common.training.repository.TrainingRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Scheduler responsible for monitoring training records that are about to expire.
 *
 * Runs daily and:
 * - Identifies training logs expiring within the next 30 days
 * - Notifies the affected employee
 * - Notifies organization managers
 */
@Component
class TrainingScheduler(
    private val trainingRepository: TrainingRepository,
    private val notificationService: NotificationsService,
) {

    /**
     * Scheduled task that runs every day at 08:00.
     *
     * Performs the following:
     * 1. Calculates the time window (now → 30 days ahead)
     * 2. Retrieves training logs expiring within that window
     * 3. Sends notifications to:
     *    - The employee whose training is expiring
     *    - Organization managers
     */
    @Scheduled(cron = "0 0 8 * * *")
    fun checkTrainingExpiries() {
        val now = Instant.now()
        val thirtyDaysFromNow = now.plus(30, ChronoUnit.DAYS)

        val expiringLogs = trainingRepository.findExpiringSoon(now, thirtyDaysFromNow)

        expiringLogs.forEach { log ->
            val message = "Training '${log.title}' for ${log.employeeUser.fullName} expires on ${log.expiresAt}"

            notificationService.send(
                organizationId = log.organizationId,
                recipientUserId = log.employeeUser.id,
                type = NotificationType.TRAINING_EXPIRY,
                title = "Training Expiring Soon",
                message = message,
                referenceType = ReferenceType.TRAINING_LOG,
                referenceId = log.id
            )

            notificationService.sendToOrgManagers(
                organizationId = log.organizationId,
                type = NotificationType.TRAINING_EXPIRY,
                title = "Staff Training Expiry: ${log.employeeUser.fullName}",
                message = message,
                referenceType = ReferenceType.TRAINING_LOG,
                referenceId = log.id
            )
        }
    }
}