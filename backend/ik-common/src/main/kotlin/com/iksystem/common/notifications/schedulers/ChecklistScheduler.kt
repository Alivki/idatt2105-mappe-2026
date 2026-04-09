package com.iksystem.common.notifications.schedulers

import com.iksystem.common.checklist.model.ChecklistFrequency
import com.iksystem.common.checklist.repository.ChecklistItemRepository
import com.iksystem.common.checklist.repository.ChecklistRepository
import com.iksystem.common.notifications.model.NotificationType
import com.iksystem.common.notifications.model.ReferenceType
import com.iksystem.common.notifications.service.NotificationsService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * Scheduler responsible for monitoring incomplete checklists and sending overdue notifications.
 *
 * Handles different checklist frequencies:
 * - Daily
 * - Weekly
 * - Monthly
 * - Yearly
 *
 * For each frequency, it:
 * - Finds incomplete checklists
 * - Counts incomplete items
 * - Sends notifications to organization admins and managers
 */
@Component
class ChecklistScheduler(
    private val checklistRepository: ChecklistRepository,
    private val checklistItemRepository: ChecklistItemRepository,
    private val notificationsService: NotificationsService,
) {
    private val log = LoggerFactory.getLogger(ChecklistScheduler::class.java)

    /**
     * Checks daily checklists for incomplete items.
     *
     * Runs every day at 22:00 (end of business day).
     */
    @Scheduled(cron = "0 0 22 * * *")
    fun checkDailyChecklists() = notifyIncompleteChecklists(ChecklistFrequency.DAILY)

    /**
     * Checks weekly checklists for incomplete items.
     *
     * Runs every Sunday at 22:00.
     */
    @Scheduled(cron = "0 0 22 * * SUN")
    fun checkWeeklyChecklists() = notifyIncompleteChecklists(ChecklistFrequency.WEEKLY)

    /**
     * Checks monthly checklists for incomplete items.
     *
     * Runs on the last day of each month at 22:00.
     */
    @Scheduled(cron = "0 0 22 L * *")
    fun checkMonthlyChecklists() = notifyIncompleteChecklists(ChecklistFrequency.MONTHLY)

    /**
     * Checks yearly checklists for incomplete items.
     *
     * Runs on December 31st at 22:00.
     */
    @Scheduled(cron = "0 0 22 31 12 *")
    fun checkYearlyChecklists() = notifyIncompleteChecklists(ChecklistFrequency.YEARLY)

    /**
     * Finds incomplete checklists for a given frequency and sends notifications.
     *
     * For each checklist:
     * - Counts incomplete items
     * - Sends a notification to admins and managers
     *
     * @param frequency The checklist frequency to evaluate
     */
    private fun notifyIncompleteChecklists(frequency: ChecklistFrequency) {
        log.info("Checking {} checklists for overdue items", frequency)
        val incompleteChecklists = checklistRepository.findIncompleteByFrequency(frequency)

        if (incompleteChecklists.isEmpty()) {
            log.info("No incomplete {} checklists found", frequency)
            return
        }

        log.info("Found {} incomplete {} checklists, sending notifications", incompleteChecklists.size, frequency)
        incompleteChecklists.forEach { checklist ->
            val incompleteCount = checklistItemRepository.countByChecklistIdAndCompletedFalse(checklist.id)
            val message = "Checklist '${checklist.name}' ($frequency) has $incompleteCount incomplete item(s) and is overdue."

            notificationsService.sendToOrgAdminsAndManagers(
                organizationId = checklist.organizationId,
                type = NotificationType.CHECKLIST_OVERDUE,
                title = "Checklist Overdue: ${checklist.name}",
                message = message,
                referenceType = ReferenceType.CHECKLIST,
                referenceId = checklist.id
            )
        }
    }
}