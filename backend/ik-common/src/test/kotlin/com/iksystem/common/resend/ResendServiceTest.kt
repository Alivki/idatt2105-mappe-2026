package com.iksystem.common.resend.service

import com.iksystem.common.config.ResendConfig
import com.iksystem.common.resend.EmailTemplateBuilder
import com.iksystem.common.resend.ResendClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class ResendServiceTest {

    private lateinit var resendClient: ResendClient
    private lateinit var templateBuilder: EmailTemplateBuilder
    private lateinit var props: ResendConfig
    private lateinit var service: ResendService

    @BeforeEach
    fun setUp() {
        resendClient = mock(ResendClient::class.java)
        templateBuilder = mock(EmailTemplateBuilder::class.java)
        props = mock(ResendConfig::class.java)

        `when`(props.baseUrl).thenReturn("http://localhost:80")

        service = ResendService(
            resendClient = resendClient,
            tempBuilder = templateBuilder,
            props = props
        )
    }

    @Test
    fun `sendRegistrationEmail builds registration template and sends email`() {
        val html = "<html>registration</html>"

        `when`(
            templateBuilder.registrationEmail(
                "Welcome to IK-system",
                "Hi, Alice\nThank you for joining!"
            )
        ).thenReturn(html)

        service.sendRegistrationEmail("alice@example.com", "Alice")

        verify(templateBuilder).registrationEmail(
            "Welcome to IK-system",
            "Hi, Alice\nThank you for joining!"
        )
        verify(resendClient).sendEmail(
            "alice@example.com",
            "Welcome to IK-system",
            html
        )
    }

    @Test
    fun `sendVerificationEmail builds verification email and sends it`() {
        val html = "<html>verify</html>"

        `when`(
            templateBuilder.actionEmail(
                "Verify your account",
                "Click below to verify your account.",
                "Verify Account",
                "http://localhost:80/verify?token=abc123"
            )
        ).thenReturn(html)

        service.sendVerificationEmail("user@example.com", "abc123")

        verify(templateBuilder).actionEmail(
            "Verify your account",
            "Click below to verify your account.",
            "Verify Account",
            "http://localhost:80/verify?token=abc123"
        )
        verify(resendClient).sendEmail(
            "user@example.com",
            "Verify your account",
            html
        )
    }

    @Test
    fun `sendNotificationEmail builds action email and sends it`() {
        val html = "<html>notification</html>"

        `when`(
            templateBuilder.actionEmail(
                "Important update",
                "Please check the app",
                "View in App",
                "http://localhost:80"
            )
        ).thenReturn(html)

        service.sendNotificationEmail(
            "notify@example.com",
            "Important update",
            "Please check the app"
        )

        verify(templateBuilder).actionEmail(
            "Important update",
            "Please check the app",
            "View in App",
            "http://localhost:80"
        )
        verify(resendClient).sendEmail(
            "notify@example.com",
            "Important update",
            html
        )
    }

    @Test
    fun `sendInviteEmail builds invite email and sends it`() {
        val html = "<html>invite</html>"

        `when`(
            templateBuilder.actionEmail(
                "Du er invitert til My Org",
                "Du har blitt invitert til å bli med i IK-systemet for My Org. Klikk på knappen under for å fullføre din profil.",
                "Godta invitasjon",
                "http://localhost:80/invite?token=invite-token"
            )
        ).thenReturn(html)

        service.sendInviteEmail("invitee@example.com", "My Org", "invite-token")

        verify(templateBuilder).actionEmail(
            "Du er invitert til My Org",
            "Du har blitt invitert til å bli med i IK-systemet for My Org. Klikk på knappen under for å fullføre din profil.",
            "Godta invitasjon",
            "http://localhost:80/invite?token=invite-token"
        )
        verify(resendClient).sendEmail(
            "invitee@example.com",
            "Invitasjon til My Org",
            html
        )
    }

    @Test
    fun `sendChecklistAlert builds status card and sends it`() {
        val html = "<html>checklist-alert</html>"

        `when`(
            templateBuilder.statusCard(
                "Opening checklist",
                listOf(
                    "Due date" to "2026-04-20",
                    "Status" to "Expiring soon"
                ),
                "Expires Soon",
                "#B7791F"
            )
        ).thenReturn(html)

        service.sendChecklistAlert(
            "check@example.com",
            "Opening checklist",
            "2026-04-20"
        )

        verify(templateBuilder).statusCard(
            "Opening checklist",
            listOf(
                "Due date" to "2026-04-20",
                "Status" to "Expiring soon"
            ),
            "Expires Soon",
            "#B7791F"
        )
        verify(resendClient).sendEmail(
            "check@example.com",
            "Checklist expiring soon",
            html
        )
    }

    @Test
    fun `sendTrainingExpiringSoon builds status card and sends it`() {
        val html = "<html>training-expiring</html>"

        `when`(
            templateBuilder.statusCard(
                "Missing Certification",
                listOf(
                    "Employee" to "Bob",
                    "Status" to "Missing"
                ),
                "Expiring soon",
                "#B7791F"
            )
        ).thenReturn(html)

        service.sendTrainingExpiringSoon("manager@example.com", "Bob")

        verify(templateBuilder).statusCard(
            "Missing Certification",
            listOf(
                "Employee" to "Bob",
                "Status" to "Missing"
            ),
            "Expiring soon",
            "#B7791F"
        )
        verify(resendClient).sendEmail(
            "manager@example.com",
            "Training Missing",
            html
        )
    }

    @Test
    fun `sendDeviationEmail truncates description and sends it`() {
        val html = "<html>deviation</html>"
        val longDescription = "x".repeat(150)
        val truncated = "x".repeat(100)

        `when`(
            templateBuilder.statusCard(
                "New Deviation Reported",
                listOf(
                    "Type" to "Fake ID",
                    "Severity" to "High",
                    "Reported by" to "Alice",
                    "Description" to truncated
                ),
                "Action Required",
                "#C53030"
            )
        ).thenReturn(html)

        service.sendDeviationEmail(
            "admin@example.com",
            "Fake ID",
            "High",
            "Alice",
            longDescription
        )

        verify(templateBuilder).statusCard(
            "New Deviation Reported",
            listOf(
                "Type" to "Fake ID",
                "Severity" to "High",
                "Reported by" to "Alice",
                "Description" to truncated
            ),
            "Action Required",
            "#C53030"
        )
        verify(resendClient).sendEmail(
            "admin@example.com",
            "New Deviation: Fake ID",
            html
        )
    }

    @Test
    fun `sendChecklistOverdueEmail builds overdue email and sends it`() {
        val html = "<html>overdue</html>"

        `when`(
            templateBuilder.statusCard(
                "Checklist Overdue",
                listOf(
                    "Checklist" to "Closing checklist",
                    "Frequency" to "WEEKLY",
                    "Incomplete items" to "3"
                ),
                "Overdue",
                "#C53030"
            )
        ).thenReturn(html)

        service.sendChecklistOverdueEmail(
            "lead@example.com",
            "Closing checklist",
            "WEEKLY",
            3
        )

        verify(templateBuilder).statusCard(
            "Checklist Overdue",
            listOf(
                "Checklist" to "Closing checklist",
                "Frequency" to "WEEKLY",
                "Incomplete items" to "3"
            ),
            "Overdue",
            "#C53030"
        )
        verify(resendClient).sendEmail(
            "lead@example.com",
            "Checklist Overdue: Closing checklist",
            html
        )
    }

    @Test
    fun `sendTrainingMissing builds missing training email and sends it`() {
        val html = "<html>training-missing</html>"

        `when`(
            templateBuilder.statusCard(
                "Missing Certification",
                listOf(
                    "Employee" to "Eve",
                    "Status" to "Missing"
                ),
                "Action Required",
                "#C53030"
            )
        ).thenReturn(html)

        service.sendTrainingMissing("manager@example.com", "Eve")

        verify(templateBuilder).statusCard(
            "Missing Certification",
            listOf(
                "Employee" to "Eve",
                "Status" to "Missing"
            ),
            "Action Required",
            "#C53030"
        )
        verify(resendClient).sendEmail(
            "manager@example.com",
            "Training Missing",
            html
        )
    }
}