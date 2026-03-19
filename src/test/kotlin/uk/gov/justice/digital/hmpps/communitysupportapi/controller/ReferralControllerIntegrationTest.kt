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
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.communitysupportapi.authorization.UserMapper
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.PersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralDetailsBffResponseDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralInformationDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralProgressDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SubmitReferralResponseDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentStatusHistoryType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralEventType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.AppointmentTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.ReferralTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.model.CreateReferralRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentDeliveryRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentIcsRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentStatusHistoryRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.CommunityServiceProviderRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralProviderAssignmentRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralUserRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.PersonAdditionalDetailsFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.PersonFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ReferralFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ReferralProviderAssignmentFactory
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class ReferralControllerIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @Autowired
  private lateinit var personRepository: PersonRepository

  @Autowired
  private lateinit var communityServiceProviderRepository: CommunityServiceProviderRepository

  @Autowired
  private lateinit var referralProviderAssignmentRepository: ReferralProviderAssignmentRepository

  @Autowired
  private lateinit var referralUserRepository: ReferralUserRepository

  @Autowired
  private lateinit var appointmentRepository: AppointmentRepository

  @Autowired
  private lateinit var appointmentIcsRepository: AppointmentIcsRepository

  @Autowired
  private lateinit var appointmentDeliveryRepository: AppointmentDeliveryRepository

  @Autowired
  private lateinit var appointmentStatusHistoryRepository: AppointmentStatusHistoryRepository

  @Autowired
  private lateinit var appointmentHelper: AppointmentTestSupport

  @Autowired
  private lateinit var referralHelper: ReferralTestSupport

  @MockitoBean
  private lateinit var userMapper: UserMapper

  private lateinit var testUser: ReferralUser

  @Nested
  @DisplayName("GET /bff/referral-details/{referralId}")
  inner class ReferralEndPoint {

    @BeforeEach
    fun setup() {
      testDataCleaner.cleanAllTables()
      testUser = referralHelper.ensureReferralUser()
    }

    @Test
    fun `should return unauthorized if no token`() {
      assertUnauthorized(HttpMethod.GET, "/bff/referral-details/${referralHelper.communityServiceProviderId}")
    }

    @Test
    fun `should return forbidden if no role`() {
      assertForbiddenNoRole(HttpMethod.GET, "/bff/referral-details/${referralHelper.communityServiceProviderId}")
    }

    @Test
    fun `should return forbidden if wrong role`() {
      assertForbiddenWrongRole(HttpMethod.GET, "/bff/referral-details/${referralHelper.communityServiceProviderId}")
    }

    @Test
    fun `should return OK with valid referral information`() {
      val person = referralHelper.createPerson()
      val savedReferral = referralHelper.createReferral(
        person,
        referenceNumber = "REF123456",
        submittedBy = testUser,
      )

      val referralDto = ReferralDto(
        id = savedReferral.id,
        crn = savedReferral.crn,
        referenceNumber = savedReferral.referenceNumber,
        createdDate = savedReferral.createdAt,
      )

      webTestClient.get()
        .uri("/bff/referral-details/${savedReferral.id}")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isOk
        .expectBody<ReferralDto>()
        .consumeWith { response ->
          val body = response.responseBody!!
          // compare fields individually and allow a tiny tolerance for createdDate
          body.id shouldBe referralDto.id
          body.crn shouldBe referralDto.crn
          body.referenceNumber shouldBe referralDto.referenceNumber

          val nanosDiff =
            Duration.between(referralDto.createdDate, body.createdDate).abs().toNanos()
          // allow up to 1-millisecond difference to avoid nanosecond serialization jitter
          assertThat(nanosDiff).isLessThanOrEqualTo(1_000_000L)
        }
    }

    @Test
    fun `should return Not Found with invalid referral identifier`() {
      webTestClient.get()
        .uri("/bff/referrals/${referralHelper.communityServiceProviderId}")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isNotFound
    }
  }

  @Nested
  @DisplayName("POST /bff/referral")
  inner class CreateReferrals {

    @BeforeEach
    fun setup() {
      testDataCleaner.cleanAllTables()
      testUser = referralHelper.ensureReferralUser()
    }

    @Test
    fun `should return unauthorized if no token`() {
      assertUnauthorized(HttpMethod.POST, "/bff/referral")
    }

    @Test
    fun `should return forbidden if no role`() {
      assertForbiddenNoRole(HttpMethod.POST, "/bff/referral", setUpData())
    }

    @Test
    fun `should return forbidden if wrong role`() {
      assertForbiddenWrongRole(HttpMethod.POST, "/bff/referral", setUpData())
    }

    @Test
    fun `should return OK with valid referral information`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)

      webTestClient.post()
        .uri("/bff/referral")
        .headers(setAuthorisation())
        .bodyValue(setUpData())
        .exchange()
        .expectStatus()
        .isOk
        .expectBody<ReferralInformationDto>()
        .consumeWith { response ->
          run {
            val referral = referralRepository.findAll().firstOrNull()!!
            val person = personRepository.findById(referral.personId).get()
            val providerAssignment = referralProviderAssignmentRepository.findByReferralId(referral.id).first()
            val communityServiceProvider = providerAssignment.communityServiceProvider

            val referralInfo = ReferralInformationDto(
              referralId = referral.id,
              personId = referral.personId,
              firstName = person.firstName,
              lastName = person.lastName,
              sex = person.gender,
              crn = referral.crn,
              communityServiceProviderId = communityServiceProvider.id,
              communityServiceProviderName = communityServiceProvider.name,
              region = communityServiceProvider.contractArea.region.name,
              deliveryPartner = communityServiceProvider.serviceProvider.name,
            )
            response.responseBody shouldBe referralInfo
          }
        }
    }

    @Test
    fun `should return Not Found with invalid referral identifier`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)

      webTestClient.post()
        .uri("/bff/referral")
        .headers(setAuthorisation())
        .bodyValue(
          CreateReferralRequest(
            personDetails = PersonDto(
              id = UUID.randomUUID(),
              personIdentifier = "X123456",
              firstName = "John",
              lastName = "Smith",
              dateOfBirth = LocalDate.of(1980, 1, 1),
              sex = "Male",
              additionalDetails = null,
            ),
            communityServiceProviderId = UUID.randomUUID(),
            crn = "X123456",
          ),
        )
        .exchange()
        .expectStatus().isNotFound
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

  @Nested
  @DisplayName("POST /bff/{referralId}/submit-a-referral")
  inner class SubmitReferral {

    @BeforeEach
    fun setup() {
      testDataCleaner.cleanAllTables()
      testUser = referralHelper.ensureReferralUser()
    }

    @Test
    fun `should return unauthorized if no token`() {
      assertUnauthorized(HttpMethod.POST, "/bff/${UUID.randomUUID()}/submit-a-referral")
    }

    @Test
    fun `should return forbidden if no role`() {
      assertForbiddenNoRole(HttpMethod.POST, "/bff/${UUID.randomUUID()}/submit-a-referral")
    }

    @Test
    fun `should return forbidden if wrong role`() {
      assertForbiddenWrongRole(HttpMethod.POST, "/bff/${UUID.randomUUID()}/submit-a-referral")
    }

    @Test
    fun `should return OK and create submitted event for valid referral`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)

      val person = personRepository.save(
        PersonFactory()
          .withFirstName("John")
          .withLastName("Smith")
          .withIdentifier("X123456")
          .create(),
      )
      val communityServiceProvider = referralHelper.getCommunityServiceProvider()

      val referral = ReferralFactory()
        .withPersonId(person.id)
        .withCrn("X123456")
        .create()

      val savedReferral = referralRepository.save(referral)

      val providerAssignment = ReferralProviderAssignmentFactory()
        .withReferral(savedReferral)
        .withCommunityServiceProvider(communityServiceProvider)
        .create()
      referralProviderAssignmentRepository.save(providerAssignment)

      webTestClient.post()
        .uri("/bff/${savedReferral.id}/submit-a-referral")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isOk
        .expectBody<SubmitReferralResponseDto>()
        .consumeWith { response ->
          run {
            val updatedReferral = referralRepository.findById(savedReferral.id).get()
            val submitReferralResponseDto = SubmitReferralResponseDto(
              referralId = updatedReferral.id,
              referenceNumber = updatedReferral.referenceNumber,
            )
            response.responseBody shouldBe submitReferralResponseDto
          }

          val updated = referralRepository.findById(savedReferral.id).get()
          assertThat(updated.submittedEvent?.eventType).isEqualTo(ReferralEventType.SUBMITTED)
        }
    }
  }

  @Nested
  @DisplayName("GET /bff/referral-details-page/{referralId}")
  inner class ReferralViewPageEndPoint {

    @BeforeEach
    fun setup() {
      testDataCleaner.cleanAllTables()
    }

    @Test
    fun `should return unauthorized if no token`() {
      assertUnauthorized(HttpMethod.GET, "/bff/referral-details-page/${referralHelper.communityServiceProviderId}")
    }

    @Test
    fun `should return forbidden if no role`() {
      assertForbiddenNoRole(HttpMethod.GET, "/bff/referral-details-page/${referralHelper.communityServiceProviderId}")
    }

    @Test
    fun `should return forbidden if wrong role`() {
      assertForbiddenWrongRole(HttpMethod.GET, "/bff/referral-details-page/${referralHelper.communityServiceProviderId}")
    }

    @Test
    fun `should return OK with valid referral details page information`() {
      val testUser = referralHelper.createTestUser()
      val person = referralHelper.createPerson(identifier = "CRN12345")

      val additionalDetails = PersonAdditionalDetailsFactory()
        .withPerson(person)
        .withEthnicity("White")
        .withPreferredLanguage("English")
        .withNeurodiverseConditions("None")
        .withReligionOrBelief("None")
        .withTransgender("No")
        .withSexualOrientation("Straight")
        .withAddress("123 Test Street /n Test Town /n Testshire")
        .withPhoneNumber("0191 234 5678")
        .withEmailAddress("test@test.com")
        .create()

      person.additionalDetails = additionalDetails
      personRepository.save(person)

      val savedReferral = referralHelper.createReferral(person = person, referenceNumber = "REF123456", submittedBy = testUser)

      val personDetailsTable = ReferralDetailsBffResponseDto.PersonDetailsTableDataDto(
        name = "${person.firstName} ${person.lastName}",
        crn = savedReferral.crn,
        dateOfBirth = person.dateOfBirth.toString(),
        preferredLanguage = person.additionalDetails?.preferredLanguage.toString(),
        disabilities = "",
      )

      val equalityDetailsTable = ReferralDetailsBffResponseDto.EqualityDetailsTableDataDto(
        ethnicity = person.additionalDetails?.ethnicity.toString(),
        religionOrBelief = person.additionalDetails?.religionOrBelief.toString(),
        sex = "",
        genderIdentity = person.gender,
        sexualOrientation = person.additionalDetails?.sexualOrientation.toString(),
        transgender = person.additionalDetails?.transgender.toString(),
      )

      val contactDetailsTable = ReferralDetailsBffResponseDto.ContactDetailsTableDataDto(
        phoneNumber = person.additionalDetails?.phoneNumber,
        mobileNumber = null,
        email = person.additionalDetails?.emailAddress,
        address = person.additionalDetails?.address,
      )

      val referralDetailsTable = ReferralDetailsBffResponseDto.ReferralDetailsTableDataDto(
        referralDate = savedReferral.createdAt.toString(),
        assignedTo = emptyList(),
      )

      val referralDetailsDto = ReferralDetailsBffResponseDto(
        id = savedReferral.id,
        referenceNumber = savedReferral.referenceNumber,
        createdDate = savedReferral.createdAt,
        personDetailsTableData = personDetailsTable,
        equalityDetailsTableData = equalityDetailsTable,
        contactDetailsTableData = contactDetailsTable,
        referralDetailsTableData = referralDetailsTable,
      )

      webTestClient.get()
        .uri("/bff/referral-details-page/${savedReferral.id}")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isOk
        .expectBody<ReferralDetailsBffResponseDto>()
        .consumeWith { response ->
          val body = response.responseBody!!
          // compare fields individually and allow a tiny tolerance for createdDate
          body.id shouldBe referralDetailsDto.id
          body.referenceNumber shouldBe referralDetailsDto.referenceNumber
          body.personDetailsTableData shouldBe referralDetailsDto.personDetailsTableData
          body.equalityDetailsTableData shouldBe referralDetailsDto.equalityDetailsTableData
          body.contactDetailsTableData shouldBe referralDetailsDto.contactDetailsTableData

          val nanosDiff =
            Duration.between(referralDetailsDto.createdDate, body.createdDate).abs().toNanos()
          // allow up to 1-millisecond difference to avoid nanosecond serialization jitter
          assertThat(nanosDiff).isLessThanOrEqualTo(1_000_000L)
        }
    }

    @Test
    fun `should return Not Found with invalid referral identifier`() {
      webTestClient.get()
        .uri("/bff/referral-details-page/${referralHelper.communityServiceProviderId}")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isNotFound
    }
  }

  @Nested
  @DisplayName("GET /bff/referral-details/{referralId}/progress")
  inner class ReferralProgressPageEndPoint {
    val referralId = UUID.randomUUID().toString()

    @BeforeEach
    fun setup() {
      testDataCleaner.cleanAllTables()
      testUser = referralHelper.ensureReferralUser()
    }

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri("/bff/referral-details/$referralId/progress")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri("/bff/referral-details/$referralId/progress")
        .headers(setAuthorisation("AUTH_ADM", listOf(), listOf("read")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri("/bff/referral-details/$referralId/progress")
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return 404 when referral does not exist`() {
      webTestClient.get()
        .uri("/bff/referral-details/$referralId/progress")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `should return an empty list when no appointments exist for referral`() {
      val person = referralHelper.createPerson()
      val referralUser = referralHelper.ensureReferralUser()
      val referral = referralHelper.createReferral(person, submittedBy = referralUser)

      webTestClient.get()
        .uri("/bff/referral-details/${referral.id}/progress")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isOk
        .expectBody()
        .json("[]")
    }

    @Test
    fun `should returns 500 when an ICS is missing from referral progress`() {
      val person = referralHelper.createPerson()
      val referralUser = referralHelper.ensureReferralUser()
      val referral = referralHelper.createReferral(person, submittedBy = referralUser)

      appointmentHelper.createAppointment(referral)

      webTestClient.get()
        .uri("/bff/referral-details/${referral.id}/progress")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().is5xxServerError
    }

    @Test
    fun `should return list containing referral progress Dto`() {
      val appointmentDateTime = LocalDateTime.of(2026, 3, 4, 15, 30)
      val oneWeekAgo = appointmentDateTime.minusWeeks(1)

      val person = referralHelper.createPerson()
      val referralUser = referralHelper.ensureReferralUser()
      val referral = referralHelper.createReferral(person, submittedBy = referralUser)
      val appointment = appointmentHelper.createAppointment(referral)

      appointmentHelper.createAppointmentStatusHistory(
        appointment,
        AppointmentStatusHistoryType.SCHEDULED,
        oneWeekAgo,
      )
      appointmentHelper.createAppointmentStatusHistory(
        appointment,
        AppointmentStatusHistoryType.NEEDS_FEEDBACK,
        appointmentDateTime,
      )

      appointmentHelper.createAppointmentIcs(
        appointment,
        delivery = appointmentHelper.createAppointmentDelivery(),
        user = referralUser,
        appointmentDateTime = appointmentDateTime,
        createdAt = oneWeekAgo,
        communications = listOf("EMAIL", "SMS", "LETTER"),
      )

      webTestClient.get()
        .uri("/bff/referral-details/${referral.id}/progress")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBody<List<ReferralProgressDto>>()
        .consumeWith { response ->
          val body = response.responseBody!!
          body.size shouldBe 1

          val referralProgressDto = body.first()

          referralProgressDto.referralId shouldBe referral.id
          referralProgressDto.personName shouldBe person.firstName + " " + person.lastName
          referralProgressDto.appointmentId shouldBe appointment.id
          referralProgressDto.appointmentDateTime shouldBe appointmentDateTime
          referralProgressDto.status shouldBe AppointmentStatusHistoryType.NEEDS_FEEDBACK
        }
    }
  }
}
