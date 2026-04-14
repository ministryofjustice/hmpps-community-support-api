package uk.gov.justice.digital.hmpps.communitysupportapi.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod.POST
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.communitysupportapi.authorization.UserMapper
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.AppointmentIcsFeedbackResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CreateIcsFeedbackRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.RecordSessionRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionDurationRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionMethodRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionMethodType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActorType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentDeliveryMethod
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralEventType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.AppointmentTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.ReferralTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentIcsFeedbackRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentIcsRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralEventRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import java.time.LocalDateTime
import java.util.UUID

class AppointmentIcsFeedbackControllerIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @Autowired
  private lateinit var appointmentIcsRepository: AppointmentIcsRepository

  @Autowired
  private lateinit var appointmentIcsFeedbackRepository: AppointmentIcsFeedbackRepository

  @Autowired
  private lateinit var referralEventRepository: ReferralEventRepository

  @Autowired
  private lateinit var referralHelper: ReferralTestSupport

  @Autowired
  private lateinit var appointmentHelper: AppointmentTestSupport

  @MockitoBean
  private lateinit var userMapper: UserMapper

  private lateinit var referralId: UUID
  private lateinit var testUser: ReferralUser

  @BeforeEach
  fun setUp() {
    val person = referralHelper.createPerson(firstName = "Alex", lastName = "Jones", identifier = "X654321")
    testUser = referralHelper.ensureReferralUser()
    val referral = referralHelper.createReferral(person, submittedBy = testUser)
    referralId = referral.id
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  private fun createIcsAppointmentId(): UUID {
    val referral = referralRepository.findById(referralId).orElseThrow()
    val appointment = appointmentHelper.createAppointment(referral)
    appointmentHelper.createAppointmentStatusHistory(appointment)
    val delivery = appointmentHelper.createAppointmentDelivery(AppointmentDeliveryMethod.PHONE_CALL)
    val ics = appointmentHelper.createAppointmentIcs(
      appointment,
      delivery,
      testUser,
      LocalDateTime.of(2026, 4, 9, 10, 0),
      LocalDateTime.of(2026, 4, 8, 9, 0),
      listOf("Phone call"),
    )
    return ics.id
  }

  // ── Tests ─────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("POST /bff/referral/{referralId}/ics/{icsAppointmentId}/feedback")
  inner class SubmitIcsFeedbackEndpoint {

    @Test
    fun `should return 401 unauthorized when no token provided`() {
      assertUnauthorized(POST, "/bff/referral/$referralId/ics/${UUID.randomUUID()}/feedback")
    }

    @Test
    fun `should return 403 forbidden when no roles provided`() {
      assertForbiddenNoRole(
        POST,
        "/bff/referral/$referralId/ics/${UUID.randomUUID()}/feedback",
        appointmentHelper.buildIcsFeedbackRequest(),
      )
    }

    @Test
    fun `should return 403 forbidden when wrong role provided`() {
      assertForbiddenWrongRole(
        POST,
        "/bff/referral/$referralId/ics/${UUID.randomUUID()}/feedback",
        appointmentHelper.buildIcsFeedbackRequest(),
      )
    }

    @Test
    fun `should return 404 when referral does not exist`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)
      assertNotFound(
        POST,
        "/bff/referral/${UUID.randomUUID()}/ics/${UUID.randomUUID()}/feedback",
        appointmentHelper.buildIcsFeedbackRequest(),
      )
    }

    @Test
    fun `should return 404 when ics appointment does not exist`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)
      assertNotFound(
        POST,
        "/bff/referral/$referralId/ics/${UUID.randomUUID()}/feedback",
        appointmentHelper.buildIcsFeedbackRequest(),
      )
    }

    @Test
    fun `should return 404 when ics appointment belongs to a different referral`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)

      val otherPerson = referralHelper.createPerson(identifier = "Y999999")
      val otherReferral = referralHelper.createReferral(otherPerson, submittedBy = testUser)
      val otherAppointment = appointmentHelper.createAppointment(otherReferral)
      appointmentHelper.createAppointmentStatusHistory(otherAppointment)
      val delivery = appointmentHelper.createAppointmentDelivery()
      val otherIcs = appointmentHelper.createAppointmentIcs(
        otherAppointment,
        delivery,
        testUser,
        LocalDateTime.now().plusDays(1),
        LocalDateTime.now(),
        listOf("Email"),
      )

      assertNotFound(
        POST,
        "/bff/referral/$referralId/ics/${otherIcs.id}/feedback",
        appointmentHelper.buildIcsFeedbackRequest(),
      )
    }

    @Test
    fun `should return 201 and persist all feedback fields`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)
      val icsId = createIcsAppointmentId()

      val request = appointmentHelper.buildIcsFeedbackRequest(
        didSessionHappen = true,
        howSessionTookPlace = SessionMethodRequest(
          type = SessionMethodType.PHONE,
          additionalDetails = "Client preferred phone",
        ),
        wasPersonLate = true,
        lateReason = "Car trouble",
        duration = SessionDurationRequest(hours = 1, minutes = 45),
        whatHappened = "Reviewed progress on employment goals",
        behaviour = "Engaged and motivated",
        strengthsIdentified = "Excellent communication skills",
        issuesConcernsIdentified = "Risk of housing loss",
        notifyProbationPractitioner = true,
        plannedForNextSession = "Review housing options",
        actionsBeforeNextSession = "Contact housing officer",
      )

      webTestClient.post()
        .uri("/bff/referral/$referralId/ics/$icsId/feedback")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation())
        .bodyValue(request)
        .exchange()
        .expectStatus().isCreated
        .expectBody<AppointmentIcsFeedbackResponse>()
        .consumeWith { result ->
          val body = result.responseBody!!

          assertThat(body.id).isNotNull()
          assertThat(body.appointmentIcsId).isEqualTo(icsId)
          assertThat(body.recordSessionDidSessionHappen).isTrue()
          assertThat(body.recordSessionHowSessionTookPlace).isEqualTo("Phone call")
          assertThat(body.recordSessionNotInPersonReason).isEqualTo("Client preferred phone")
          assertThat(body.sessionDetailsWasPersonLate).isTrue()
          assertThat(body.sessionDetailsLateReason).isEqualTo("Car trouble")
          assertThat(body.sessionDetailsDuration).isEqualTo("1 hour and 45 minutes")
          assertThat(body.sessionFeedbackWhatHappened).isEqualTo("Reviewed progress on employment goals")
          assertThat(body.sessionFeedbackBehaviour).isEqualTo("Engaged and motivated")
          assertThat(body.sessionFeedbackStrengthsIdentified).isEqualTo("Excellent communication skills")
          assertThat(body.issuesOrConcernsIdentified).isEqualTo("Risk of housing loss")
          assertThat(body.issuesOrConcernsNotifyProbationPractitioner).isTrue()
          assertThat(body.nextStepsPlannedForNextSession).isEqualTo("Review housing options")
          assertThat(body.nextStepsActionsBeforeNextSession).isEqualTo("Contact housing officer")
          val saved = appointmentIcsFeedbackRepository.findById(body.id).orElseThrow()
          assertThat(saved.appointmentIcs.id).isEqualTo(icsId)
          assertThat(saved.recordSessionDidSessionHappen).isTrue()
          assertThat(saved.sessionDetailsDuration).isEqualTo("1 hour and 45 minutes")
          assertThat(saved.issuesConcernsNotifyProbationPractitioner).isTrue()
          assertThat(saved.createdBy?.id).isEqualTo(testUser.id)
          val events = referralEventRepository.findAll()
            .filter { it.eventType == ReferralEventType.APPOINTMENT_FEEDBACK_SENT }
          assertThat(events).hasSize(1)
          assertThat(events.first().referral.id).isEqualTo(referralId)
          assertThat(events.first().actorType).isEqualTo(ActorType.EXTERNAL)
          assertThat(events.first().actorId).isEqualTo(testUser.id)
        }
    }

    @Test
    fun `should return 201 when session did not happen with only required fields`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)
      val icsId = createIcsAppointmentId()

      val request = CreateIcsFeedbackRequest(record = RecordSessionRequest(didSessionHappen = false))

      webTestClient.post()
        .uri("/bff/referral/$referralId/ics/$icsId/feedback")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation())
        .bodyValue(request)
        .exchange()
        .expectStatus().isCreated
        .expectBody<AppointmentIcsFeedbackResponse>()
        .consumeWith { result ->
          val body = result.responseBody!!
          assertThat(body.recordSessionDidSessionHappen).isFalse()
          assertThat(body.recordSessionHowSessionTookPlace).isNull()
          assertThat(body.sessionDetailsWasPersonLate).isNull()
          assertThat(body.issuesOrConcernsIdentified).isNull()
          assertThat(body.nextStepsPlannedForNextSession).isNull()
        }
    }

    @Test
    fun `should return 201 when person was not late and no late reason given`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)
      val icsId = createIcsAppointmentId()

      webTestClient.post()
        .uri("/bff/referral/$referralId/ics/$icsId/feedback")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation())
        .bodyValue(appointmentHelper.buildIcsFeedbackRequest(wasPersonLate = false, lateReason = null))
        .exchange()
        .expectStatus().isCreated
        .expectBody<AppointmentIcsFeedbackResponse>()
        .consumeWith { result ->
          val body = result.responseBody!!
          assertThat(body.sessionDetailsWasPersonLate).isFalse()
          assertThat(body.sessionDetailsLateReason).isNull()
        }
    }

    @Test
    fun `should return 201 when issues identified and probation practitioner should be notified`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)
      val icsId = createIcsAppointmentId()

      webTestClient.post()
        .uri("/bff/referral/$referralId/ics/$icsId/feedback")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation())
        .bodyValue(
          appointmentHelper.buildIcsFeedbackRequest(
            issuesConcernsIdentified = "Substance misuse concern",
            notifyProbationPractitioner = true,
          ),
        )
        .exchange()
        .expectStatus().isCreated
        .expectBody<AppointmentIcsFeedbackResponse>()
        .consumeWith { result ->
          val body = result.responseBody!!
          assertThat(body.issuesOrConcernsIdentified).isEqualTo("Substance misuse concern")
          assertThat(body.issuesOrConcernsNotifyProbationPractitioner).isTrue()
        }
    }

    @Test
    fun `should return 201 and persist pdu when session happened at probation office`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)
      val icsId = createIcsAppointmentId()

      val request = appointmentHelper.buildIcsFeedbackRequest(
        howSessionTookPlace = SessionMethodRequest(
          type = SessionMethodType.PROBATION_OFFICE,
          pdu = "PDU-North-West",
        ),
      )

      webTestClient.post()
        .uri("/bff/referral/$referralId/ics/$icsId/feedback")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation())
        .bodyValue(request)
        .exchange()
        .expectStatus().isCreated
        .expectBody<AppointmentIcsFeedbackResponse>()
        .consumeWith { result ->
          val body = result.responseBody!!
          assertThat(body.recordSessionHowSessionTookPlace).isEqualTo("In person (probation office)")
          assertThat(body.recordSessionPdu).isEqualTo("PDU-North-West")
          assertThat(body.recordSessionNotInPersonReason).isNull()
          assertThat(body.recordSessionAddressLine1).isNull()

          val saved = appointmentIcsFeedbackRepository.findById(body.id).orElseThrow()
          assertThat(saved.recordSessionPdu).isEqualTo("PDU-North-West")
        }
    }

    @Test
    fun `should create APPOINTMENT_FEEDBACK_SENT referral audit event after submitting feedback`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)
      val icsId = createIcsAppointmentId()

      webTestClient.post()
        .uri("/bff/referral/$referralId/ics/$icsId/feedback")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation())
        .bodyValue(appointmentHelper.buildIcsFeedbackRequest())
        .exchange()
        .expectStatus().isCreated

      val events = referralEventRepository.findAll()
        .filter { it.eventType == ReferralEventType.APPOINTMENT_FEEDBACK_SENT }
      assertThat(events).hasSize(1)
      with(events.first()) {
        assertThat(referral.id).isEqualTo(referralId)
        assertThat(actorType).isEqualTo(ActorType.EXTERNAL)
        assertThat(actorId).isEqualTo(testUser.id)
        assertThat(createdAt).isNotNull()
      }
    }
  }
}
