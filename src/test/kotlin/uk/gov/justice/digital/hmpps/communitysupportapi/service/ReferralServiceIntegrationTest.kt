package uk.gov.justice.digital.hmpps.communitysupportapi.service

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.cpr.CprIdentifiersDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActorType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentStatusHistoryType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralEventType
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.AppointmentTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.PersonTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.ReferralTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.model.AdditionalSupportNeedsRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.model.CreateReferralRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentDeliveryRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentIcsRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentStatusHistoryRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.CommunityServiceProviderRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonAdditionalSupportNeedsRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralProviderAssignmentRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralUserRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.PRISONER_NUMBER
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.createCprPrisonPersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.createCprProbationPersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ReferralProviderAssignmentFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.util.toJson
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

  @Autowired
  private lateinit var personHelper: PersonTestSupport

  @Autowired
  private lateinit var personAdditionSupportNeedsRepository: PersonAdditionalSupportNeedsRepository

  @Test
  fun `createReferral should save referral and referral events`() {
    val referralUser = referralHelper.ensureReferralUser()
    val createReferralRequest = setUpData()

    val result = referralService.createReferral(referralUser.id, createReferralRequest)
    val savedReferral = result.referral

    assertThat(savedReferral.personId).isEqualTo(result.person.id)
    assertThat(savedReferral.personIdentifier).isEqualTo(createReferralRequest.personIdentifier)
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

    val cprPersonDTO = createCprProbationPersonDto("X123456")
    stubFor(
      get(urlEqualTo("/person/probation/X123456"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(cprPersonDTO.toJson()),
        ),
    )

    val communityServiceProvider = referralHelper.getCommunityServiceProvider()

    val request = CreateReferralRequest(
      communityServiceProviderId = communityServiceProvider.id,
      personIdentifier = "X123456",
    )

    val result = referralService.createReferral(referralUser.id, request)

    val persistedPerson = personRepository.findById(existingPerson.id).get()

    assertThat(result.referral.personId).isEqualTo(existingPerson.id)
    assertThat(persistedPerson.firstName).isEqualTo(cprPersonDTO.firstName)
    assertThat(persistedPerson.lastName).isEqualTo(cprPersonDTO.lastName)
    assertThat(persistedPerson.dateOfBirth).isEqualTo(LocalDate.of(1985, 1, 1))
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

    val cprPersonDTO = createCprProbationPersonDto("X999999")
    stubFor(
      get(urlEqualTo("/person/probation/X999999"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(cprPersonDTO.toJson()),
        ),
    )

    val request = CreateReferralRequest(
      communityServiceProviderId = communityServiceProvider.id,
      personIdentifier = "X999999",
    )

    referralService.createReferral(referralUser.id, request)

    val persistedPerson = personRepository.findById(existingPerson.id).get()

    assertThat(persistedPerson.firstName).isEqualTo(cprPersonDTO.firstName)
    assertThat(persistedPerson.lastName).isEqualTo(cprPersonDTO.lastName)
    assertThat(persistedPerson.dateOfBirth).isEqualTo(LocalDate.of(1985, 1, 1))
    assertThat(persistedPerson.additionalDetails?.ethnicity).isEqualTo("White")
    assertThat(persistedPerson.additionalDetails?.preferredLanguage).isNull()
    assertThat(persistedPerson.additionalDetails?.sexualOrientation).isEqualTo("Heterosexual")
    assertThat(persistedPerson.additionalDetails?.id).isEqualTo(existingDetails.id)
  }

  @Test
  fun `createReferral should persist prison numbers on person when provided`() {
    val cprPersonDTO = createCprPrisonPersonDto(listOf("A1234BC", "B5678DE"))
    stubFor(
      get(urlEqualTo("/person/prison/A1234BC"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(cprPersonDTO.toJson()),
        ),
    )
    val referralUser = referralHelper.ensureReferralUser()
    val communityServiceProvider = referralHelper.getCommunityServiceProvider()

    val request = CreateReferralRequest(
      communityServiceProviderId = communityServiceProvider.id,
      personIdentifier = "A1234BC",
    )

    val result = referralService.createReferral(referralUser.id, request)
    val persistedPerson = personRepository.findById(result.person.id).get()

    assertThat(persistedPerson.prisonNumbers).isEqualTo("A1234BC,B5678DE")
  }

  @Test
  fun `createReferral should update prison numbers on existing person`() {
    val cprPersonDTO = createCprPrisonPersonDto(PRISONER_NUMBER)
    stubFor(
      get(urlEqualTo("/person/prison/$PRISONER_NUMBER"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(cprPersonDTO.toJson()),
        ),
    )

    val referralUser = referralHelper.ensureReferralUser()

    val existingPerson = referralHelper.createPerson(
      firstName = "John",
      lastName = "Smith",
      identifier = PRISONER_NUMBER,
      dateOfBirth = LocalDate.of(1980, 1, 1),
    )

    val communityServiceProvider = referralHelper.getCommunityServiceProvider()

    val request = CreateReferralRequest(
      communityServiceProviderId = communityServiceProvider.id,
      personIdentifier = PRISONER_NUMBER,
    )

    referralService.createReferral(referralUser.id, request)

    val persistedPerson = personRepository.findById(existingPerson.id).get()
    assertThat(persistedPerson.prisonNumbers).isEqualTo(PRISONER_NUMBER)
  }

  @Test
  fun `submitReferral should create a referral and referral number`() {
    val referralUser = referralHelper.ensureReferralUser()
    val createReferralRequest = setUpData()

    val result = referralService.createReferral(referralUser.id, createReferralRequest)
    val savedReferral = result.referral
    val submissionResult = referralService.submitReferral(savedReferral.id, referralUser.id)

    assertThat(submissionResult).isNotNull()
    assertThat(savedReferral.id).isEqualTo(submissionResult.referralId)
    assertThat(submissionResult.referenceNumber).isNotNull()
  }

  @Test
  fun `update additional information should be saved`() {
    val referralUser = referralHelper.ensureReferralUser()
    val createReferralRequest = setUpData()

    val result = referralService.createReferral(referralUser.id, createReferralRequest)
    val savedReferral = result.referral

    val supportNeeds = AdditionalSupportNeedsRequest(
      employmentResponsibilities = "Test employment responsibilities",
      caringResponsibilities = "Test caring responsibilities",
      needsAdditionalSupport = true,
    )

    val updatedResult = referralService.upsertAdditionalSupportNeeds(
      savedReferral.id,
      referralUser.id,
      supportNeeds,
    )
    assertThat(updatedResult).isNotNull()

    val savedSupportNeeds = personAdditionSupportNeedsRepository.findByReferralId(savedReferral.id)
    assertThat(savedSupportNeeds).isNotNull()
    assertThat(savedSupportNeeds?.referralId).isEqualTo(savedReferral.id)
    assertThat(savedSupportNeeds?.personId).isEqualTo(savedReferral.personId)
    assertThat(savedSupportNeeds?.caringResponsibilitiesDetails).isEqualTo("Test caring responsibilities")
    assertThat(savedSupportNeeds?.noAdditionalSupportNeeded).isFalse()
    assertThat(savedSupportNeeds?.physicalHealthDetails).isNull()
    assertThat(savedSupportNeeds?.mentalEmotionalHealthDetails).isNull()
    assertThat(savedSupportNeeds?.diversityDetails).isNull()
    assertThat(savedSupportNeeds?.employmentResponsibilitiesDetails).isEqualTo("Test employment responsibilities")
    assertThat(savedSupportNeeds?.locationTravelDetails).isNull()
    assertThat(savedSupportNeeds?.neurodiversityDetails).isNull()
    assertThat(savedSupportNeeds?.anythingElseDetails).isNull()
    assertThat(savedSupportNeeds?.createdBy).isEqualTo(referralUser.id)
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
    assertEquals(referral.personIdentifier, result.personDetailsTableData.crn)
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
    assertEquals(referral.personIdentifier, result.personDetailsTableData.crn)
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
    assertEquals(referral.personIdentifier, referralInformation.personIdentifier)
    assertEquals(person.firstName, referralInformation.firstName)
    assertEquals(person.lastName, referralInformation.lastName)
    assertEquals(communityServiceProvider.id, referralInformation.communityServiceProviderId)
  }

  @Nested
  @DisplayName("getConfirmPersonDetailsBffDto")
  inner class GetConfirmPersonDetailsBffDtoo {
    @Test
    fun `Finding a person with both a CRN and a Prison Number`() {
      val theCrn = personHelper.generateCrn()
      val person = referralHelper.createPerson(identifier = theCrn)

      val cprResponseWithBothIdentifiers = createCprProbationPersonDto(theCrn)
        .copy(
          identifiers = CprIdentifiersDto(
            crns = listOf(theCrn),
            prisonNumbers = listOf("THE_PRISON_NUMBER"),
            pncs = listOf("2012/0052494Q"),
            cros = listOf("123456/24A"),
            nationalInsuranceNumbers = listOf("AA123456A"),
          ),
        ).toJson()

      stubFor(
        get(urlPathEqualTo("/person/probation/$theCrn"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(cprResponseWithBothIdentifiers),
          ),
      )

      val result = referralService.getConfirmPersonDetailsBffDto(theCrn)

      assertEquals(person.id, result.id)
      assertEquals(theCrn, result.personalDetails.crn)
      assertEquals(listOf("THE_PRISON_NUMBER"), result.personalDetails.prisonNumbers)
      assertEquals("John", result.personalDetails.firstName)
      assertEquals("Smith", result.personalDetails.lastName)
    }

    @Test
    fun `should throw NotFoundException when person does not exist in the database`() {
      val unknownCrn = "DEFINITELYDOESNOTEXIST"

      assertThrows(NotFoundException::class.java) {
        referralService.getConfirmPersonDetailsBffDto(unknownCrn)
      }
    }
  }

  private fun setUpData(): CreateReferralRequest {
    val communityServiceProvider = referralHelper.getCommunityServiceProvider()

    return CreateReferralRequest(
      communityServiceProviderId = communityServiceProvider.id,
      personIdentifier = "X123456",
    )
  }
}
