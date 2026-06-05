package uk.gov.justice.digital.hmpps.communitysupportapi.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.communitysupportapi.authorization.UserMapper
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.AppointmentIcsFeedbackResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CaseWorkerSummaryDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CreateIcsFeedbackRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.RecordSessionRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionDurationRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionMethodRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionMethodType
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionNotHappenReason
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionNotHappenReasonRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActorType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentDeliveryMethod
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentStatusHistoryType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralEventType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.AppointmentTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.ReferralTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentIcsFeedbackRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentIcsRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentStatusHistoryRepository
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
  private lateinit var appointmentStatusHistoryRepository: AppointmentStatusHistoryRepository

  @Autowired
  private lateinit var referralEventRepository: ReferralEventRepository

  @Autowired
  private lateinit var referralHelper: ReferralTestSupport

  @Autowired
  private lateinit var appointmentHelper: AppointmentTestSupport

  @MockitoBean
  private lateinit var userMapper: UserMapper

  private lateinit var referral: Referral
  private lateinit var referralId: UUID
  private lateinit var referralCaseReference: String
  private lateinit var testUser: ReferralUser

  @BeforeEach
  fun setUp() {
    val person = referralHelper.createPerson(firstName = "Alex", lastName = "Jones", identifier = "X654321")
    testUser = referralHelper.ensureReferralUser()
    referral = referralHelper.createReferral(person, submittedBy = testUser)
    referralId = referral.id
    referralCaseReference = referral.referenceNumber!!
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
  @DisplayName("POST /bff/referral/{caseReference}/ics/{icsAppointmentId}/feedback")
  inner class SubmitIcsFeedbackEndpoint {

    @Test
    fun `should return 401 unauthorized when no token provided`() {
      assertUnauthorized(POST, "/bff/referral/$referralCaseReference/ics/${UUID.randomUUID()}/feedback")
    }

    @Test
    fun `should return 403 forbidden when no roles provided`() {
      assertForbiddenNoRole(
        POST,
        "/bff/referral/$referralCaseReference/ics/${UUID.randomUUID()}/feedback",
        appointmentHelper.buildIcsFeedbackRequest(),
      )
    }

    @Test
    fun `should return 403 forbidden when wrong role provided`() {
      assertForbiddenWrongRole(
        POST,
        "/bff/referral/$referralCaseReference/ics/${UUID.randomUUID()}/feedback",
        appointmentHelper.buildIcsFeedbackRequest(),
      )
    }

    @Test
    fun `should return 404 when referral does not exist`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)
      assertNotFound(
        POST,
        "/bff/referral/ZZ9999ZZ/ics/${UUID.randomUUID()}/feedback",
        appointmentHelper.buildIcsFeedbackRequest(),
      )
    }

    @Test
    fun `should return 404 when ics appointment does not exist`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)
      assertNotFound(
        POST,
        "/bff/referral/$referralCaseReference/ics/${UUID.randomUUID()}/feedback",
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
        "/bff/referral/$referralCaseReference/ics/${otherIcs.id}/feedback",
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
        .uri("/bff/referral/$referralCaseReference/ics/$icsId/feedback")
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
        .uri("/bff/referral/$referralCaseReference/ics/$icsId/feedback")
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
        .uri("/bff/referral/$referralCaseReference/ics/$icsId/feedback")
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
        .uri("/bff/referral/$referralCaseReference/ics/$icsId/feedback")
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
          type = SessionMethodType.IN_PERSON_PROBATION_OFFICE,
          pdu = "PDU-North-West",
        ),
      )

      webTestClient.post()
        .uri("/bff/referral/$referralCaseReference/ics/$icsId/feedback")
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
        .uri("/bff/referral/$referralCaseReference/ics/$icsId/feedback")
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

    @Test
    fun `should return 201 and persist didPersonAttend and no attendance information when person did not come`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)
      val icsId = createIcsAppointmentId()

      val request = CreateIcsFeedbackRequest(
        record = RecordSessionRequest(
          didSessionHappen = false,
          didPersonAttend = false,
          noAttendanceInformation = "Called twice on 26 April, no answer. Left a voicemail.",
        ),
      )

      webTestClient.post()
        .uri("/bff/referral/$referralCaseReference/ics/$icsId/feedback")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation())
        .bodyValue(request)
        .exchange()
        .expectStatus().isCreated
        .expectBody<AppointmentIcsFeedbackResponse>()
        .consumeWith { result ->
          val body = result.responseBody!!
          assertThat(body.recordSessionDidSessionHappen).isFalse()
          assertThat(body.recordSessionDidPersonAttend).isFalse()
          assertThat(body.recordSessionNoAttendanceInformation).isEqualTo("Called twice on 26 April, no answer. Left a voicemail.")
          assertThat(body.recordSessionNotHappenReason).isNull()
          assertThat(body.recordSessionNotHappenReasonDetails).isNull()
          val saved = appointmentIcsFeedbackRepository.findById(body.id).orElseThrow()
          assertThat(saved.recordSessionDidPersonAttend).isFalse()
          assertThat(saved.recordSessionNoAttendanceInformation).isEqualTo("Called twice on 26 April, no answer. Left a voicemail.")
        }
    }

    @Test
    fun `should return 201 and persist session not happen reason SERVICE_PROVIDER_ISSUE when person attended`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)
      val icsId = createIcsAppointmentId()

      val request = CreateIcsFeedbackRequest(
        record = RecordSessionRequest(
          didSessionHappen = false,
          didPersonAttend = true,
          sessionNotHappenReason = SessionNotHappenReasonRequest(
            reason = SessionNotHappenReason.SERVICE_PROVIDER_ISSUE,
            details = "Room booking was cancelled due to a fire alarm.",
          ),
        ),
      )

      webTestClient.post()
        .uri("/bff/referral/$referralCaseReference/ics/$icsId/feedback")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation())
        .bodyValue(request)
        .exchange()
        .expectStatus().isCreated
        .expectBody<AppointmentIcsFeedbackResponse>()
        .consumeWith { result ->
          val body = result.responseBody!!
          assertThat(body.recordSessionDidSessionHappen).isFalse()
          assertThat(body.recordSessionDidPersonAttend).isTrue()
          assertThat(body.recordSessionNotHappenReason).isEqualTo("SERVICE_PROVIDER_ISSUE")
          assertThat(body.recordSessionNotHappenReasonDetails).isEqualTo("Room booking was cancelled due to a fire alarm.")
          assertThat(body.recordSessionNoAttendanceInformation).isNull()
          val saved = appointmentIcsFeedbackRepository.findById(body.id).orElseThrow()
          assertThat(saved.recordSessionNotHappenReason).isEqualTo("SERVICE_PROVIDER_ISSUE")
          assertThat(saved.recordSessionNotHappenReasonDetails).isEqualTo("Room booking was cancelled due to a fire alarm.")
        }
    }

    @Test
    fun `should return 201 and persist session not happen reason PERSON_COULD_NOT_TAKE_PART when person attended`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)
      val icsId = createIcsAppointmentId()

      val request = CreateIcsFeedbackRequest(
        record = RecordSessionRequest(
          didSessionHappen = false,
          didPersonAttend = true,
          sessionNotHappenReason = SessionNotHappenReasonRequest(
            reason = SessionNotHappenReason.REFERRAL_COULD_NOT_TAKE_PART,
            details = "Alex was in crisis and unable to engage.",
          ),
        ),
      )

      webTestClient.post()
        .uri("/bff/referral/$referralCaseReference/ics/$icsId/feedback")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation())
        .bodyValue(request)
        .exchange()
        .expectStatus().isCreated
        .expectBody<AppointmentIcsFeedbackResponse>()
        .consumeWith { result ->
          val body = result.responseBody!!
          assertThat(body.recordSessionNotHappenReason).isEqualTo("REFERRAL_COULD_NOT_TAKE_PART")
          assertThat(body.recordSessionNotHappenReasonDetails).isEqualTo("Alex was in crisis and unable to engage.")
        }
    }

    @Test
    fun `should return 201 and persist session not happen reason PERSON_DID_NOT_COMPLY when person attended`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)
      val icsId = createIcsAppointmentId()

      val request = CreateIcsFeedbackRequest(
        record = RecordSessionRequest(
          didSessionHappen = false,
          didPersonAttend = true,
          sessionNotHappenReason = SessionNotHappenReasonRequest(
            reason = SessionNotHappenReason.REFERRAL_DID_NOT_COMPLY,
            details = "Alex was disruptive and refused to engage with the session.",
          ),
        ),
      )

      webTestClient.post()
        .uri("/bff/referral/$referralCaseReference/ics/$icsId/feedback")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation())
        .bodyValue(request)
        .exchange()
        .expectStatus().isCreated
        .expectBody<AppointmentIcsFeedbackResponse>()
        .consumeWith { result ->
          val body = result.responseBody!!
          assertThat(body.recordSessionNotHappenReason).isEqualTo("REFERRAL_DID_NOT_COMPLY")
          assertThat(body.recordSessionNotHappenReasonDetails).isEqualTo("Alex was disruptive and refused to engage with the session.")
        }
    }

    @Test
    fun `should set appointment status to COMPLETED when didSessionHappen is true`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)
      val icsId = createIcsAppointmentId()

      webTestClient.post()
        .uri("/bff/referral/$referralCaseReference/ics/$icsId/feedback")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation())
        .bodyValue(appointmentHelper.buildIcsFeedbackRequest(didSessionHappen = true))
        .exchange()
        .expectStatus().isCreated

      val ics = appointmentIcsRepository.findById(icsId).orElseThrow()
      val latestStatus = appointmentStatusHistoryRepository
        .findTopByAppointmentIdOrderByCreatedAtDesc(ics.appointment.id)
      assertThat(latestStatus?.status).isEqualTo(AppointmentStatusHistoryType.COMPLETED)
    }

    @Test
    fun `should set appointment status to DID_NOT_ATTEND when didSessionHappen is false and person did not come`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)
      val icsId = createIcsAppointmentId()

      val request = CreateIcsFeedbackRequest(
        record = RecordSessionRequest(
          didSessionHappen = false,
          didPersonAttend = false,
          noAttendanceInformation = "No answer when called twice.",
        ),
      )

      webTestClient.post()
        .uri("/bff/referral/$referralCaseReference/ics/$icsId/feedback")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation())
        .bodyValue(request)
        .exchange()
        .expectStatus().isCreated

      val ics = appointmentIcsRepository.findById(icsId).orElseThrow()
      val latestStatus = appointmentStatusHistoryRepository
        .findTopByAppointmentIdOrderByCreatedAtDesc(ics.appointment.id)
      assertThat(latestStatus?.status).isEqualTo(AppointmentStatusHistoryType.DID_NOT_ATTEND)
    }

    @Test
    fun `should set appointment status to DID_NOT_HAPPEN when didSessionHappen is false and person attended`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)
      val icsId = createIcsAppointmentId()

      val request = CreateIcsFeedbackRequest(
        record = RecordSessionRequest(
          didSessionHappen = false,
          didPersonAttend = true,
          sessionNotHappenReason = SessionNotHappenReasonRequest(
            reason = SessionNotHappenReason.SERVICE_PROVIDER_ISSUE,
            details = "Room was unavailable.",
          ),
        ),
      )

      webTestClient.post()
        .uri("/bff/referral/$referralCaseReference/ics/$icsId/feedback")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation())
        .bodyValue(request)
        .exchange()
        .expectStatus().isCreated

      val ics = appointmentIcsRepository.findById(icsId).orElseThrow()
      val latestStatus = appointmentStatusHistoryRepository
        .findTopByAppointmentIdOrderByCreatedAtDesc(ics.appointment.id)
      assertThat(latestStatus?.status).isEqualTo(AppointmentStatusHistoryType.DID_NOT_HAPPEN)
    }

    @Test
    fun `should return 409 Conflict when same ics appointment is submitted twice`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)
      val icsId = createIcsAppointmentId()

      val firstResponse = webTestClient.post()
        .uri("/bff/referral/$referralCaseReference/ics/$icsId/feedback")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation())
        .bodyValue(appointmentHelper.buildIcsFeedbackRequest())
        .exchange()
        .expectStatus().isCreated
        .expectBody<AppointmentIcsFeedbackResponse>()
        .returnResult()
        .responseBody!!

      // Second attempt should return 409 Conflict
      webTestClient.post()
        .uri("/bff/referral/$referralCaseReference/ics/$icsId/feedback")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation())
        .bodyValue(appointmentHelper.buildIcsFeedbackRequest())
        .exchange()
        .expectStatus().isEqualTo(409)

      // Only one record in the DB
      val allFeedback = appointmentIcsFeedbackRepository.findAll()
        .filter { it.appointmentIcs.id == icsId }
      assertThat(allFeedback).hasSize(1)
      assertThat(allFeedback.first().id).isEqualTo(firstResponse.id)

      // Only one audit event created
      val events = referralEventRepository.findAll()
        .filter { it.eventType == ReferralEventType.APPOINTMENT_FEEDBACK_SENT }
      assertThat(events).hasSize(1)
    }
  }

  // ── Helper to create feedback and return its id ────────────────────────────

  private fun createIcsFeedback(icsId: UUID, icsFeedbackRequest: CreateIcsFeedbackRequest): AppointmentIcsFeedbackResponse {
    whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)
    return webTestClient.post()
      .uri("/bff/referral/$referralCaseReference/ics/$icsId/feedback")
      .contentType(MediaType.APPLICATION_JSON)
      .headers(setAuthorisation())
      .bodyValue(icsFeedbackRequest)
      .exchange()
      .expectStatus().isCreated
      .expectBody<AppointmentIcsFeedbackResponse>()
      .returnResult()
      .responseBody!!
  }

  // ── Tests for GET /bff/ics-feedback/{icsFeedbackId} ──

  @Nested
  @DisplayName("GET /bff/ics-feedback/{icsFeedbackId}")
  inner class GetIcsFeedbackEndpoint {

    @Test
    fun `should return 401 unauthorized when no token provided`() {
      assertUnauthorized(GET, "/bff/ics-feedback/${UUID.randomUUID()}")
    }

    @Test
    fun `should return 403 forbidden when no roles provided`() {
      assertForbiddenNoRole(GET, "/bff/ics-feedback/${UUID.randomUUID()}")
    }

    @Test
    fun `should return 403 forbidden when wrong role provided`() {
      assertForbiddenWrongRole(GET, "/bff/ics-feedback/${UUID.randomUUID()}")
    }

    @Test
    fun `should return 404 when feedback does not exist`() {
      assertNotFound(GET, "/bff/ics-feedback/${UUID.randomUUID()}")
    }

    @Test
    fun `should return ICS feedback with session details for DID_NOT_HAPPEN appointment`() {
      val icsId = createIcsAppointmentId()
      val icsFeedbackRequest = CreateIcsFeedbackRequest(
        record = RecordSessionRequest(
          didSessionHappen = false,
          didPersonAttend = true,
          sessionNotHappenReason = SessionNotHappenReasonRequest(
            reason = SessionNotHappenReason.REFERRAL_DID_NOT_COMPLY,
            details = "Alex was disruptive and refused to engage with the session.",
          ),
        ),
      )

      referralHelper.assignCaseWorkers(referral, referralHelper.createCaseWorkers("CaseWorker One"))

      val icsFeedback = createIcsFeedback(icsId, icsFeedbackRequest)

      appointmentHelper.updateAppointmentStatusHistory(icsId, AppointmentStatusHistoryType.DID_NOT_HAPPEN)

      webTestClient.get()
        .uri("/bff/ics-feedback/${icsFeedback.id}")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBody<AppointmentIcsFeedbackResponse>()
        .consumeWith { result ->
          val body = result.responseBody!!
          assertThat(body.id).isEqualTo(icsFeedback.id)
          assertThat(body.appointmentIcsId).isEqualTo(icsId)
          assertThat(body.recordSessionDidSessionHappen).isFalse()
          assertThat(body.recordSessionDidPersonAttend).isTrue()
          assertThat(body.recordSessionNotHappenReason).isEqualTo("REFERRAL_DID_NOT_COMPLY")
          assertThat(body.recordSessionNotHappenReasonDetails).isEqualTo("Alex was disruptive and refused to engage with the session.")

          assertThat(body.sessionFeedbackDetails?.currentCaseworkers).isEqualTo(
            listOf(CaseWorkerSummaryDto(fullName = "CaseWorker One", emailAddress = "test-user")),
          )
          assertThat(body.sessionFeedbackDetails?.feedbackSubmittedBy).isEqualTo(
            CaseWorkerSummaryDto(fullName = "fullname", emailAddress = "test-user"),
          )
          assertThat(body.sessionFeedbackDetails?.startDateTime).isEqualTo("2026-04-09T10:00:00")
          assertThat(body.sessionFeedbackDetails?.sessionMethod).isEqualTo(AppointmentDeliveryMethod.PHONE_CALL)
          assertThat(body.sessionFeedbackDetails?.sessionCommunications).isEqualTo(listOf("Phone call"))
          assertThat(body.sessionFeedbackDetails?.personFirstName).isEqualTo("Alex")
        }
    }

    @Test
    fun `should return ICS feedback with session details for DID_NOT_ATTEND appointment`() {
      val icsId = createIcsAppointmentId()
      val icsFeedbackRequest = CreateIcsFeedbackRequest(
        record = RecordSessionRequest(
          didSessionHappen = false,
          didPersonAttend = false,
          noAttendanceInformation = "Called on two separate occasions this afternoon and got no answer. Left voicemail to call back.",
        ),
      )

      referralHelper.assignCaseWorkers(
        referral,
        referralHelper.createCaseWorkers("CaseWorker One", "CaseWorker Two"),
      )

      val icsFeedback = createIcsFeedback(icsId, icsFeedbackRequest)

      appointmentHelper.updateAppointmentStatusHistory(icsId, AppointmentStatusHistoryType.DID_NOT_ATTEND)

      webTestClient.get()
        .uri("/bff/ics-feedback/${icsFeedback.id}")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBody<AppointmentIcsFeedbackResponse>()
        .consumeWith { result ->
          val body = result.responseBody!!
          assertThat(body.id).isEqualTo(icsFeedback.id)
          assertThat(body.appointmentIcsId).isEqualTo(icsId)
          assertThat(body.recordSessionDidSessionHappen).isFalse()
          assertThat(body.recordSessionDidPersonAttend).isFalse()
          assertThat(body.recordSessionNotHappenReason).isNull()
          assertThat(body.recordSessionNotHappenReasonDetails).isNull()

          assertThat(body.sessionFeedbackDetails?.currentCaseworkers).isEqualTo(
            listOf(
              CaseWorkerSummaryDto(fullName = "CaseWorker One", emailAddress = "test-user"),
              CaseWorkerSummaryDto(fullName = "CaseWorker Two", emailAddress = "test-user"),
            ),
          )
          assertThat(body.sessionFeedbackDetails?.feedbackSubmittedBy).isEqualTo(
            CaseWorkerSummaryDto(fullName = "fullname", emailAddress = "test-user"),
          )
          assertThat(body.sessionFeedbackDetails?.startDateTime).isEqualTo("2026-04-09T10:00:00")
          assertThat(body.sessionFeedbackDetails?.sessionMethod).isEqualTo(AppointmentDeliveryMethod.PHONE_CALL)
          assertThat(body.sessionFeedbackDetails?.sessionCommunications).isEqualTo(listOf("Phone call"))
          assertThat(body.sessionFeedbackDetails?.personFirstName).isEqualTo("Alex")
        }
    }

    @Test
    fun `should return ICS feedback with session details for COMPLETED appointment`() {
      val icsId = createIcsAppointmentId()
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)

      val completedIcsFeedback = appointmentHelper.buildIcsFeedbackRequest(
        didSessionHappen = true,
        howSessionTookPlace = SessionMethodRequest(type = SessionMethodType.PHONE, additionalDetails = "Client preferred phone"),
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

      referralHelper.assignCaseWorkers(referral, referralHelper.createCaseWorkers("CaseWorker One"))

      val icsFeedback = createIcsFeedback(icsId, completedIcsFeedback)

      appointmentHelper.updateAppointmentStatusHistory(icsId, AppointmentStatusHistoryType.COMPLETED)

      webTestClient.get()
        .uri("/bff/ics-feedback/${icsFeedback.id}")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBody<AppointmentIcsFeedbackResponse>()
        .consumeWith { result ->
          val body = result.responseBody!!
          assertThat(body.id).isEqualTo(icsFeedback.id)
          assertThat(body.appointmentIcsId).isEqualTo(icsId)
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
        }
    }
  }
}
