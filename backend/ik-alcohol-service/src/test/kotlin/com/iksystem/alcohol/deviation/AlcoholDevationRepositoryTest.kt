package com.iksystem.alcohol.deviation.repository

import com.iksystem.alcohol.deviation.model.*
import com.iksystem.common.user.model.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import java.time.Instant
import java.time.temporal.ChronoUnit

@DataJpaTest
class AlcoholDeviationRepositoryTest {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var repository: AlcoholDeviationRepository

    private lateinit var reporter: User
    private val orgId = 1L
    private val otherOrgId = 2L

    @BeforeEach
    fun setUp() {
        reporter = entityManager.persist(
            User(email = "reporter@test.com", fullName = "Test Reporter")
        )
        entityManager.flush()
    }

    private fun saveDeviation(
        organizationId: Long = orgId,
        reportedAt: Instant = Instant.now(),
        deviationType: AlcoholDeviationType = AlcoholDeviationType.BERUSET_PERSON_I_LOKALET,
    ): AlcoholDeviation {
        val deviation = AlcoholDeviation(
            organizationId = organizationId,
            reportedAt = reportedAt,
            reportedByUser = reporter,
            reportSource = AlcoholReportSource.EGENRAPPORT,
            deviationType = deviationType,
            description = "Test deviation for org $organizationId",
        )
        val saved = entityManager.persist(deviation)
        entityManager.flush()
        return saved
    }

    // find all by orgId

    @Test
    fun `findAllByOrganizationIdOrderByReportedAtDesc returns deviations for correct org`() {
        saveDeviation(orgId)
        saveDeviation(orgId)
        saveDeviation(otherOrgId)

        val result = repository.findAllByOrganizationIdOrderByReportedAtDesc(orgId)

        assertThat(result).hasSize(2)
        assertThat(result).allMatch { it.organizationId == orgId }
    }

    @Test
    fun `findAllByOrganizationIdOrderByReportedAtDesc orders by reportedAt descending`() {
        val older = Instant.now().minus(2, ChronoUnit.DAYS)
        val newer = Instant.now().minus(1, ChronoUnit.DAYS)

        saveDeviation(reportedAt = older)
        saveDeviation(reportedAt = newer)

        val result = repository.findAllByOrganizationIdOrderByReportedAtDesc(orgId)

        assertThat(result).hasSize(2)
        assertThat(result[0].reportedAt).isAfterOrEqualTo(result[1].reportedAt)
    }

    @Test
    fun `findAllByOrganizationIdOrderByReportedAtDesc returns empty list when no deviations`() {
        val result = repository.findAllByOrganizationIdOrderByReportedAtDesc(orgId)

        assertThat(result).isEmpty()
    }

    @Test
    fun `findAllByOrganizationIdOrderByReportedAtDesc does not return deviations from other orgs`() {
        saveDeviation(otherOrgId)

        val result = repository.findAllByOrganizationIdOrderByReportedAtDesc(orgId)

        assertThat(result).isEmpty()
    }

    // Find by id or organizationId

    @Test
    fun `findByIdAndOrganizationId returns deviation when id and orgId match`() {
        val saved = saveDeviation()

        val result = repository.findByIdAndOrganizationId(saved.id, orgId)

        assertThat(result).isNotNull
        assertThat(result!!.id).isEqualTo(saved.id)
        assertThat(result.organizationId).isEqualTo(orgId)
    }

    @Test
    fun `findByIdAndOrganizationId returns null when id does not exist`() {
        val result = repository.findByIdAndOrganizationId(999L, orgId)

        assertThat(result).isNull()
    }

    @Test
    fun `findByIdAndOrganizationId returns null when orgId does not match`() {
        val saved = saveDeviation(orgId)

        val result = repository.findByIdAndOrganizationId(saved.id, otherOrgId)

        assertThat(result).isNull()
    }

    // Integrity

    @Test
    fun `persists all optional fields correctly`() {
        val responsibleUser = entityManager.persist(
            User(email = "responsible@test.com", fullName = "Responsible User")
        )
        entityManager.flush()

        val deviation = AlcoholDeviation(
            organizationId = orgId,
            reportedByUser = reporter,
            reportSource = AlcoholReportSource.SJENKEKONTROLL,
            deviationType = AlcoholDeviationType.SKJENKING_MINDREAARIGE,
            description = "Full deviation",
            immediateAction = "Immediate action taken",
            causalAnalysis = AlcoholCausalAnalysis.MANGLENDE_OPPLAERING,
            causalExplanation = "Explanation here",
            preventiveMeasures = "Preventive steps",
            preventiveDeadline = Instant.now().plus(30, ChronoUnit.DAYS),
            preventiveResponsibleUser = responsibleUser,
            status = AlcoholDeviationStatus.UNDER_TREATMENT,
        )
        val saved = entityManager.persistAndFlush(deviation)
        entityManager.clear()

        val found = repository.findByIdAndOrganizationId(saved.id, orgId)!!

        assertThat(found.immediateAction).isEqualTo("Immediate action taken")
        assertThat(found.causalAnalysis).isEqualTo(AlcoholCausalAnalysis.MANGLENDE_OPPLAERING)
        assertThat(found.causalExplanation).isEqualTo("Explanation here")
        assertThat(found.preventiveMeasures).isEqualTo("Preventive steps")
        assertThat(found.preventiveDeadline).isNotNull
        assertThat(found.preventiveResponsibleUser?.id).isEqualTo(responsibleUser.id)
        assertThat(found.status).isEqualTo(AlcoholDeviationStatus.UNDER_TREATMENT)
    }

    @Test
    fun `default status is OPEN`() {
        val saved = saveDeviation()
        entityManager.clear()

        val found = repository.findByIdAndOrganizationId(saved.id, orgId)!!

        assertThat(found.status).isEqualTo(AlcoholDeviationStatus.OPEN)
    }

    @Test
    fun `delete removes the deviation`() {
        val saved = saveDeviation()

        repository.delete(saved)
        entityManager.flush()

        assertThat(repository.findByIdAndOrganizationId(saved.id, orgId)).isNull()
    }
}