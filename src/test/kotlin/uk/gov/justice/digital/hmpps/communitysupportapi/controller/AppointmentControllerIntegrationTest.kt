package uk.gov.justice.digital.hmpps.communitysupportapi.controller

import io.kotest.matchers.shouldBe
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.HttpMethod.POST
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.test.web.reactive.server.expectBodyList
import uk.gov.justice.digital.hmpps.communitysupportapi.authorization.UserMapper
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.AppointmentIcsResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.AppointmentTimeRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CreateAppointmentRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.IcsFeedbackSessionDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.InPersonAppointment
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionMethodRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionMethodType
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.VirtualAppointment
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentDeliveryMethod
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.AppointmentTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.ReferralTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentDeliveryRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentIcsRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralUserRepository
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class AppointmentControllerIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var personRepository: PersonRepository

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @Autowired
  private lateinit var appointmentRepository: AppointmentRepository

  @Autowired
  private lateinit var appointmentDeliveryRepository: AppointmentDeliveryRepository

  @Autowired
  private lateinit var appointmentIcsRepository: AppointmentIcsRepository

  @Autowired
  private lateinit var referralUserRepository: ReferralUserRepository

  @Autowired
  private lateinit var referralHelper: ReferralTestSupport

  @Autowired
  private lateinit var appointmentHelper: AppointmentTestSupport

  @MockitoBean
  private lateinit var userMapper: UserMapper

  private lateinit var referral: Referral

  private lateinit var testUser: ReferralUser

  private lateinit var person: uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person

  @BeforeEach
  fun setUpReferral() {
    testUser = referralHelper.ensureReferralUser()
    person = referralHelper.createPerson(firstName = "Alex", lastName = "Jones", identifier = "X654321")
    referral = referralHelper.createReferral(person, "AA1234DD", submittedBy = testUser)
  }

  @Nested
  @DisplayName("POST /bff/referral/{referralId}/ics")
  inner class CreateIcsAppointmentEndpoint {

    @Test
    fun `should return 401 unauthorized when no token provided`() {
      assertUnauthorized(POST, "/bff/referral/${referral.id}/ics")
    }

    @Test
    fun `should return 403 forbidden when no roles provided`() {
      assertForbiddenNoRole(POST, "/bff/referral/${referral.id}/ics", buildRequest())
    }

    @Test
    fun `should return 403 forbidden when wrong role provided`() {
      assertForbiddenWrongRole(POST, "/bff/referral/${referral.id}/ics", buildRequest())
    }

    @Test
    fun `should return 404 not found for unknown referral id`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)
      assertNotFound(POST, "/bff/referral/${UUID.randomUUID()}/appointment/ics", buildRequest())
    }

    @Test
    fun `should return 201 created and persist appointment for a phone call`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)

      val request = buildRequest(
        hour = 10,
        minute = 30,
        amPm = "am",
        type = SessionMethodType.PHONE,
        additionalDetails = "He is not feeling good, call on mobile",
        sessionCommunication = listOf("Phone call", "Text message"),
      )

      webTestClient.post()
        .uri("/bff/referral/${referral.id}/ics")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation())
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isCreated
        .expectBody<AppointmentIcsResponse>()
        .consumeWith { result ->
          val body = result.responseBody!!
          assertThat(body.referralId).isEqualTo(referral.id)
          assertThat(body.appointmentType).isEqualTo(AppointmentType.ICS)
          assertThat(body.appointmentDate).isEqualTo(LocalDate.of(2026, 3, 27))
          assertThat(body.appointmentTime.hour).isEqualTo(10)
          assertThat(body.appointmentTime.minute).isEqualTo(30)
          assertThat(body.appointmentTime.amPm).isEqualTo("am")
          assertThat(body.sessionMethod).isInstanceOf(VirtualAppointment::class.java)
          with(body.sessionMethod as VirtualAppointment) {
            assertThat(type).isEqualTo("PHONE")
            assertThat(whyNotInPersonReason).isEqualTo("He is not feeling good, call on mobile")
          }
          assertThat(body.sessionCommunications).containsExactly("Phone call", "Text message")

          val ics = appointmentIcsRepository.findById(body.appointmentIcsId).orElseThrow()
          assertThat(ics.appointmentDateTime.hour).isEqualTo(10)
          assertThat(ics.appointmentDateTime.minute).isEqualTo(30)
          assertThat(ics.sessionCommunication).containsExactly("Phone call", "Text message")
        }
    }

    @Test
    fun `should return 201 created and correctly store video call delivery`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)

      val request = buildRequest(type = SessionMethodType.VIDEO, additionalDetails = "Car broke down")

      webTestClient.post()
        .uri("/bff/referral/${referral.id}/ics")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation())
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isCreated
        .expectBody<AppointmentIcsResponse>()
        .consumeWith { result ->
          val body = result.responseBody!!
          assertThat(body.sessionMethod).isInstanceOf(VirtualAppointment::class.java)
          with(body.sessionMethod as VirtualAppointment) {
            assertThat(type).isEqualTo("VIDEO")
            assertThat(whyNotInPersonReason).isEqualTo("Car broke down")
          }

          val ics = appointmentIcsRepository.findById(body.appointmentIcsId).orElseThrow()
          assertThat(ics.appointmentDelivery?.method).isEqualTo(AppointmentDeliveryMethod.VIDEO_CALL)
        }
    }

    @Test
    fun `should return 201 created with 1pm correctly converted to 13h00`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)

      val request = buildRequest(hour = 1, minute = 0, amPm = "pm")

      webTestClient.post()
        .uri("/bff/referral/${referral.id}/ics")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation())
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isCreated
        .expectBody<AppointmentIcsResponse>()
        .consumeWith { result ->
          val body = result.responseBody!!
          // Response should give back 1pm in 12-hour format
          assertThat(body.appointmentTime.hour).isEqualTo(1)
          assertThat(body.appointmentTime.amPm).isEqualTo("pm")

          // DB should store as 13:00
          val ics = appointmentIcsRepository.findById(body.appointmentIcsId).orElseThrow()
          assertThat(ics.appointmentDateTime.hour).isEqualTo(13)
        }
    }

    @Test
    fun `should return 201 created for in-person other location with address fields`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)

      val request = CreateAppointmentRequest(
        date = LocalDate.of(2026, 3, 27),
        time = AppointmentTimeRequest(hour = 2, minute = 0, amPm = "pm"),
        sessionMethodRequest = SessionMethodRequest(
          type = SessionMethodType.OTHER_LOCATION,
          additionalDetails = "Side entrance",
          addressLine1 = "10 Downing Street",
          addressLine2 = null,
          townOrCity = "London",
          county = "Greater London",
          postcode = "SW1A 2AA",
        ),
        sessionCommunication = listOf("Email"),
      )

      webTestClient.post()
        .uri("/bff/referral/${referral.id}/ics")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation())
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isCreated
        .expectBody<AppointmentIcsResponse>()
        .consumeWith { result ->
          val body = result.responseBody!!
          assertThat(body.sessionMethod).isInstanceOf(InPersonAppointment::class.java)
          with(body.sessionMethod as InPersonAppointment) {
            assertThat(type).isEqualTo("IN_PERSON_OTHER_LOCATION")
            assertThat(addressLine1).isEqualTo("10 Downing Street")
            assertThat(townOrCity).isEqualTo("London")
            assertThat(postcode).isEqualTo("SW1A 2AA")
          }
        }
    }
  }

  @Nested
  @DisplayName("GET /bff/referral/{referralId}/ics")
  inner class GetIcsAppointmentsEndpoint {

    @Test
    fun `should return 401 unauthorized when no token provided`() {
      assertUnauthorized(HttpMethod.GET, "/bff/referral/${referral.id}/ics")
    }

    @Test
    fun `should return 403 forbidden when no roles provided`() {
      assertForbiddenNoRole(HttpMethod.GET, "/bff/referral/${referral.id}/ics")
    }

    @Test
    fun `should return 403 forbidden when wrong role provided`() {
      assertForbiddenWrongRole(HttpMethod.GET, "/bff/referral/${referral.id}/ics")
    }

    @Test
    fun `should return 404 not found for unknown referral id`() {
      assertNotFound(HttpMethod.GET, "/bff/referral/${UUID.randomUUID()}/ics")
    }

    @Test
    fun `should return 200 with empty list when no appointments exist`() {
      webTestClient.get()
        .uri("/bff/referral/${referral.id}/ics")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isOk
        .expectBodyList<AppointmentIcsResponse>()
        .hasSize(0)
    }

    @Test
    fun `should return 200 with all ics appointments for the referral`() {
      val referral = referralRepository.findById(referral.id).orElseThrow()

      val firstDelivery = appointmentHelper.createAppointmentDelivery(AppointmentDeliveryMethod.PHONE_CALL)
      val secondDelivery = appointmentHelper.createAppointmentDelivery(AppointmentDeliveryMethod.VIDEO_CALL)

      val firstAppointment = appointmentHelper.createAppointment(referral)
      val secondAppointment = appointmentHelper.createAppointment(referral)

      val firstAppointmentDateTime = LocalDateTime.of(2026, 3, 27, 10, 0)
      val firstCreatedAt = firstAppointmentDateTime.minusDays(1)

      val secondAppointmentDateTime = LocalDateTime.of(2026, 4, 5, 14, 30)
      val secondCreatedAt = secondAppointmentDateTime.minusDays(1)

      appointmentHelper.createAppointmentIcs(
        firstAppointment,
        firstDelivery,
        testUser,
        firstAppointmentDateTime,
        firstCreatedAt,
        listOf("Phone call"),
      )
      appointmentHelper.createAppointmentIcs(
        secondAppointment,
        secondDelivery,
        testUser,
        secondAppointmentDateTime,
        secondCreatedAt,
        listOf("Email", "Text message"),
      )

      appointmentHelper.createAppointmentStatusHistory(firstAppointment)
      appointmentHelper.createAppointmentStatusHistory(secondAppointment)

      val result = webTestClient.get()
        .uri("/bff/referral/${referral.id}/ics")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isOk
        .expectBodyList<AppointmentIcsResponse>()
        .hasSize(2)
        .returnResult()

      val items = result.responseBody!!.sortedBy { it.appointmentDate }

      val first = items[0]
      assertThat(first.referralId).isEqualTo(referral.id)
      assertThat(first.appointmentDate).isEqualTo(LocalDate.of(2026, 3, 27))
      assertThat(first.appointmentTime.hour).isEqualTo(10)
      assertThat(first.appointmentTime.amPm).isEqualTo("am")
      assertThat(first.sessionMethod).isInstanceOf(VirtualAppointment::class.java)
      assertThat(first.sessionMethod.type).isEqualTo("PHONE")
      assertThat(first.sessionCommunications).containsExactly("Phone call")
      assertThat(first.referralFirstName).isEqualTo("Alex")
      assertThat(first.referralLastName).isEqualTo("Jones")

      val second = items[1]
      assertThat(second.appointmentDate).isEqualTo(LocalDate.of(2026, 4, 5))
      assertThat(second.appointmentTime.hour).isEqualTo(2)
      assertThat(second.appointmentTime.amPm).isEqualTo("pm")
      assertThat(second.sessionMethod).isInstanceOf(VirtualAppointment::class.java)
      assertThat(second.sessionMethod.type).isEqualTo("VIDEO")
      assertThat(second.sessionCommunications).containsExactly("Email", "Text message")
      assertThat(second.referralFirstName).isEqualTo("Alex")
      assertThat(second.referralLastName).isEqualTo("Jones")
    }

    @Test
    fun `should not return appointments from a different referral`() {
      val firstReferral = referralRepository.findById(referral.id).orElseThrow()
      val person = referralHelper.createPerson(identifier = "Y888888")

      val secondReferral = referralHelper.createReferral(person, submittedBy = testUser)

      val firstAppointment = appointmentHelper.createAppointment(firstReferral)
      val secondAppointment = appointmentHelper.createAppointment(secondReferral)

      val firstAppointmentDateTime = LocalDateTime.of(2026, 3, 27, 10, 0)
      val firstCreatedAt = firstAppointmentDateTime.minusDays(1)

      val secondAppointmentDateTime = LocalDateTime.of(2026, 3, 28, 9, 0)
      val secondCreatedAt = secondAppointmentDateTime.minusDays(1)

      val delivery = appointmentHelper.createAppointmentDelivery()

      appointmentHelper.createAppointmentIcs(
        firstAppointment,
        delivery,
        testUser,
        firstAppointmentDateTime,
        firstCreatedAt,
        listOf("Phone call"),
      )
      appointmentHelper.createAppointmentIcs(
        secondAppointment,
        delivery,
        testUser,
        secondAppointmentDateTime,
        secondCreatedAt,
        listOf("Email", "Text message"),
      )

      appointmentHelper.createAppointmentStatusHistory(firstAppointment)
      appointmentHelper.createAppointmentStatusHistory(secondAppointment)

      val result = webTestClient.get()
        .uri("/bff/referral/${referral.id}/ics")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isOk
        .expectBodyList<AppointmentIcsResponse>()
        .hasSize(1)
        .returnResult()

      assertThat(result.responseBody!!.first().referralId).isEqualTo(referral.id)
    }
  }

  @Nested
  @DisplayName("GET /bff/referral/{referralId}/ics/{icsId}")
  inner class GetSingleIcsAppointmentEndpoint {

    @Test
    fun `should return 401 unauthorized when no token provided`() {
      assertUnauthorized(HttpMethod.GET, "/bff/referral/${referral.id}/ics/${UUID.randomUUID()}")
    }

    @Test
    fun `should return 403 forbidden when no roles provided`() {
      assertForbiddenNoRole(HttpMethod.GET, "/bff/referral/${referral.id}/ics/${UUID.randomUUID()}")
    }

    @Test
    fun `should return 403 forbidden when wrong role provided`() {
      assertForbiddenWrongRole(HttpMethod.GET, "/bff/referral/${referral.id}/ics/${UUID.randomUUID()}")
    }

    @Test
    fun `should return 404 not found for unknown ics id`() {
      assertNotFound(HttpMethod.GET, "/bff/referral/${referral.id}/appointment/ics/${UUID.randomUUID()}")
    }

    @Test
    fun `should return 200 with correct appointment details`() {
      val referral = referralRepository.findById(referral.id).orElseThrow()
      val appointment = appointmentHelper.createAppointment(referral)
      val appointmentDateTime = LocalDateTime.of(2026, 3, 27, 15, 0)
      val createdAt = appointmentDateTime.minusDays(1)
      val delivery = appointmentHelper.createAppointmentDelivery(
        AppointmentDeliveryMethod.VIDEO_CALL,
        "Zoom link",
      )
      val savedIcs = appointmentHelper.createAppointmentIcs(
        appointment,
        delivery,
        testUser,
        appointmentDateTime,
        createdAt,
        listOf("Email", "Phone call"),
      )
      appointmentHelper.createAppointmentStatusHistory(appointment)

      webTestClient.get()
        .uri("/bff/referral/${referral.id}/ics/${savedIcs.id}")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isOk
        .expectBody<AppointmentIcsResponse>()
        .consumeWith { result ->
          val body = result.responseBody!!
          body.appointmentIcsId shouldBe savedIcs.id
          body.referralId shouldBe referral.id
          body.appointmentType shouldBe AppointmentType.ICS
          body.appointmentDate shouldBe LocalDate.of(2026, 3, 27)
          body.appointmentTime.hour shouldBe 3
          body.appointmentTime.amPm shouldBe "pm"
          body.appointmentTime.minute shouldBe 0
          assertThat(body.sessionMethod).isInstanceOf(VirtualAppointment::class.java)
          with(body.sessionMethod as VirtualAppointment) {
            type shouldBe "VIDEO"
            whyNotInPersonReason shouldBe "Zoom link"
          }
          body.sessionCommunications shouldBe listOf("Email", "Phone call")
        }
    }

    @Test
    fun `should return 200 and correctly represent 12am midnight in response`() {
      val referral = referralRepository.findById(referral.id).orElseThrow()
      val delivery = appointmentHelper.createAppointmentDelivery()
      val appointment = appointmentHelper.createAppointment(referral)
      val appointmentDateTime = LocalDateTime.of(2026, 3, 27, 0, 0) // midnight
      val createdAt = appointmentDateTime.minusDays(1)
      val savedIcs = appointmentHelper.createAppointmentIcs(
        appointment,
        delivery,
        testUser,
        appointmentDateTime,
        createdAt,
        listOf("Email", "Phone call"),
      )
      appointmentHelper.createAppointmentStatusHistory(appointment)

      webTestClient.get()
        .uri("/bff/referral/${referral.id}/ics/${savedIcs.id}")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isOk
        .expectBody<AppointmentIcsResponse>()
        .consumeWith { result ->
          val body = result.responseBody!!
          body.appointmentTime.hour shouldBe 12
          body.appointmentTime.amPm shouldBe "am"
        }
    }
  }

  @Nested
  @DisplayName("GET /bff/referral/{caseReference}/ics_appointment_feedback_details")
  inner class GetIcsFeedbackSessionDetailsEndpoint {

    @Test
    fun `should return 401 unauthorized when no token provided`() {
      assertUnauthorized(HttpMethod.GET, "/bff/referral/${referral.referenceNumber}/ics_appointment_feedback_details")
    }

    @Test
    fun `should return 403 forbidden when no roles provided`() {
      assertForbiddenNoRole(HttpMethod.GET, "/bff/referral/${referral.referenceNumber}/ics_appointment_feedback_details")
    }

    @Test
    fun `should return 403 forbidden when wrong role provided`() {
      assertForbiddenWrongRole(HttpMethod.GET, "/bff/referral/${referral.referenceNumber}/ics_appointment_feedback_details")
    }

    @Test
    fun `should return 404 when referral case reference is not found`() {
      assertNotFound(HttpMethod.GET, "/bff/referral/${referral.referenceNumber}/ics_appointment_feedback_details")
    }

    @Test
    fun `should return 200 with correct ICS feedback session details`() {
      val referral = referralRepository.findById(referral.id).orElseThrow()
      val appointment = appointmentHelper.createAppointment(referral)
      val appointmentDateTime = LocalDateTime.of(2026, 3, 27, 15, 0)
      val createdAt = appointmentDateTime.minusDays(1)
      val delivery = appointmentHelper.createAppointmentDelivery(AppointmentDeliveryMethod.VIDEO_CALL, "Zoom link")

      appointmentHelper.createAppointmentIcs(
        appointment,
        delivery,
        testUser,
        appointmentDateTime,
        createdAt,
        listOf("Email", "Phone call"),
      )
      appointmentHelper.createAppointmentStatusHistory(appointment)

      webTestClient.get()
        .uri("/bff/referral/${referral.referenceNumber}/ics_appointment_feedback_details")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isOk
        .expectBody<IcsFeedbackSessionDto>()
        .consumeWith { result ->
          val body = result.responseBody!!
          body.fullName shouldBe "Alex Jones"
          body.appointmentDetails?.method shouldBe AppointmentDeliveryMethod.VIDEO_CALL
          body.appointmentDetails?.date shouldBe "27/03/2026"
          body.appointmentDetails?.time shouldBe "15:00"
          body.otherAppointmentMethods shouldBe listOf("Email", "Phone call")
        }
    }
  }

  private fun buildRequest(
    date: LocalDate = LocalDate.of(2026, 3, 27),
    hour: Int = 10,
    minute: Int? = 0,
    amPm: String = "am",
    type: SessionMethodType = SessionMethodType.PHONE,
    additionalDetails: String? = null,
    sessionCommunication: List<String> = listOf("Phone call"),
  ) = CreateAppointmentRequest(
    date = date,
    time = AppointmentTimeRequest(hour = hour, minute = minute, amPm = amPm),
    sessionMethodRequest = SessionMethodRequest(type = type, additionalDetails = additionalDetails),
    sessionCommunication = sessionCommunication,
  )
}
