package uk.gov.justice.digital.hmpps.communitysupportapi.service

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.PersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.WithUpdated
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
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.CRN
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.PRISONER_NUMBER
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.cprPrisonPersonJson
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.cprProbationPersonJson
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ReferralProviderAssignmentFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.util.toFormattedDateOfBirth
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
    assertThat(savedReferral.createdBy).isEqualTo(referralUser.id)

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
      dateOfBirth = LocalDate.of(1980, 1, 1).toFormattedDateOfBirth(),
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
    assertThat(persistedPerson.dateOfBirth).isEqualTo(LocalDate.of(1980, 1, 1))
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
      dateOfBirth = LocalDate.of(1985, 6, 6).toFormattedDateOfBirth(),
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
    assertThat(persistedPerson.dateOfBirth).isEqualTo(LocalDate.of(1985, 6, 6))
    assertThat(persistedPerson.additionalDetails?.ethnicity).isEqualTo("NewEthnicity")
    assertThat(persistedPerson.additionalDetails?.preferredLanguage).isEqualTo("NewLang")
    assertThat(persistedPerson.additionalDetails?.sexualOrientation).isEqualTo("NewOrientation")
    assertThat(persistedPerson.additionalDetails?.id).isEqualTo(existingDetails.id)
  }

  @Test
  fun `createReferral should persist prison numbers on person when provided`() {
    val referralUser = referralHelper.ensureReferralUser()
    val communityServiceProvider = referralHelper.getCommunityServiceProvider()

    val personDto = PersonDto(
      id = UUID.randomUUID(),
      personIdentifier = "A1234BC",
      firstName = "John",
      lastName = "Smith",
      dateOfBirth = LocalDate.of(1980, 1, 1).toFormattedDateOfBirth(),
      sex = "Male",
      prisonNumbers = listOf("A1234BC", "B5678DE"),
      additionalDetails = null,
    )

    val request = CreateReferralRequest(
      personDetails = personDto,
      communityServiceProviderId = communityServiceProvider.id,
      crn = "A1234BC",
    )

    val result = referralService.createReferral(referralUser.id, request)
    val persistedPerson = personRepository.findById(result.person.id).get()

    assertThat(persistedPerson.prisonNumbers).isEqualTo("A1234BC,B5678DE")
  }

  @Test
  fun `createReferral should update prison numbers on existing person`() {
    val referralUser = referralHelper.ensureReferralUser()

    val existingPerson = referralHelper.createPerson(
      firstName = "John",
      lastName = "Smith",
      identifier = "A9999ZZ",
      dateOfBirth = LocalDate.of(1980, 1, 1),
    )

    val communityServiceProvider = referralHelper.getCommunityServiceProvider()

    val updatedPersonDto = PersonDto(
      id = UUID.randomUUID(),
      personIdentifier = "A9999ZZ",
      firstName = "John",
      lastName = "Smith",
      dateOfBirth = LocalDate.of(1980, 1, 1).toFormattedDateOfBirth(),
      sex = "Male",
      prisonNumbers = listOf("A9999ZZ"),
      additionalDetails = null,
    )

    val request = CreateReferralRequest(
      personDetails = updatedPersonDto,
      communityServiceProviderId = communityServiceProvider.id,
      crn = "A9999ZZ",
    )

    referralService.createReferral(referralUser.id, request)

    val persistedPerson = personRepository.findById(existingPerson.id).get()
    assertThat(persistedPerson.prisonNumbers).isEqualTo("A9999ZZ")
  }

  @Test
  fun `getReferralProgress should throw NotFoundException when referral does not exist`() {
    val nonExistentReferralId = UUID.randomUUID()

    assertThrows(NotFoundException::class.java) {
      referralService.getReferralProgress(nonExistentReferralId.toString())
    }
  }

  @Test
  fun `getReferralProgress should return dto with an empty appointments list when referral has no appointments`() {
    val person = referralHelper.createPerson()
    val referralUser = referralHelper.ensureReferralUser()
    val referral = referralHelper.createReferral(person, submittedBy = referralUser)

    val result = referralService.getReferralProgress(referral.id.toString())

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
      referralService.getReferralProgress(referral.id.toString())
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
    assertEquals(referralUser.fullName, result.referralDetailsTableData.assignedTo.first().fullName)
    assertEquals(referralUser.hmppsAuthUsername, result.referralDetailsTableData.assignedTo.first().emailAddress)
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
    assertEquals(referralUser.fullName, result.referralDetailsTableData.assignedTo.first().fullName)
    assertEquals(referralUser.hmppsAuthUsername, result.referralDetailsTableData.assignedTo.first().emailAddress)
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
    appointmentHelper.createAppointmentStatusHistory(appointment, AppointmentStatusHistoryType.COMPLETED, appointmentDateTime.plusDays(1))

    val ics = appointmentHelper.createAppointmentIcs(
      appointment = appointment,
      delivery = appointmentHelper.createAppointmentDelivery(),
      user = referralUser,
      createdAt = yesterday,
      appointmentDateTime = appointmentDateTime,
      communications = listOf("EMAIL", "SMS", "LETTER"),
    )

    val icsFeedback = appointmentHelper.createIcsFeedback(ics = ics, createdBy = referralUser)

    val result = referralService.getReferralProgress(referral.id.toString())

    assertEquals(referral.id, result.referralId)
    assertEquals(person.firstName + " " + person.lastName, result.fullName)

    assertEquals(1, result.appointments.size)
    assertEquals(ics.id, result.appointments[0].appointmentIcsId)
    assertEquals(appointmentDateTime, result.appointments[0].dateTime)
    assertEquals(AppointmentStatusHistoryType.COMPLETED, result.appointments[0].status)
    assertEquals(icsFeedback.id, result.appointments[0].icsFeedbackId)
  }

  @Test
  fun `referral information bff should return referral information`() {
    val referralUser = referralHelper.ensureReferralUser()
    val person = referralHelper.createPerson()
    val communityServiceProvider = referralHelper.getCommunityServiceProvider()
    val referral = referralHelper.createReferral(person, submittedBy = referralUser)

    val providerAssignment = ReferralProviderAssignmentFactory()
      .withReferral(referral)
      .withCommunityServiceProvider(communityServiceProvider)
      .create()
    referralProviderAssignmentRepository.save(providerAssignment)

    val referralInformation = referralService.getReferralInformation(referral.id.toString())

    assertEquals(referral.id, referralInformation.referralId)
    assertEquals(referral.crn, referralInformation.crn)
    assertEquals(person.firstName, referralInformation.firstName)
    assertEquals(person.lastName, referralInformation.lastName)
    assertEquals(communityServiceProvider.id, referralInformation.communityServiceProviderId)
  }

  @Test
  fun `Get person details using CRN identifier`() {
    val person = referralHelper.createPerson(identifier = CRN)

    stubFor(
      get(urlPathEqualTo("/person/probation/$CRN"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(cprProbationPersonJson(person.identifier)),
        ),
    )

    val result = referralService.getPersonDetails(person.identifier)
    assertEquals(person.id, result.id)

    assertEquals(CRN, result.personalDetails.personIdentifier)
    assertEquals("Mr", result.personalDetails.title)
    assertEquals("John", result.personalDetails.firstName)
    assertEquals("David", result.personalDetails.middleNames)
    assertEquals("Smith", result.personalDetails.lastName)
    assertEquals("1985-01-01", result.personalDetails.dateOfBirth.toString())
    assertEquals(emptyList<String>(), result.personalDetails.prisonNumbers)
    assertNull(result.personalDetails.preferredLanguage)
    assertEquals(WithUpdated("", LocalDate.EPOCH), result.personalDetails.currentCircumstances)
    assertEquals(WithUpdated(emptyList<String>(), LocalDate.EPOCH), result.personalDetails.disabilities)

    assertEquals("Male", result.equalityMonitoring.sex)
    assertEquals("White", result.equalityMonitoring.ethnicity)
    assertNull(result.equalityMonitoring.neurodiverseConditions)
    assertEquals("Christian", result.equalityMonitoring.religionOrBelief)
    assertNull(result.equalityMonitoring.transgender)
    assertEquals("Heterosexual", result.equalityMonitoring.sexualOrientation)
    assertEquals("Male", result.equalityMonitoring.genderIdentity)
    assertEquals(listOf("Argentine", "Brazilian"), result.equalityMonitoring.nationalities)
    assertNull(result.equalityMonitoring.interestToImmigration)
    assertEquals(true, result.equalityMonitoring.disability)

    assertEquals("1, Test Street, Testville, TE1 1ST", result.contactDetails.address.address)
    assertEquals("Friends/Family (settled) (verified)", result.contactDetails.address.addressType)
    assertEquals(false, result.contactDetails.address.addressTypeVerified)
    assertEquals("2005-12-01", result.contactDetails.address.addressStartDate.toString())
    assertEquals("No notes", result.contactDetails.address.addressNotes)
    assertEquals("01234567890", result.contactDetails.phoneNumber)
    assertEquals("07700900002", result.contactDetails.mobileNumber)
    assertEquals("john.smith@example.com", result.contactDetails.emailAddress)
  }

  @Test
  fun `Get person details using prison number`() {
    val person = referralHelper.createPerson(identifier = PRISONER_NUMBER)

    stubFor(
      get(urlPathEqualTo("/person/prison/$PRISONER_NUMBER"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(cprPrisonPersonJson(person.identifier)),
        ),
    )

    val result = referralService.getPersonDetails(person.identifier)
    assertEquals(person.id, result.id)

    assertEquals(PRISONER_NUMBER, result.personalDetails.personIdentifier)
    assertEquals("Mr", result.personalDetails.title)
    assertEquals("John", result.personalDetails.firstName)
    assertEquals("James", result.personalDetails.middleNames)
    assertEquals("Smith", result.personalDetails.lastName)
    assertEquals("1985-01-01", result.personalDetails.dateOfBirth.toString())
    assertEquals(listOf(PRISONER_NUMBER), result.personalDetails.prisonNumbers)
    assertNull(result.personalDetails.preferredLanguage)
    assertEquals(WithUpdated("", LocalDate.EPOCH), result.personalDetails.currentCircumstances)
    assertEquals(WithUpdated(emptyList<String>(), LocalDate.EPOCH), result.personalDetails.disabilities)

    assertEquals("Male", result.equalityMonitoring.sex)
    assertEquals("White", result.equalityMonitoring.ethnicity)
    assertNull(result.equalityMonitoring.neurodiverseConditions)
    assertEquals("Christian", result.equalityMonitoring.religionOrBelief)
    assertNull(result.equalityMonitoring.transgender)
    assertEquals("Heterosexual", result.equalityMonitoring.sexualOrientation)
    assertEquals("Male", result.equalityMonitoring.genderIdentity)
    assertEquals(listOf("British"), result.equalityMonitoring.nationalities)
    assertNull(result.equalityMonitoring.interestToImmigration)
    assertEquals(false, result.equalityMonitoring.disability)

    assertEquals("10, Prison Road, Leeds, LS1 1AA", result.contactDetails.address.address)
    assertEquals("Home", result.contactDetails.address.addressType)
    assertEquals(false, result.contactDetails.address.addressTypeVerified)
    assertEquals("2020-04-03", result.contactDetails.address.addressStartDate.toString())
    assertNull(result.contactDetails.address.addressNotes)
    assertEquals("01234567890", result.contactDetails.phoneNumber)
    assertEquals("07700900002", result.contactDetails.mobileNumber)
    assertEquals("john.smith@example.com", result.contactDetails.emailAddress)
  }

  private fun setUpData(): CreateReferralRequest {
    val communityServiceProvider = referralHelper.getCommunityServiceProvider()

    val personDto = PersonDto(
      id = UUID.randomUUID(),
      personIdentifier = "X123456",
      firstName = "John",
      lastName = "Smith",
      dateOfBirth = LocalDate.of(1980, 1, 1).toFormattedDateOfBirth(),
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
