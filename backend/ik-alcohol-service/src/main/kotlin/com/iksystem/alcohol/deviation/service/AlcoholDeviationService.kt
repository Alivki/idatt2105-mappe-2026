package com.iksystem.alcohol.deviation.service

import com.iksystem.alcohol.deviation.dto.AlcoholDeviationResponse
import com.iksystem.alcohol.deviation.dto.CreateAlcoholDeviationRequest
import com.iksystem.alcohol.deviation.dto.UpdateAlcoholDeviationRequest
import com.iksystem.alcohol.deviation.model.AlcoholDeviation
import com.iksystem.alcohol.deviation.model.AlcoholReportSource
import com.iksystem.alcohol.deviation.repository.AlcoholDeviationRepository
import com.iksystem.alcohol.penaltypoints.service.PenaltyPointService
import com.iksystem.common.exception.BadRequestException
import com.iksystem.common.exception.NotFoundException
import com.iksystem.common.membership.repository.MembershipRepository
import com.iksystem.common.notifications.model.NotificationType
import com.iksystem.common.notifications.model.ReferenceType
import com.iksystem.common.notifications.service.NotificationsService
import com.iksystem.common.security.AuthenticatedUser
import com.iksystem.common.user.model.User
import com.iksystem.common.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * Service responsible for managing alcohol deviations.
 *
 * Handles retrieval, creation, update, and deletion of deviations,
 * as well as related validation, notifications, and automatic
 * penalty point creation for certain report sources.
 *
 * All operations are scoped to the authenticated user's organization.
 */
@Service
class AlcoholDeviationService(
    private val repository: AlcoholDeviationRepository,
    private val userRepository: UserRepository,
    private val membershipRepository: MembershipRepository,
    private val penaltyPointService: PenaltyPointService,
    private val notificationsService: NotificationsService,
) {

    /**
     * Retrieves all alcohol deviations for the authenticated user's organization.
     *
     * Results are ordered by reported time descending.
     *
     * @param auth The authenticated user
     * @return List of deviation responses
     */
    @Transactional(readOnly = true)
    fun list(auth: AuthenticatedUser): List<AlcoholDeviationResponse> {
        val orgId = auth.requireOrganizationId()
        return repository.findAllByOrganizationIdOrderByReportedAtDesc(orgId).map { it.toResponse() }
    }

    /**
     * Retrieves a specific alcohol deviation by ID.
     *
     * @param id The deviation ID
     * @param auth The authenticated user
     * @return The matching deviation response
     * @throws NotFoundException If the deviation does not exist in the organization
     */
    @Transactional(readOnly = true)
    fun getById(id: Long, auth: AuthenticatedUser): AlcoholDeviationResponse {
        val orgId = auth.requireOrganizationId()
        return requireDeviation(id, orgId).toResponse()
    }

    /**
     * Creates a new alcohol deviation.
     *
     * If the report source is SJENKEKONTROLL or POLITIRAPPORT,
     * penalty points are automatically created for the deviation.
     *
     * Notifications are also sent to relevant users after creation.
     *
     * @param request Request containing deviation details
     * @param auth The authenticated user
     * @return The created deviation response
     */
    @Transactional
    fun create(request: CreateAlcoholDeviationRequest, auth: AuthenticatedUser): AlcoholDeviationResponse {
        val orgId = auth.requireOrganizationId()
        val reporter = requireUser(auth.userId)
        val responsibleUser = resolveOrgMember(request.preventiveResponsibleUserId, orgId)

        val deviation = repository.save(
            AlcoholDeviation(
                organizationId = orgId,
                reportedAt = request.reportedAt?.let { Instant.parse(it) } ?: Instant.now(),
                reportedByUser = reporter,
                reportSource = request.reportSource,
                deviationType = request.deviationType,
                description = request.description.trim(),
                immediateAction = request.immediateAction?.trim()?.takeIf { it.isNotEmpty() },
                causalAnalysis = request.causalAnalysis,
                causalExplanation = request.causalExplanation?.trim()?.takeIf { it.isNotEmpty() },
                preventiveMeasures = request.preventiveMeasures?.trim()?.takeIf { it.isNotEmpty() },
                preventiveDeadline = request.preventiveDeadline?.let { Instant.parse(it) },
                preventiveResponsibleUser = responsibleUser,
            )
        )

        // Auto-add penalty points for sjenkekontroll or politirapport
        if (request.reportSource == AlcoholReportSource.SJENKEKONTROLL ||
            request.reportSource == AlcoholReportSource.POLITIRAPPORT
        ) {
            penaltyPointService.addForDeviation(deviation)
        }

        notifyDeviationCreated(deviation)

        return deviation.toResponse()
    }

    /**
     * Updates an existing alcohol deviation.
     *
     * Only fields provided in the request are updated.
     * Other fields retain their current values.
     *
     * @param id The deviation ID
     * @param request Request containing updated values
     * @param auth The authenticated user
     * @return The updated deviation response
     */
    @Transactional
    fun update(id: Long, request: UpdateAlcoholDeviationRequest, auth: AuthenticatedUser): AlcoholDeviationResponse {
        val orgId = auth.requireOrganizationId()
        val existing = requireDeviation(id, orgId)

        val responsibleUser = if (request.preventiveResponsibleUserId != null)
            resolveOrgMember(request.preventiveResponsibleUserId, orgId) else existing.preventiveResponsibleUser

        val updated = repository.save(
            existing.copy(
                reportedAt = request.reportedAt?.let { Instant.parse(it) } ?: existing.reportedAt,
                reportSource = request.reportSource ?: existing.reportSource,
                deviationType = request.deviationType ?: existing.deviationType,
                description = request.description?.trim()?.takeIf { it.isNotEmpty() } ?: existing.description,
                immediateAction = request.immediateAction?.trim()?.takeIf { it.isNotEmpty() } ?: existing.immediateAction,
                causalAnalysis = request.causalAnalysis ?: existing.causalAnalysis,
                causalExplanation = request.causalExplanation?.trim()?.takeIf { it.isNotEmpty() } ?: existing.causalExplanation,
                preventiveMeasures = request.preventiveMeasures?.trim()?.takeIf { it.isNotEmpty() } ?: existing.preventiveMeasures,
                preventiveDeadline = request.preventiveDeadline?.let { Instant.parse(it) } ?: existing.preventiveDeadline,
                preventiveResponsibleUser = responsibleUser,
                status = request.status ?: existing.status,
                updatedAt = Instant.now(),
            )
        )
        return updated.toResponse()
    }

    /**
     * Deletes an alcohol deviation.
     *
     * @param id The deviation ID
     * @param auth The authenticated user
     * @throws NotFoundException If the deviation does not exist in the organization
     */
    @Transactional
    fun delete(id: Long, auth: AuthenticatedUser) {
        val orgId = auth.requireOrganizationId()
        val deviation = requireDeviation(id, orgId)
        repository.delete(deviation)
    }

    /**
     * Sends notifications when a new deviation is created.
     *
     * Administrators and managers in the organization are notified,
     * and the responsible user is also notified if one is assigned.
     *
     * @param deviation The created deviation
     */
    private fun notifyDeviationCreated(deviation: AlcoholDeviation) {
        val message = "Alcohol deviation '${deviation.deviationType}' reported by ${deviation.reportedByUser.fullName}: ${deviation.description.take(100)}"

        notificationsService.sendToOrgAdminsAndManagers(
            organizationId = deviation.organizationId,
            type = NotificationType.DEVIATION_CREATED,
            title = "New Alcohol Deviation: ${deviation.deviationType}",
            message = message,
            referenceType = ReferenceType.DEVIATION,
            referenceId = deviation.id
        )

        val responsibleUser = deviation.preventiveResponsibleUser
        if (responsibleUser != null) {
            notificationsService.send(
                organizationId = deviation.organizationId,
                recipientUserId = responsibleUser.id,
                type = NotificationType.DEVIATION_ASSIGNED,
                title = "You have been assigned a deviation",
                message = message,
                referenceType = ReferenceType.DEVIATION,
                referenceId = deviation.id
            )
        }
    }

    /**
     * Retrieves a deviation by ID within the given organization.
     *
     * @param id The deviation ID
     * @param organizationId The organization ID
     * @return The matching deviation entity
     * @throws NotFoundException If no matching deviation exists
     */
    private fun requireDeviation(id: Long, organizationId: Long): AlcoholDeviation {
        return repository.findByIdAndOrganizationId(id, organizationId)
            ?: throw NotFoundException("Alcohol deviation not found")
    }

    /**
     * Retrieves a user by ID.
     *
     * @param userId The user ID
     * @return The matching user entity
     * @throws NotFoundException If the user does not exist
     */
    private fun requireUser(userId: Long): User {
        return userRepository.findById(userId)
            .orElseThrow { NotFoundException("User not found") }
    }

    /**
     * Resolves a user as a valid member of the organization.
     *
     * Returns null if no user ID is provided.
     *
     * @param userId The user ID, if provided
     * @param organizationId The organization ID
     * @return The user if valid, otherwise null
     * @throws BadRequestException If the user is not a member of the organization
     */
    private fun resolveOrgMember(userId: Long?, organizationId: Long): User? {
        if (userId == null) return null
        val user = requireUser(userId)
        if (!membershipRepository.existsByUserIdAndOrganizationId(user.id, organizationId)) {
            throw BadRequestException("User is not a member of this organization")
        }
        return user
    }
}

/**
 * Maps an [AlcoholDeviation] entity to an [AlcoholDeviationResponse].
 *
 * @return The mapped response DTO
 */
private fun AlcoholDeviation.toResponse() = AlcoholDeviationResponse(
    id = id,
    organizationId = organizationId,
    reportedAt = reportedAt.toString(),
    reportedByUserId = reportedByUser.id,
    reportedByUserName = reportedByUser.fullName,
    reportSource = reportSource,
    deviationType = deviationType,
    description = description,
    immediateAction = immediateAction,
    causalAnalysis = causalAnalysis,
    causalExplanation = causalExplanation,
    preventiveMeasures = preventiveMeasures,
    preventiveDeadline = preventiveDeadline?.toString(),
    preventiveResponsibleUserId = preventiveResponsibleUser?.id,
    preventiveResponsibleUserName = preventiveResponsibleUser?.fullName,
    status = status,
    ageVerificationShiftId = ageVerificationShiftId,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString(),
)