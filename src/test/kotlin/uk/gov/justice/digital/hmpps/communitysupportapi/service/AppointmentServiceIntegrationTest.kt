package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.AppointmentTimeRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CreateAppointmentRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CreateIcsFeedbackRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.InPersonAppointment
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.RecordSessionRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionDurationRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionMethodRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionMethodType
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.VirtualAppointment
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActorType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentDeliveryMethod
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentStatusHistoryType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralEventType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.AppointmentTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.ReferralTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentDeliveryRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentIcsFeedbackRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentIcsRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentStatusHistoryRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralEventRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralUserRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.CreateIcsFeedbackRequestFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.PersonFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ReferralFactory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

class AppointmentServiceIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var appointmentService: AppointmentService

  @Autowired
  private lateinit var appointmentRepository: AppointmentRepository

  @Autowired
  private lateinit var appointmentDeliveryRepository: AppointmentDeliveryRepository

  @Autowired
  private lateinit var appointmentIcsRepository: AppointmentIcsRepository

  @Autowired
  private lateinit var appointmentStatusHistoryRepository: AppointmentStatusHistoryRepository

  @Autowired
  private lateinit var appointmentIcsFeedbackRepository: AppointmentIcsFeedbackRepository

  @Autowired
  private lateinit var referralEventRepository: ReferralEventRepository

  @Autowired
  private lateinit var appointmentHelper: AppointmentTestSupport

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @Autowired
  private lateinit var referralUserRepository: ReferralUserRepository

  @Autowired
  private lateinit var referralHelper: ReferralTestSupport

  private lateinit var referral: Referral

  @Autowired
  private lateinit var personRepository: PersonRepository

  private lateinit var person: uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person

  private lateinit var testUser: ReferralUser

  @BeforeEach
  fun setUpReferral() {
    testUser = referralHelper.ensureReferralUser()
    person = referralHelper.createPerson(firstName = "Alex", lastName = "Jones", identifier = "X654321")
    referral = referralHelper.createReferral(person, submittedBy = testUser)
  }

  @Nested
  @DisplayName("createIcsAppointment")
  inner class CreateIcsAppointment {

    @Test
    fun `should persist appointment, delivery and ics records for a phone call`() {
      val request = buildRequest(
        hour = 10,
        minute = 30,
        amPm = "am",
        type = SessionMethodType.PHONE,
        additionalDetails = "Call on mobile",
        sessionCommunication = listOf("Phone call", "Text message"),
      )

      val response = appointmentService.createIcsAppointment(referral.referenceNumber!!, request, testUser)

      // Appointment persisted
      val savedAppointment = appointmentRepository.findById(response.appointmentId).orElseThrow()
      assertThat(savedAppointment.referral.id).isEqualTo(referral.id)
      assertThat(savedAppointment.type).isEqualTo(AppointmentType.ICS)

      // Status History persisted
      val savedAppointmentStatusHistory =
        appointmentStatusHistoryRepository.findTopByAppointmentIdOrderByCreatedAtDesc(response.appointmentId)
      assertThat(savedAppointmentStatusHistory?.appointment?.id).isEqualTo(savedAppointment.id)
      assertThat(savedAppointmentStatusHistory?.status).isEqualTo(AppointmentStatusHistoryType.SCHEDULED)

      // Delivery persisted
      val savedIcs = appointmentIcsRepository.findById(response.appointmentIcsId).orElseThrow()
      val savedDelivery = appointmentDeliveryRepository.findById(savedIcs.appointmentDelivery!!.id).orElseThrow()
      assertThat(savedDelivery.method).isEqualTo(AppointmentDeliveryMethod.PHONE_CALL)
      assertThat(savedDelivery.methodDetails).isEqualTo("Call on mobile")

      // ICS persisted
      assertThat(savedIcs.appointmentDateTime.toLocalDate()).isEqualTo(LocalDate.of(2026, 3, 27))
      assertThat(savedIcs.appointmentDateTime.toLocalTime()).isEqualTo(LocalTime.of(10, 30))
      assertThat(savedIcs.sessionCommunication).containsExactly("Phone call", "Text message")
    }

    @Test
    fun `should correctly convert 12-hour am time to 24-hour`() {
      val request = buildRequest(hour = 12, minute = 0, amPm = "am") // midnight edge case
      val response = appointmentService.createIcsAppointment(referral.referenceNumber!!, request, testUser)

      val ics = appointmentIcsRepository.findById(response.appointmentIcsId).orElseThrow()
      assertThat(ics.appointmentDateTime.hour).isEqualTo(0) // 12am → 00:xx
    }

    @Test
    fun `should correctly convert 12-hour pm time to 24-hour`() {
      val request = buildRequest(hour = 12, minute = 0, amPm = "pm") // noon
      val response = appointmentService.createIcsAppointment(referral.referenceNumber!!, request, testUser)

      val ics = appointmentIcsRepository.findById(response.appointmentIcsId).orElseThrow()
      assertThat(ics.appointmentDateTime.hour).isEqualTo(12) // 12pm stays 12
    }

    @Test
    fun `should correctly convert 1pm to 13 in 24-hour format`() {
      val request = buildRequest(hour = 1, minute = 0, amPm = "pm")
      val response = appointmentService.createIcsAppointment(referral.referenceNumber!!, request, testUser)

      val ics = appointmentIcsRepository.findById(response.appointmentIcsId).orElseThrow()
      assertThat(ics.appointmentDateTime.hour).isEqualTo(13)
    }

    @Test
    fun `should map VIDEO type to VIDEO_CALL delivery method`() {
      val request = buildRequest(type = SessionMethodType.VIDEO, additionalDetails = "Teams link")
      val response = appointmentService.createIcsAppointment(referral.referenceNumber!!, request, testUser)

      val ics = appointmentIcsRepository.findById(response.appointmentIcsId).orElseThrow()
      assertThat(ics.appointmentDelivery?.method).isEqualTo(AppointmentDeliveryMethod.VIDEO_CALL)
      assertThat(ics.appointmentDelivery?.methodDetails).isEqualTo("Teams link")

      val sessionMethod = response.sessionMethod
      assertThat(sessionMethod).isInstanceOf(VirtualAppointment::class.java)
      assertThat((sessionMethod as VirtualAppointment).whyNotInPersonReason).isEqualTo("Teams link")
    }

    @Test
    fun `should map IN_PERSON_OTHER_LOCATION and persist address fields`() {
      val request = buildRequest(
        type = SessionMethodType.OTHER_LOCATION,
        additionalDetails = "Side entrance",
      ).copy(
        sessionMethodRequest = SessionMethodRequest(
          type = SessionMethodType.OTHER_LOCATION,
          additionalDetails = "Side entrance",
          addressLine1 = "10 Downing Street",
          addressLine2 = null,
          townOrCity = "London",
          county = "Greater London",
          postcode = "SW1A 2AA",
        ),
      )
      val response = appointmentService.createIcsAppointment(referral.referenceNumber!!, request, testUser)

      val ics = appointmentIcsRepository.findById(response.appointmentIcsId).orElseThrow()
      val delivery = ics.appointmentDelivery!!
      assertThat(delivery.method).isEqualTo(AppointmentDeliveryMethod.IN_PERSON_OTHER_LOCATION)
      assertThat(delivery.addressLine1).isEqualTo("10 Downing Street")
      assertThat(delivery.townOrCity).isEqualTo("London")
      assertThat(delivery.postcode).isEqualTo("SW1A 2AA")

      val sessionMethod = response.sessionMethod
      assertThat(sessionMethod).isInstanceOf(InPersonAppointment::class.java)
      with(sessionMethod as InPersonAppointment) {
        assertThat(type).isEqualTo("IN_PERSON_OTHER_LOCATION")
        assertThat(addressLine1).isEqualTo("10 Downing Street")
        assertThat(townOrCity).isEqualTo("London")
        assertThat(postcode).isEqualTo("SW1A 2AA")
      }
    }

    @Test
    fun `should return VirtualAppointment for phone with no address fields`() {
      val request = buildRequest(type = SessionMethodType.PHONE)
      val response = appointmentService.createIcsAppointment(referral.referenceNumber!!, request, testUser)

      val sessionMethod = response.sessionMethod
      assertThat(sessionMethod).isInstanceOf(VirtualAppointment::class.java)
      // VirtualAppointment must not carry any address data
      val virtual = sessionMethod as VirtualAppointment
      assertThat(virtual.type).isEqualTo("PHONE")
      assertThat(virtual.whyNotInPersonReason).isNull()
    }

    @Test
    fun `should persist empty sessionCommunication list as empty session_communication`() {
      val request = buildRequest(sessionCommunication = emptyList())
      val response = appointmentService.createIcsAppointment(referral.referenceNumber!!, request, testUser)

      val ics = appointmentIcsRepository.findById(response.appointmentIcsId).orElseThrow()
      assertThat(ics.sessionCommunication).isEmpty()
    }

    @Test
    fun `should throw NotFoundException for unknown referral id`() {
      val referralId = UUID.randomUUID()
      val request = buildRequest()

      val notFoundException = assertThrows<NotFoundException> {
        appointmentService.createIcsAppointment(referralId.toString(), request, testUser)
      }
      assertThat(notFoundException.message).isEqualTo("Referral not found for id ${referralId}")
    }

    @Test
    fun `should link ics to the correct referral via appointment`() {
      val request = buildRequest()
      val response = appointmentService.createIcsAppointment(referral.referenceNumber!!, request, testUser)

      assertThat(response.referralId).isEqualTo(referral.id)
    }

    @Test
    fun `should persist createdBy when a user is supplied`() {
      val testUser = referralUserRepository.findById(testUser.id).orElseThrow()
      val request = buildRequest()
      val response = appointmentService.createIcsAppointment(referral.referenceNumber!!, request, testUser)

      val ics = appointmentIcsRepository.findById(response.appointmentIcsId).orElseThrow()
      assertThat(ics.createdBy.id).isEqualTo(testUser.id)
    }
  }

  @Nested
  @DisplayName("getIcsAppointmentsByReferral")
  inner class GetIcsAppointmentsByReferral {

    @Test
    fun `should return empty list when no appointments exist for the referral`() {
      val result = appointmentService.getIcsAppointmentsByReferral(referral.id)
      assertThat(result).isEmpty()
    }

    @Test
    fun `should return all appointments for the referral`() {
      appointmentService.createIcsAppointment(referral.referenceNumber!!, buildRequest(hour = 9, amPm = "am"), testUser)
      appointmentService.createIcsAppointment(referral.referenceNumber!!, buildRequest(hour = 2, amPm = "pm"), testUser)

      val result = appointmentService.getIcsAppointmentsByReferral(referral.id)
      assertThat(result).hasSize(2)
    }

    @Test
    fun `should not return appointments belonging to a different referral`() {
      // Second referral
      val anotherPerson = personRepository.save(PersonFactory().withIdentifier("Y999999").create())
      val anotherReferral = referralRepository.save(
        ReferralFactory().withPersonId(anotherPerson.id).withCrn("Y999999").create(),
      )

      appointmentService.createIcsAppointment(referral.referenceNumber!!, buildRequest(), testUser)
      appointmentService.createIcsAppointment(anotherReferral.referenceNumber!!, buildRequest(), testUser)

      val result = appointmentService.getIcsAppointmentsByReferral(referral.id)
      assertThat(result).hasSize(1)
      assertThat(result.first().referralId).isEqualTo(referral.id)
    }

    @Test
    fun `should throw NotFoundException for unknown referral id`() {
      assertThrows<NotFoundException> {
        appointmentService.getIcsAppointmentsByReferral(UUID.randomUUID())
      }
    }

    @Test
    fun `should return correct date and time in response`() {
      appointmentService.createIcsAppointment(
        referral.referenceNumber!!,
        buildRequest(hour = 3, minute = 15, amPm = "pm"),
        testUser,
      )
      val result = appointmentService.getIcsAppointmentsByReferral(referral.id)
      val time = result.first().appointmentTime

      assertThat(time.hour).isEqualTo(3)
      assertThat(time.minute).isEqualTo(15)
      assertThat(time.amPm).isEqualTo("pm")
    }
  }

  @Nested
  @DisplayName("getIcsAppointment")
  inner class GetIcsAppointment {

    @Test
    fun `should return the correct ICS appointment by id`() {
      val created = appointmentService.createIcsAppointment(referral.referenceNumber!!, buildRequest(hour = 11, amPm = "am"), testUser)
      val fetched = appointmentService.getIcsAppointment(created.appointmentIcsId)

      assertThat(fetched.appointmentIcsId).isEqualTo(created.appointmentIcsId)
      assertThat(fetched.referralId).isEqualTo(referral.id)
      assertThat(fetched.appointmentType).isEqualTo(AppointmentType.ICS)
    }

    @Test
    fun `should throw NotFoundException for unknown ics id`() {
      assertThrows<NotFoundException> {
        appointmentService.getIcsAppointment(UUID.randomUUID())
      }
    }

    @Test
    fun `should return correct sessionCommunication in response`() {
      val created = appointmentService.createIcsAppointment(
        referral.referenceNumber!!,
        buildRequest(sessionCommunication = listOf("Email", "Phone call")),
        testUser,
      )
      val fetched = appointmentService.getIcsAppointment(created.appointmentIcsId)

      assertThat(fetched.sessionCommunications).containsExactly("Email", "Phone call")
    }

    @Test
    fun `should return correct howTakePlace type in response`() {
      val created = appointmentService.createIcsAppointment(
        referral.referenceNumber!!,
        buildRequest(type = SessionMethodType.PROBATION_OFFICE),
        testUser,
      )
      val fetched = appointmentService.getIcsAppointment(created.appointmentIcsId)

      val sessionMethod = fetched.sessionMethod
      assertThat(sessionMethod).isInstanceOf(InPersonAppointment::class.java)
      assertThat(sessionMethod.type).isEqualTo("IN_PERSON_PROBATION_OFFICE")
    }
  }

  @Nested
  @DisplayName("getIcsAppointmentsByReferral")
  inner class GetIcsFeedbackSessionDetails {

    @Test
    fun `should throw NotFoundException if no appointment exists`() {
      assertThrows<NotFoundException> {
        appointmentService.getIcsFeedbackSessionDetails(referral)
      }
    }

    @Test
    fun `should return ICS Feedback session details if no appointment delivery method was provided`() {
      val appointment = appointmentHelper.createAppointment(referral)
      val appointmentDateTime = LocalDateTime.of(2026, 4, 5, 14, 30)
      val createdAt = appointmentDateTime.minusDays(1)

      appointmentHelper.createAppointmentIcs(
        appointment,
        null,
        testUser,
        appointmentDateTime,
        createdAt,
        listOf("Email"),
      )

      appointmentHelper.createAppointmentStatusHistory(appointment)

      val result = appointmentService.getIcsFeedbackSessionDetails(referral)

      assertThat(result.fullName).isEqualTo("Alex Jones")
      assertThat(result.appointmentDetails?.method).isNull()
      assertThat(result.appointmentDetails?.date).isEqualTo("05/04/2026")
      assertThat(result.appointmentDetails?.time).isEqualTo("14:30")
    }

    @Test
    fun `should return ICS feedback session details for appointment with latest appointmentDateTime`() {
      val firstAppointment = appointmentHelper.createAppointment(referral)
      val firstDelivery = appointmentHelper.createAppointmentDelivery(AppointmentDeliveryMethod.PHONE_CALL)
      val firstAppointmentDateTime = LocalDateTime.of(2026, 3, 27, 10, 0)
      val firstCreatedAt = firstAppointmentDateTime.minusDays(1)

      val secondAppointment = appointmentHelper.createAppointment(referral)
      val secondDelivery = appointmentHelper.createAppointmentDelivery(AppointmentDeliveryMethod.VIDEO_CALL)
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

      val result = appointmentService.getIcsFeedbackSessionDetails(referral)

      assertThat(result.fullName).isEqualTo("Alex Jones")
      assertThat(result.appointmentDetails?.method).isEqualTo(AppointmentDeliveryMethod.VIDEO_CALL)
      assertThat(result.appointmentDetails?.date).isEqualTo("05/04/2026")
      assertThat(result.appointmentDetails?.time).isEqualTo("14:30")
      assertThat(result.otherAppointmentMethods).isEqualTo(listOf("Email", "Text message"))
    }

    @Test
    fun `should return ICS Feedback session details for latest created ICS appointment when appointmentDateTime is identical`() {
      val appointment1 = appointmentHelper.createAppointment(referral)
      val delivery1 =
        appointmentHelper.createAppointmentDelivery(AppointmentDeliveryMethod.VIDEO_CALL)

      val appointment2 = appointmentHelper.createAppointment(referral)
      val delivery2 =
        appointmentHelper.createAppointmentDelivery(AppointmentDeliveryMethod.PHONE_CALL)

      val sharedDateTime = LocalDateTime.of(2026, 4, 17, 17, 30)

      appointmentHelper.createAppointmentIcs(
        appointment1,
        delivery1,
        testUser,
        sharedDateTime,
        sharedDateTime.minusDays(2),
        listOf("Email"),
      )

      appointmentHelper.createAppointmentIcs(
        appointment2,
        delivery2,
        testUser,
        sharedDateTime,
        sharedDateTime.minusDays(1),
        listOf("Text message"),
      )

      appointmentHelper.createAppointmentStatusHistory(appointment1)
      appointmentHelper.createAppointmentStatusHistory(appointment2)

      val result = appointmentService.getIcsFeedbackSessionDetails(referral)

      assertThat(result.fullName).isEqualTo("Alex Jones")
      assertThat(result.appointmentDetails?.method).isEqualTo(AppointmentDeliveryMethod.PHONE_CALL)
      assertThat(result.appointmentDetails?.date).isEqualTo("17/04/2026")
      assertThat(result.appointmentDetails?.time).isEqualTo("17:30")
      assertThat(result.otherAppointmentMethods).isEqualTo(listOf("Text message"))
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

  /** Creates an ICS appointment and returns its id, ready to receive feedback. */
  private fun createIcsAndGetId(): UUID = appointmentService.createIcsAppointment(referral.referenceNumber!!, buildRequest(), testUser).appointmentIcsId

  private fun buildFeedbackRequest(
    didSessionHappen: Boolean = true,
    howSessionTookPlace: SessionMethodRequest? = SessionMethodRequest(type = SessionMethodType.PHONE),
    wasPersonLate: Boolean? = false,
    lateReason: String? = null,
    duration: SessionDurationRequest? = SessionDurationRequest(hours = 1, minutes = 45),
    whatHappened: String? = "Reviewed progress on employment goals",
    behaviour: String? = "Engaged and motivated",
    strengthsIdentified: String? = "Good communication",
    issuesConcernsIdentified: String? = null,
    notifyProbationPractitioner: Boolean? = false,
    plannedForNextSession: String? = "Continue with action plan",
    actionsBeforeNextSession: String? = "Complete referral details",
  ): CreateIcsFeedbackRequest = CreateIcsFeedbackRequestFactory()
    .withDidSessionHappen(didSessionHappen)
    .withHowSessionTookPlace(howSessionTookPlace)
    .withWasPersonLate(wasPersonLate)
    .withLateReason(lateReason)
    .withDuration(duration)
    .withWhatHappened(whatHappened)
    .withBehaviour(behaviour)
    .withStrengthsIdentified(strengthsIdentified)
    .withIssuesConcernsIdentified(issuesConcernsIdentified)
    .withNotifyProbationPractitioner(notifyProbationPractitioner)
    .withPlannedForNextSession(plannedForNextSession)
    .withActionsBeforeNextSession(actionsBeforeNextSession)
    .create()

  @Nested
  @DisplayName("createIcsFeedback")
  inner class CreateIcsFeedback {

    @Test
    fun `should persist all feedback fields and return correct response`() {
      val icsId = createIcsAndGetId()
      val request = buildFeedbackRequest(
        didSessionHappen = true,
        howSessionTookPlace = SessionMethodRequest(
          type = SessionMethodType.PHONE,
          additionalDetails = "Client preferred phone",
        ),
        wasPersonLate = true,
        lateReason = "Car trouble",
        duration = SessionDurationRequest(hours = 1, minutes = 45),
        whatHappened = "Reviewed employment goals",
        behaviour = "Engaged and motivated",
        strengthsIdentified = "Excellent communication",
        issuesConcernsIdentified = "Risk of housing loss",
        notifyProbationPractitioner = true,
        plannedForNextSession = "Review housing options",
        actionsBeforeNextSession = "Contact housing officer",
      )

      val response = appointmentService.createIcsFeedback(referral.id, icsId, request, testUser)

      // Verify DB persistence
      val saved = appointmentIcsFeedbackRepository.findById(response.id).orElseThrow()
      assertThat(saved.appointmentIcs.id).isEqualTo(icsId)
      assertThat(saved.recordSessionDidSessionHappen).isTrue()
      assertThat(saved.recordSessionHowSessionTookPlace).isEqualTo("Phone call")
      assertThat(saved.recordSessionNotInPersonReason).isEqualTo("Client preferred phone")
      assertThat(saved.sessionDetailsWasPersonLate).isTrue()
      assertThat(saved.sessionDetailsLateReason).isEqualTo("Car trouble")
      assertThat(saved.sessionDetailsDuration).isEqualTo("1 hour and 45 minutes")
      assertThat(saved.sessionFeedbackWhatHappened).isEqualTo("Reviewed employment goals")
      assertThat(saved.sessionFeedbackBehaviour).isEqualTo("Engaged and motivated")
      assertThat(saved.sessionFeedbackStrengthsIdentified).isEqualTo("Excellent communication")
      assertThat(saved.issuesConcernsIdentified).isEqualTo("Risk of housing loss")
      assertThat(saved.issuesConcernsNotifyProbationPractitioner).isTrue()
      assertThat(saved.nextStepsPlannedForNextSession).isEqualTo("Review housing options")
      assertThat(saved.nextStepsActionsBeforeNextSession).isEqualTo("Contact housing officer")

      // Verify audit event created
      val events = referralEventRepository.findAll().filter { it.eventType == ReferralEventType.APPOINTMENT_FEEDBACK_SENT }
      assertThat(events).hasSize(1)
      assertThat(events.first().referral.referenceNumber!!).isEqualTo(referral.referenceNumber!!)
      assertThat(events.first().actorType).isEqualTo(ActorType.EXTERNAL)
      assertThat(events.first().actorId).isEqualTo(testUser.id)
    }

    @Test
    fun `should persist createdBy as the submitting user`() {
      val icsId = createIcsAndGetId()
      val response = appointmentService.createIcsFeedback(referral.id, icsId, buildFeedbackRequest(), testUser)

      val saved = appointmentIcsFeedbackRepository.findById(response.id).orElseThrow()
      assertThat(saved.createdBy?.id).isEqualTo(testUser.id)
      assertThat(response.createdBy).isEqualTo(testUser.id)
    }

    @Test
    fun `should persist only the required field when all optional fields are null`() {
      val icsId = createIcsAndGetId()
      val minimalRequest = CreateIcsFeedbackRequest(record = RecordSessionRequest(didSessionHappen = false))

      val response = appointmentService.createIcsFeedback(referral.id, icsId, minimalRequest, testUser)

      assertThat(response.recordSessionDidSessionHappen).isFalse()
      assertThat(response.recordSessionHowSessionTookPlace).isNull()
      assertThat(response.recordSessionNotInPersonReason).isNull()
      assertThat(response.sessionDetailsWasPersonLate).isNull()
      assertThat(response.sessionDetailsLateReason).isNull()
      assertThat(response.sessionDetailsDuration).isNull()
      assertThat(response.sessionFeedbackWhatHappened).isNull()
      assertThat(response.sessionFeedbackBehaviour).isNull()
      assertThat(response.sessionFeedbackStrengthsIdentified).isNull()
      assertThat(response.issuesOrConcernsIdentified).isNull()
      assertThat(response.issuesOrConcernsNotifyProbationPractitioner).isNull()
      assertThat(response.nextStepsPlannedForNextSession).isNull()
      assertThat(response.nextStepsActionsBeforeNextSession).isNull()
    }

    @Test
    fun `should format 2 hours and 30 minutes correctly`() {
      val icsId = createIcsAndGetId()
      val request = buildFeedbackRequest(duration = SessionDurationRequest(hours = 2, minutes = 30))

      val response = appointmentService.createIcsFeedback(referral.id, icsId, request, testUser)

      assertThat(response.sessionDetailsDuration).isEqualTo("2 hours and 30 minutes")
      val saved = appointmentIcsFeedbackRepository.findById(response.id).orElseThrow()
      assertThat(saved.sessionDetailsDuration).isEqualTo("2 hours and 30 minutes")
    }

    @Test
    fun `should throw NotFoundException when ics appointment does not exist`() {
      val unknownIcsId = UUID.randomUUID()

      val ex = assertThrows<NotFoundException> {
        appointmentService.createIcsFeedback(referral.id, unknownIcsId, buildFeedbackRequest(), testUser)
      }
      assertThat(ex.message).isEqualTo("ICS appointment not found for id $unknownIcsId")
    }

    @Test
    fun `should throw NotFoundException when ics appointment belongs to a different referral`() {
      val otherPerson = personRepository.save(PersonFactory().withIdentifier("Z777777").create())
      val otherReferral = referralRepository.save(
        ReferralFactory().withPersonId(otherPerson.id).withCrn("Z777777").create(),
      )

      val otherIcsId = appointmentService.createIcsAppointment(otherReferral.referenceNumber!!, buildRequest(), testUser).appointmentIcsId

      val ex = assertThrows<NotFoundException> {
        appointmentService.createIcsFeedback(referral.id, otherIcsId, buildFeedbackRequest(), testUser)
      }
      assertThat(ex.message).contains("does not belong to referral")
    }

    @Test
    fun `should persist pdu when session happened at probation office`() {
      val icsId = createIcsAndGetId()
      val request = buildFeedbackRequest(
        howSessionTookPlace = SessionMethodRequest(
          type = SessionMethodType.PROBATION_OFFICE,
          pdu = "PDU-South-East",
        ),
      )

      val response = appointmentService.createIcsFeedback(referral.id, icsId, request, testUser)

      val saved = appointmentIcsFeedbackRepository.findById(response.id).orElseThrow()
      assertThat(saved.recordSessionHowSessionTookPlace).isEqualTo("In person (probation office)")
      assertThat(saved.recordSessionPdu).isEqualTo("PDU-South-East")
      assertThat(saved.recordSessionNotInPersonReason).isNull()
      assertThat(saved.recordSessionAddressLine1).isNull()
      assertThat(response.recordSessionPdu).isEqualTo("PDU-South-East")
    }

    @Test
    fun `should persist address when session happened at other location`() {
      val icsId = createIcsAndGetId()
      val request = buildFeedbackRequest(
        howSessionTookPlace = SessionMethodRequest(
          type = SessionMethodType.OTHER_LOCATION,
          addressLine1 = "10 Downing Street",
          townOrCity = "London",
          postcode = "SW1A 2AA",
        ),
      )

      val response = appointmentService.createIcsFeedback(referral.id, icsId, request, testUser)

      val saved = appointmentIcsFeedbackRepository.findById(response.id).orElseThrow()
      assertThat(saved.recordSessionHowSessionTookPlace).isEqualTo("In person (other location)")
      assertThat(saved.recordSessionAddressLine1).isEqualTo("10 Downing Street")
      assertThat(saved.recordSessionTownOrCity).isEqualTo("London")
      assertThat(saved.recordSessionPostcode).isEqualTo("SW1A 2AA")
      assertThat(saved.recordSessionPdu).isNull()
    }

    @Test
    fun `should create APPOINTMENT_FEEDBACK_SENT referral event after saving feedback`() {
      val icsId = createIcsAndGetId()

      appointmentService.createIcsFeedback(referral.id, icsId, buildFeedbackRequest(), testUser)

      val events = referralEventRepository.findAll()
        .filter { it.eventType == ReferralEventType.APPOINTMENT_FEEDBACK_SENT }
      assertThat(events).hasSize(1)
      with(events.first()) {
        assertThat(referral.referenceNumber!!).isEqualTo(referral.referenceNumber!!)
        assertThat(actorType).isEqualTo(ActorType.EXTERNAL)
        assertThat(actorId).isEqualTo(testUser.id)
        assertThat(createdAt).isNotNull()
      }
    }

    @Test
    fun `should create one audit event per feedback submission`() {
      val icsId1 = createIcsAndGetId()
      val icsId2 = createIcsAndGetId()

      appointmentService.createIcsFeedback(referral.id, icsId1, buildFeedbackRequest(), testUser)
      appointmentService.createIcsFeedback(referral.id, icsId2, buildFeedbackRequest(), testUser)

      val events = referralEventRepository.findAll()
        .filter { it.eventType == ReferralEventType.APPOINTMENT_FEEDBACK_SENT }
      assertThat(events).hasSize(2)
      assertThat(events.all { it.referral.id == referral.id }).isTrue()
    }
  }
}
