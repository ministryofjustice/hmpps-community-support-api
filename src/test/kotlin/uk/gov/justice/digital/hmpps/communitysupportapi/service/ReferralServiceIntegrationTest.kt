package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.PersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActorType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentStatusHistoryType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralEventType
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.AppointmentTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.ReferralTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.model.CreateReferralRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonAdditionalDetails
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentDeliveryRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentIcsRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentStatusHistoryRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.CommunityServiceProviderRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralProviderAssignmentRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralUserRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class ReferralServiceIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var communityServiceProviderRepository: CommunityServiceProviderRepository

  @Autowired
  private lateinit var referralProviderAssignmentRepository: ReferralProviderAssignmentRepository

  @Autowired
  private lateinit var personRepository: PersonRepository

  @Autowired
  private lateinit var referralService: ReferralService

  @Autowired
  private lateinit var referralUserRepository: ReferralUserRepository

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @Autowired
  private lateinit var appointmentRepository: AppointmentRepository

  @Autowired
  private lateinit var appointmentIcsRepository: AppointmentIcsRepository

  @Autowired
  private lateinit var appointmentDeliveryRepository: AppointmentDeliveryRepository

  @Autowired
  private lateinit var appointmentStatusHistoryRepository: AppointmentStatusHistoryRepository

  @Autowired
  private lateinit var referralAssignmentService: ReferralAssignmentService

  @Autowired
  private lateinit var appointmentHelper: AppointmentTestSupport

  @Autowired
  private lateinit var referralHelper: ReferralTestSupport

  @Test
  fun `createReferral should save referral and referral events`() {
    val referralUser = referralHelper.ensureReferralUser()
    val createReferralRequest = setUpData()

    val result = referralService.createReferral(referralUser.id, createReferralRequest)
    val savedReferral = result.referral

    assertThat(savedReferral.personId).isEqualTo(result.person.id)
    assertThat(savedReferral.crn).isEqualTo(createReferralRequest.crn)
    assertThat(savedReferral.referralEvents.size).isEqualTo(1)
    assertThat(savedReferral.referenceNumber).isNull()

    val providerAssignments = referralProviderAssignmentRepository.findByReferralId(savedReferral.id)
    assertThat(providerAssignments).hasSize(1)
    assertThat(providerAssignments[0].communityServiceProvider.id).isEqualTo(createReferralRequest.communityServiceProviderId)

    val createdEvent = savedReferral.referralEvents.first { it.eventType == ReferralEventType.CREATED }
    assertThat(createdEvent).isNotNull
    assertThat(createdEvent.actorType).isEqualTo(ActorType.AUTH)
    assertThat(createdEvent.actorId).isEqualTo(referralUser.id)
  }

  @Test
  fun `createReferral should update existing person when identifier matches`() {
    val referralUser = referralHelper.ensureReferralUser()

    val existingPerson = referralHelper.createPerson(
      firstName = "Old",
      lastName = "Name",
      dateOfBirth = LocalDate.of(1970, 1, 1),
    )

    val communityServiceProvider = referralHelper.getCommunityServiceProvider()

    val updatedPersonDto = PersonDto(
      id = UUID.randomUUID(),
      personIdentifier = "X123456",
      firstName = "John",
      lastName = "Smith",
      dateOfBirth = LocalDate.of(1980, 1, 1),
      sex = "Male",
      additionalDetails = null,
    )

    val request = CreateReferralRequest(
      personDetails = updatedPersonDto,
      communityServiceProviderId = communityServiceProvider.id,
      crn = "X123456",
    )

    val result = referralService.createReferral(referralUser.id, request)

    val persistedPerson = personRepository.findById(existingPerson.id).get()

    assertThat(result.referral.personId).isEqualTo(existingPerson.id)
    assertThat(persistedPerson.firstName).isEqualTo(updatedPersonDto.firstName)
    assertThat(persistedPerson.lastName).isEqualTo(updatedPersonDto.lastName)
    assertThat(persistedPerson.dateOfBirth).isEqualTo(updatedPersonDto.dateOfBirth)
  }

  @Test
  fun `createReferral should update existing person additional details when identifier matches`() {
    val referralUser = referralHelper.ensureReferralUser()

    val existingPerson = referralHelper.createPerson(
      firstName = "Old",
      lastName = "Name",
      identifier = "X999999",
      dateOfBirth = LocalDate.of(1975, 5, 5),
    )

    val existingDetails = referralHelper.createPersonAdditionalDetails(existingPerson)

    existingPerson.additionalDetails = existingDetails

    personRepository.save(existingPerson)

    val communityServiceProvider = referralHelper.getCommunityServiceProvider()

    val updatedPersonDto = PersonDto(
      id = UUID.randomUUID(),
      personIdentifier = "X999999",
      firstName = "NewFirst",
      lastName = "NewLast",
      dateOfBirth = LocalDate.of(1985, 6, 6),
      sex = "Male",
      additionalDetails = PersonAdditionalDetails(
        ethnicity = "NewEthnicity",
        preferredLanguage = "NewLang",
        sexualOrientation = "NewOrientation",
      ),
    )

    val request = CreateReferralRequest(
      personDetails = updatedPersonDto,
      communityServiceProviderId = communityServiceProvider.id,
      crn = "X999999",
    )

    referralService.createReferral(referralUser.id, request)

    val persistedPerson = personRepository.findById(existingPerson.id).get()

    assertThat(persistedPerson.firstName).isEqualTo(updatedPersonDto.firstName)
    assertThat(persistedPerson.lastName).isEqualTo(updatedPersonDto.lastName)
    assertThat(persistedPerson.dateOfBirth).isEqualTo(updatedPersonDto.dateOfBirth)
    assertThat(persistedPerson.additionalDetails?.ethnicity).isEqualTo("NewEthnicity")
    assertThat(persistedPerson.additionalDetails?.preferredLanguage).isEqualTo("NewLang")
    assertThat(persistedPerson.additionalDetails?.sexualOrientation).isEqualTo("NewOrientation")
    assertThat(persistedPerson.additionalDetails?.id).isEqualTo(existingDetails.id)
  }

  @Test
  fun `getReferralProgress should throw NotFoundException when referral does not exist`() {
    val nonExistentReferralId = UUID.randomUUID()

    assertThrows(NotFoundException::class.java) {
      referralService.getReferralProgress(nonExistentReferralId)
    }
  }

  @Test
  fun `getReferralProgress should return dto with an empty appointments list when referral has no appointments`() {
    val person = referralHelper.createPerson()
    val referralUser = referralHelper.ensureReferralUser()
    val referral = referralHelper.createReferral(person, submittedBy = referralUser)

    val result = referralService.getReferralProgress(referral.id)

    assertEquals(referral.id, result.referralId)
    assertEquals(person.firstName + " " + person.lastName, result.fullName)
    assertTrue(result.appointments.isEmpty())
  }

  @Test
  fun `getReferralProgress should throw an IllegalStateException when ICS missing from appointments`() {
    val person = referralHelper.createPerson()
    val referralUser = referralHelper.ensureReferralUser()
    val referral = referralHelper.createReferral(person, submittedBy = referralUser)

    appointmentHelper.createAppointment(referral)

    assertThrows(IllegalStateException::class.java) {
      referralService.getReferralProgress(referral.id)
    }
  }

  @Test
  fun `view referral detail page bff should return referral details with uuid`() {
    val referralUser = referralHelper.ensureReferralUser()
    val person = referralHelper.createPerson()
    val referral = referralHelper.createReferral(person, submittedBy = referralUser)

    referralHelper.assignCaseWorkers(referral, listOf(referralUser))

    val result = referralService.getReferralDetailsPage(referral.id.toString())

    assertEquals(referral.id, result.id)
    assertEquals(referral.crn, result.personDetailsTableData.crn)
    assertEquals(referralUser.fullName, result.referralDetailsTableData.assignedTo.first())
  }

  @Test
  fun `view referral detail page bff should return referral details with case id`() {
    val referralUser = referralHelper.ensureReferralUser()
    val person = referralHelper.createPerson()
    val referral = referralHelper.createReferral(person, submittedBy = referralUser, referenceNumber = "AA1234BB")

    referralHelper.assignCaseWorkers(referral, listOf(referralUser))

    val result = referralService.getReferralDetailsPage(referral.referenceNumber)

    assertEquals(referral.id, result.id)
    assertEquals(referral.crn, result.personDetailsTableData.crn)
    assertEquals(referralUser.fullName, result.referralDetailsTableData.assignedTo.first())
  }

  @Test
  fun `getReferralProgress should return a list containing a referral progress dto`() {
    val appointmentDateTime = LocalDateTime.of(2026, 3, 4, 15, 30)
    val yesterday = appointmentDateTime.minusDays(1)

    val person = referralHelper.createPerson()
    val referralUser = referralHelper.ensureReferralUser()
    val referral = referralHelper.createReferral(person, submittedBy = referralUser)
    val appointment = appointmentHelper.createAppointment(referral)

    appointmentHelper.createAppointmentStatusHistory(appointment, AppointmentStatusHistoryType.SCHEDULED, yesterday)
    appointmentHelper.createAppointmentStatusHistory(appointment, AppointmentStatusHistoryType.NEEDS_FEEDBACK, appointmentDateTime)

    appointmentHelper.createAppointmentIcs(
      appointment = appointment,
      delivery = appointmentHelper.createAppointmentDelivery(),
      user = referralUser,
      createdAt = yesterday,
      appointmentDateTime = appointmentDateTime,
      communications = listOf("EMAIL", "SMS", "LETTER"),
    )

    val result = referralService.getReferralProgress(referral.id)

    assertEquals(referral.id, result.referralId)
    assertEquals(person.firstName + " " + person.lastName, result.fullName)

    assertEquals(1, result.appointments.size)
    assertEquals(appointment.id, result.appointments[0].appointmentId)
    assertEquals(appointmentDateTime, result.appointments[0].dateTime)
    assertEquals(AppointmentStatusHistoryType.NEEDS_FEEDBACK, result.appointments[0].status)
  }

  private fun setUpData(): CreateReferralRequest {
    val communityServiceProvider = referralHelper.getCommunityServiceProvider()

    val personDto = PersonDto(
      id = UUID.randomUUID(),
      personIdentifier = "X123456",
      firstName = "John",
      lastName = "Smith",
      dateOfBirth = LocalDate.of(1980, 1, 1),
      sex = "Male",
      additionalDetails = null,
    )

    return CreateReferralRequest(
      personDetails = personDto,
      communityServiceProviderId = communityServiceProvider.id,
      crn = "X123456",
    )
  }
}
