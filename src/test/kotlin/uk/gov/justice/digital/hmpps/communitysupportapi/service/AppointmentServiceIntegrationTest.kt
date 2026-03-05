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
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.InPersonAppointment
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionMethodRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionMethodType
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.VirtualAppointment
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentDeliveryMethod
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentStatusHistoryType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.ReferralTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentDeliveryRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentIcsRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentStatusHistoryRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralUserRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.PersonFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ReferralFactory
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

class AppointmentServiceIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var appointmentService: AppointmentService

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @Autowired
  private lateinit var personRepository: PersonRepository

  @Autowired
  private lateinit var appointmentRepository: AppointmentRepository

  @Autowired
  private lateinit var appointmentDeliveryRepository: AppointmentDeliveryRepository

  @Autowired
  private lateinit var appointmentIcsRepository: AppointmentIcsRepository

  @Autowired
  private lateinit var appointmentStatusHistoryRepository: AppointmentStatusHistoryRepository

  @Autowired
  private lateinit var referralUserRepository: ReferralUserRepository

  @Autowired
  private lateinit var referralHelper: ReferralTestSupport

  private lateinit var referralId: UUID

  private lateinit var testUser: ReferralUser

  @BeforeEach
  fun setUpReferral() {
    val person = referralHelper.createPerson(firstName = "Alex", lastName = "Jones", identifier = "X654321")

    testUser = referralHelper.createReferralUser()

    val referral = referralHelper.createReferral(person, submittedBy = testUser)

    referralId = referral.id
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

      val response = appointmentService.createIcsAppointment(referralId, request, testUser)

      // Appointment persisted
      val savedAppointment = appointmentRepository.findById(response.appointmentId).orElseThrow()
      assertThat(savedAppointment.referral.id).isEqualTo(referralId)
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
      val response = appointmentService.createIcsAppointment(referralId, request, testUser)

      val ics = appointmentIcsRepository.findById(response.appointmentIcsId).orElseThrow()
      assertThat(ics.appointmentDateTime.hour).isEqualTo(0) // 12am → 00:xx
    }

    @Test
    fun `should correctly convert 12-hour pm time to 24-hour`() {
      val request = buildRequest(hour = 12, minute = 0, amPm = "pm") // noon
      val response = appointmentService.createIcsAppointment(referralId, request, testUser)

      val ics = appointmentIcsRepository.findById(response.appointmentIcsId).orElseThrow()
      assertThat(ics.appointmentDateTime.hour).isEqualTo(12) // 12pm stays 12
    }

    @Test
    fun `should correctly convert 1pm to 13 in 24-hour format`() {
      val request = buildRequest(hour = 1, minute = 0, amPm = "pm")
      val response = appointmentService.createIcsAppointment(referralId, request, testUser)

      val ics = appointmentIcsRepository.findById(response.appointmentIcsId).orElseThrow()
      assertThat(ics.appointmentDateTime.hour).isEqualTo(13)
    }

    @Test
    fun `should map VIDEO type to VIDEO_CALL delivery method`() {
      val request = buildRequest(type = SessionMethodType.VIDEO, additionalDetails = "Teams link")
      val response = appointmentService.createIcsAppointment(referralId, request, testUser)

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
      val response = appointmentService.createIcsAppointment(referralId, request, testUser)

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
      val response = appointmentService.createIcsAppointment(referralId, request, testUser)

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
      val response = appointmentService.createIcsAppointment(referralId, request, testUser)

      val ics = appointmentIcsRepository.findById(response.appointmentIcsId).orElseThrow()
      assertThat(ics.sessionCommunication).isEmpty()
    }

    @Test
    fun `should throw NotFoundException for unknown referral id`() {
      val unknownId = UUID.randomUUID()
      val request = buildRequest()

      val notFoundException = assertThrows<NotFoundException> {
        appointmentService.createIcsAppointment(unknownId, request, testUser)
      }
      assertThat(notFoundException.message).isEqualTo("Referral not found for id $unknownId")
    }

    @Test
    fun `should link ics to the correct referral via appointment`() {
      val request = buildRequest()
      val response = appointmentService.createIcsAppointment(referralId, request, testUser)

      assertThat(response.referralId).isEqualTo(referralId)
    }

    @Test
    fun `should persist createdBy when a user is supplied`() {
      val testUser = referralUserRepository.findById(testUser.id).orElseThrow()
      val request = buildRequest()
      val response = appointmentService.createIcsAppointment(referralId, request, testUser)

      val ics = appointmentIcsRepository.findById(response.appointmentIcsId).orElseThrow()
      assertThat(ics.createdBy.id).isEqualTo(testUser.id)
    }
  }

  @Nested
  @DisplayName("getIcsAppointmentsByReferral")
  inner class GetIcsAppointmentsByReferral {

    @Test
    fun `should return empty list when no appointments exist for the referral`() {
      val result = appointmentService.getIcsAppointmentsByReferral(referralId)
      assertThat(result).isEmpty()
    }

    @Test
    fun `should return all appointments for the referral`() {
      appointmentService.createIcsAppointment(referralId, buildRequest(hour = 9, amPm = "am"), testUser)
      appointmentService.createIcsAppointment(referralId, buildRequest(hour = 2, amPm = "pm"), testUser)

      val result = appointmentService.getIcsAppointmentsByReferral(referralId)
      assertThat(result).hasSize(2)
    }

    @Test
    fun `should not return appointments belonging to a different referral`() {
      // Second referral
      val anotherPerson = personRepository.save(PersonFactory().withIdentifier("Y999999").create())
      val anotherReferral = referralRepository.save(
        ReferralFactory().withPersonId(anotherPerson.id).withCrn("Y999999").create(),
      )

      appointmentService.createIcsAppointment(referralId, buildRequest(), testUser)
      appointmentService.createIcsAppointment(anotherReferral.id, buildRequest(), testUser)

      val result = appointmentService.getIcsAppointmentsByReferral(referralId)
      assertThat(result).hasSize(1)
      assertThat(result.first().referralId).isEqualTo(referralId)
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
        referralId,
        buildRequest(hour = 3, minute = 15, amPm = "pm"),
        testUser,
      )
      val result = appointmentService.getIcsAppointmentsByReferral(referralId)
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
      val created = appointmentService.createIcsAppointment(referralId, buildRequest(hour = 11, amPm = "am"), testUser)
      val fetched = appointmentService.getIcsAppointment(created.appointmentIcsId)

      assertThat(fetched.appointmentIcsId).isEqualTo(created.appointmentIcsId)
      assertThat(fetched.referralId).isEqualTo(referralId)
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
        referralId,
        buildRequest(sessionCommunication = listOf("Email", "Phone call")),
        testUser,
      )
      val fetched = appointmentService.getIcsAppointment(created.appointmentIcsId)

      assertThat(fetched.sessionCommunications).containsExactly("Email", "Phone call")
    }

    @Test
    fun `should return correct howTakePlace type in response`() {
      val created = appointmentService.createIcsAppointment(
        referralId,
        buildRequest(type = SessionMethodType.PROBATION_OFFICE),
        testUser,
      )
      val fetched = appointmentService.getIcsAppointment(created.appointmentIcsId)

      val sessionMethod = fetched.sessionMethod
      assertThat(sessionMethod).isInstanceOf(InPersonAppointment::class.java)
      assertThat(sessionMethod.type).isEqualTo("IN_PERSON_PROBATION_OFFICE")
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
