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
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.communitysupportapi.authorization.UserMapper
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralDetailsBffResponseDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralInformationDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SubmitReferralResponseDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralEventType
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.model.CreateReferralRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.CommunityServiceProviderRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralProviderAssignmentRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.PersonFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ReferralFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ReferralProviderAssignmentFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ReferralUserFactory
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.PersonAdditionalDetailsFactory

class ReferralControllerIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @Autowired
  private lateinit var personRepository: PersonRepository

  @Autowired
  private lateinit var communityServiceProviderRepository: CommunityServiceProviderRepository

  @Autowired
  private lateinit var referralProviderAssignmentRepository: ReferralProviderAssignmentRepository

  @MockitoBean
  private lateinit var userMapper: UserMapper

  private val testUser = ReferralUserFactory()
    .withHmppsAuthUsername("test-user")
    .create()

  @Nested
  @DisplayName("GET /bff/referral-details/{referralId}")
  inner class ReferralEndPoint {

    @BeforeEach
    fun setup() {
      testDataCleaner.cleanAllTables()
    }

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri("/bff/referral-details/bc852b9d-1997-4ce4-ba7f-cd1759e15d2b")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri("/bff/referral-details/bc852b9d-1997-4ce4-ba7f-cd1759e15d2b")
        .headers(
          setAuthorisation(
            "AUTH_ADM",
            listOf(),
            listOf("read"),
          ),
        )
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri("/bff/referral-details/bc852b9d-1997-4ce4-ba7f-cd1759e15d2b")
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return OK with valid referral information`() {
      // given some referral data are there
      val person = personRepository.save(
        PersonFactory()
          .withFirstName("John")
          .withLastName("Smith")
          .withIdentifier("X123456")
          .withDateOfBirth(LocalDate.of(1980, 1, 1))
          .withGender("Male")
          .withCreatedAt(OffsetDateTime.now())
          .create(),
      )

      val savedReferral = referralRepository.save(
        ReferralFactory()
          .withPersonId(person.id)
          .withCrn("X123456")
          .withReferenceNumber("REF123456")
          .withCreatedAt(OffsetDateTime.now())
          .create(),
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
        .expectBody(ReferralDto::class.java)
        .consumeWith { response ->
          val body = response.responseBody!!
          // compare fields individually and allow a tiny tolerance for createdDate
          body.id shouldBe referralDto.id
          body.crn shouldBe referralDto.crn
          body.referenceNumber shouldBe referralDto.referenceNumber

          val nanosDiff =
            java.time.Duration.between(referralDto.createdDate, body.createdDate).abs().toNanos()
          // allow up to 1-millisecond difference to avoid nanosecond serialization jitter
          assertThat(nanosDiff).isLessThanOrEqualTo(1_000_000L)
        }
    }

    @Test
    fun `should return Not Found with invalid referral identifier`() {
      webTestClient.get()
        .uri("/bff/referrals/bc852b9d-1997-4ce4-ba7f-cd1759e15d2b")
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
    }

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.post()
        .uri("/bff/referral")
        .bodyValue(setUpData())
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.post()
        .uri("/bff/referral")
        .headers(
          setAuthorisation(
            "AUTH_ADM",
            listOf(),
            listOf("read"),
          ),
        )
        .bodyValue(setUpData())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.post()
        .uri("/bff/referral")
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .bodyValue(setUpData())
        .exchange()
        .expectStatus()
        .isForbidden
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
        .expectBody(ReferralInformationDto::class.java)
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
              sex = person.additionalDetails?.sexualOrientation,
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
            personId = "bc852b9d-1997-4ce4-ba7f-cd1759e15d2b".let { UUID.fromString(it) },
            communityServiceProviderId = "bc852b9d-1997-4ce4-ba7f-cd1759e15d2b".let { UUID.fromString(it) },
            crn = "X123456",
          ),
        )
        .exchange()
        .expectStatus()
        .isNotFound
    }

    private fun setUpData(): CreateReferralRequest {
      val person = personRepository.save(
        PersonFactory()
          .withFirstName("John")
          .withLastName("Smith")
          .withIdentifier("X123456")
          .create(),
      )
      val communityServiceProvider =
        communityServiceProviderRepository.findById(UUID.fromString("bc852b9d-1997-4ce4-ba7f-cd1759e15d2b"))
          .get()

      return CreateReferralRequest(
        personId = person.id,
        communityServiceProviderId = communityServiceProvider.id,
        crn = "X123456",
      )
    }
  }

  // kotlin
  @Nested
  @DisplayName("POST /bff/{referralId}/submit-a-referral")
  inner class SubmitReferral {

    @BeforeEach
    fun setup() {
      testDataCleaner.cleanAllTables()
    }

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.post()
        .uri("/bff/${UUID.randomUUID()}/submit-a-referral")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.post()
        .uri("/bff/${UUID.randomUUID()}/submit-a-referral")
        .headers(
          setAuthorisation(
            "AUTH_ADM",
            listOf(),
            listOf("read"),
          ),
        )
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.post()
        .uri("/bff/${UUID.randomUUID()}/submit-a-referral")
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
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
      val communityServiceProvider =
        communityServiceProviderRepository.findById(UUID.fromString("bc852b9d-1997-4ce4-ba7f-cd1759e15d2b"))
          .get()

      val referral = ReferralFactory()
        .withPersonId(person.id)
        .withCrn("X123456")
        .create()

      val savedReferral = referralRepository.save(referral)

      // Create the provider assignment
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
        .expectBody(SubmitReferralResponseDto::class.java)
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
      webTestClient.get()
        .uri("/bff/referral-details-page/bc852b9d-1997-4ce4-ba7f-cd1759e15d2b")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri("/bff/referral-details-page/bc852b9d-1997-4ce4-ba7f-cd1759e15d2b")
        .headers(
          setAuthorisation(
            "AUTH_ADM",
            listOf(),
            listOf("read"),
          ),
        )
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri("/bff/referral-details-page/bc852b9d-1997-4ce4-ba7f-cd1759e15d2b")
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return OK with valid referral details page information`() {
      // given some referral data are there
      val person = personRepository.save(
        PersonFactory()
          .withFirstName("John")
          .withLastName("Smith")
          .withIdentifier("X123456")
          .withDateOfBirth(LocalDate.of(1980, 1, 1))
          .withGender("Male")
          .create(),
      )

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


      val savedReferral = referralRepository.save(
        ReferralFactory()
          .withPersonId(person.id)
          .withCrn("X123456")
          .withReferenceNumber("REF123456")
          .create(),
      )

      val personDetailsTable = ReferralDetailsBffResponseDto.PersonDetailsTableDataDto(
        name = "${person.firstName} ${person.lastName}",
        CRN = savedReferral.crn,
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
        phoneNumber = person.additionalDetails?.phoneNumber.toString(),
        mobileNumber = "",
        email = person.additionalDetails?.emailAddress.toString(),
        address = person.additionalDetails?.address.toString(),
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
        .expectBody(ReferralDetailsBffResponseDto::class.java)
        .consumeWith { response ->
          val body = response.responseBody!!
          // compare fields individually and allow a tiny tolerance for createdDate
          body.id shouldBe referralDetailsDto.id
          body.referenceNumber shouldBe referralDetailsDto.referenceNumber
          body.personDetailsTableData shouldBe referralDetailsDto.personDetailsTableData
          body.equalityDetailsTableData shouldBe referralDetailsDto.equalityDetailsTableData
          body.contactDetailsTableData shouldBe referralDetailsDto.contactDetailsTableData

          val nanosDiff =
            java.time.Duration.between(referralDetailsDto.createdDate, body.createdDate).abs().toNanos()
          // allow up to 1-millisecond difference to avoid nanosecond serialization jitter
          assertThat(nanosDiff).isLessThanOrEqualTo(1_000_000L)
        }
    }

    @Test
    fun `should return Not Found with invalid referral identifier`() {
      webTestClient.get()
        .uri("/bff/referral-details-page/bc852b9d-1997-4ce4-ba7f-cd1759e15d2b")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isNotFound
    }
  }
}
