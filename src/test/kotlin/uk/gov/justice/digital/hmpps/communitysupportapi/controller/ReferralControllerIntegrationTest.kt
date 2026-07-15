package uk.gov.justice.digital.hmpps.communitysupportapi.controller

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.PATCH
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.communitysupportapi.authorization.UserMapper
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.AdditionalSupportNeedsBffResponseDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.AppointmentIcsResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ConfirmPersonDetailsBffDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.PersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralDetailsBffResponseDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralInformationDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralProgressDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SubmitReferralResponseDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.TaskListStatusResponseDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.VirtualAppointment
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentDeliveryMethod
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentStatusHistoryType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralEventType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.AppointmentTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
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
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.CRN
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.cprProbationPersonJson
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.PersonAdditionalDetailsFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.PersonAdditionalSupportNeedsFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.PersonFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ReferralProviderAssignmentFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.util.toFormattedDateOfBirth
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
  private lateinit var personAdditionalSupportNeedsRepository: PersonAdditionalSupportNeedsRepository

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
      assertUnauthorized(GET, "/bff/referral-details/${referralHelper.communityServiceProviderId}")
    }

    @Test
    fun `should return forbidden if no role`() {
      assertForbiddenNoRole(GET, "/bff/referral-details/${referralHelper.communityServiceProviderId}")
    }

    @Test
    fun `should return forbidden if wrong role`() {
      assertForbiddenWrongRole(GET, "/bff/referral-details/${referralHelper.communityServiceProviderId}")
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
        crn = savedReferral.personIdentifier,
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
      assertNotFound(GET, "/bff/referrals/${referralHelper.communityServiceProviderId}")
    }
  }

  @Nested
  @DisplayName("POST /referral")
  inner class CreateReferrals {

    @BeforeEach
    fun setup() {
      testDataCleaner.cleanAllTables()
      testUser = referralHelper.ensureReferralUser()
    }

    @Test
    fun `should return unauthorized if no token`() {
      assertUnauthorized(HttpMethod.POST, "/referral")
    }

    @Test
    fun `should return forbidden if no role`() {
      assertForbiddenNoRole(HttpMethod.POST, "/referral", setUpData())
    }

    @Test
    fun `should return forbidden if wrong role`() {
      assertForbiddenWrongRole(HttpMethod.POST, "/referral", setUpData())
    }

    @Test
    fun `should return OK with valid referral information`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)

      webTestClient.post()
        .uri("/referral")
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
              referralDate = referral.createdAt.toLocalDate(),
              personId = referral.personId,
              firstName = person.firstName,
              lastName = person.lastName,
              sex = person.gender,
              personIdentifier = referral.personIdentifier,
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
        .uri("/referral")
        .headers(setAuthorisation())
        .bodyValue(
          CreateReferralRequest(
            personDetails = PersonDto(
              id = UUID.randomUUID(),
              personIdentifier = "X123456",
              firstName = "John",
              lastName = "Smith",
              dateOfBirth = LocalDate.of(1980, 1, 1).toFormattedDateOfBirth(),
              sex = "Male",
              additionalDetails = null,
            ),
            communityServiceProviderId = UUID.randomUUID(),
            personIdentifier = "X123456",
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
        dateOfBirth = LocalDate.of(1980, 1, 1).toFormattedDateOfBirth(),
        sex = "Male",
        additionalDetails = null,
      )

      return CreateReferralRequest(
        personDetails = personDto,
        communityServiceProviderId = communityServiceProvider.id,
        personIdentifier = "X123456",
      )
    }
  }

  @Nested
  @DisplayName("POST /{referralId}/submit-a-referral")
  inner class SubmitReferral {

    @BeforeEach
    fun setup() {
      testDataCleaner.cleanAllTables()
      testUser = referralHelper.ensureReferralUser()
    }

    @Test
    fun `should return unauthorized if no token`() {
      assertUnauthorized(HttpMethod.POST, "/${UUID.randomUUID()}/submit-a-referral")
    }

    @Test
    fun `should return forbidden if no role`() {
      assertForbiddenNoRole(HttpMethod.POST, "/${UUID.randomUUID()}/submit-a-referral")
    }

    @Test
    fun `should return forbidden if wrong role`() {
      assertForbiddenWrongRole(HttpMethod.POST, "/${UUID.randomUUID()}/submit-a-referral")
    }

    @Test
    fun `should return OK and create submitted event for valid referral`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)

      val person = referralHelper.createPerson()
      val communityServiceProvider = referralHelper.getCommunityServiceProvider()
      val savedReferral = referralHelper.createDraftReferral(
        person = person,
        createdBy = testUser.id,
      )
      referralHelper.createProviderAssignment(savedReferral, communityServiceProvider)

      webTestClient.post()
        .uri("/${savedReferral.id}/submit-a-referral")
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
              personId = updatedReferral.personId,
              referenceNumber = updatedReferral.referenceNumber,
            )
            response.responseBody shouldBe submitReferralResponseDto
            response.responseBody?.referralId shouldBe savedReferral.id
            response.responseBody?.personId shouldBe person.id
            response.responseBody?.referenceNumber shouldNotBe null
          }

          val updated = referralRepository.findById(savedReferral.id).get()
          assertThat(updated.submittedEvent?.eventType).isEqualTo(ReferralEventType.SUBMITTED)
        }
    }

    @Test
    fun `should return 409 conflict when referral has already been submitted`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)

      val person = personRepository.save(
        PersonFactory()
          .withFirstName("Jane")
          .withLastName("Doe")
          .withIdentifier("X654321")
          .create(),
      )
      val alreadySubmittedReferral = referralHelper.createReferral(
        person = person,
        submittedBy = testUser,
      )

      webTestClient.post()
        .uri("/${alreadySubmittedReferral.id}/submit-a-referral")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .is4xxClientError
        .expectStatus()
        .isEqualTo(409)
    }
  }

  @Nested
  @DisplayName("PATCH /draft-referral/addition-support-needs/:referralId")
  inner class AdditionalSupportNeedsTest {

    @BeforeEach
    fun setup() {
      testDataCleaner.cleanAllTables()
      testUser = referralHelper.ensureReferralUser()
    }

    @Test
    fun `should return unauthorized if no token`() {
      assertUnauthorized(PATCH, "/draft-referral/addition-support-needs/${UUID.randomUUID()}")
    }

    @Test
    fun `should return OK and updated additional information for a draft referral - partial support needs`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)

      val person = referralHelper.createPerson()
      val communityServiceProvider = referralHelper.getCommunityServiceProvider()
      val referral = referralHelper.createDraftReferral(
        person = person,
        createdBy = testUser.id,
      )
      referralHelper.createProviderAssignment(referral, communityServiceProvider)

      val request = AdditionalSupportNeedsRequest(
        needsAdditionalSupport = true,
        physicalHealth = "Requires wheelchair access",
      )

      webTestClient.patch()
        .uri("/draft-referral/additional-support-needs/${referral.id}")
        .headers(setAuthorisation())
        .bodyValue(request)
        .exchange()
        .expectStatus().isOk
        .expectBody<AdditionalSupportNeedsBffResponseDto>()

      val supportNeeds = personAdditionalSupportNeedsRepository.findByReferralId(referral.id)
      supportNeeds shouldNotBe null
      supportNeeds!!.physicalHealthDetails shouldBe "Requires wheelchair access"
    }

    @Test
    fun `should return OK and updated additional information for a draft referral - full support needs`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)

      val person = referralHelper.createPerson()
      val communityServiceProvider = referralHelper.getCommunityServiceProvider()
      val referral = referralHelper.createDraftReferral(
        person = person,
        createdBy = testUser.id,
      )
      referralHelper.createProviderAssignment(referral, communityServiceProvider)

      val request = AdditionalSupportNeedsRequest(
        needsAdditionalSupport = true,
        physicalHealth = "Wheelchair access required",
        mentalEmotionalHealth = "Anxiety support needed",
        neurodiversity = "ADHD diagnosis",
        locationTravel = "Cannot use public transport",
        caringResponsibilities = "Caring for elderly parent",
        employmentResponsibilities = "Part-time work",
        diversity = "Requires cultural sensitivity",
        anythingElse = "Additional notes here",
      )

      webTestClient.patch()
        .uri("/draft-referral/additional-support-needs/${referral.id}")
        .headers(setAuthorisation())
        .bodyValue(request)
        .exchange()
        .expectStatus().isOk
        .expectBody<AdditionalSupportNeedsBffResponseDto>()

      val supportNeeds = personAdditionalSupportNeedsRepository.findByReferralId(referral.id)!!

      supportNeeds.noAdditionalSupportNeeded shouldBe false
      supportNeeds.physicalHealthDetails shouldBe "Wheelchair access required"
      supportNeeds.mentalEmotionalHealthDetails shouldBe "Anxiety support needed"
      supportNeeds.neurodiversityDetails shouldBe "ADHD diagnosis"
      supportNeeds.locationTravelDetails shouldBe "Cannot use public transport"
      supportNeeds.caringResponsibilitiesDetails shouldBe "Caring for elderly parent"
      supportNeeds.employmentResponsibilitiesDetails shouldBe "Part-time work"
      supportNeeds.diversityDetails shouldBe "Requires cultural sensitivity"
      supportNeeds.anythingElseDetails shouldBe "Additional notes here"
    }

    @Test
    fun `should return OK and updated additional information for a draft referral - no additional needs`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)

      val person = referralHelper.createPerson()
      val communityServiceProvider = referralHelper.getCommunityServiceProvider()
      val referral = referralHelper.createDraftReferral(
        person = person,
        createdBy = testUser.id,
      )
      referralHelper.createProviderAssignment(referral, communityServiceProvider)

      val request = AdditionalSupportNeedsRequest(
        needsAdditionalSupport = true,
        physicalHealth = "Wheelchair access required",
        mentalEmotionalHealth = "Anxiety support needed",
        neurodiversity = "ADHD diagnosis",
        locationTravel = "Cannot use public transport",
        caringResponsibilities = "Caring for elderly parent",
        employmentResponsibilities = "Part-time work",
        diversity = "Requires cultural sensitivity",
        anythingElse = "Additional notes here",
      )

      webTestClient.patch()
        .uri("/draft-referral/additional-support-needs/${referral.id}")
        .headers(setAuthorisation())
        .bodyValue(request)
        .exchange()
        .expectStatus().isOk
        .expectBody<AdditionalSupportNeedsBffResponseDto>()

      val supportNeeds = personAdditionalSupportNeedsRepository.findByReferralId(referral.id)!!

      supportNeeds.noAdditionalSupportNeeded shouldBe false
      supportNeeds.physicalHealthDetails shouldBe "Wheelchair access required"
      supportNeeds.mentalEmotionalHealthDetails shouldBe "Anxiety support needed"
      supportNeeds.neurodiversityDetails shouldBe "ADHD diagnosis"
      supportNeeds.locationTravelDetails shouldBe "Cannot use public transport"
      supportNeeds.caringResponsibilitiesDetails shouldBe "Caring for elderly parent"
      supportNeeds.employmentResponsibilitiesDetails shouldBe "Part-time work"
      supportNeeds.diversityDetails shouldBe "Requires cultural sensitivity"
      supportNeeds.anythingElseDetails shouldBe "Additional notes here"

      val updateRequest = AdditionalSupportNeedsRequest(
        needsAdditionalSupport = false,
      )

      webTestClient.patch()
        .uri("/draft-referral/additional-support-needs/${referral.id}")
        .headers(setAuthorisation())
        .bodyValue(updateRequest)
        .exchange()
        .expectStatus().isOk
        .expectBody<AdditionalSupportNeedsBffResponseDto>()

      val updatedSupportNeeds = personAdditionalSupportNeedsRepository.findByReferralId(referral.id)!!

      updatedSupportNeeds.noAdditionalSupportNeeded shouldBe true
      updatedSupportNeeds.physicalHealthDetails shouldBe null
      updatedSupportNeeds.mentalEmotionalHealthDetails shouldBe null
      updatedSupportNeeds.neurodiversityDetails shouldBe null
      updatedSupportNeeds.locationTravelDetails shouldBe null
      updatedSupportNeeds.caringResponsibilitiesDetails shouldBe null
      updatedSupportNeeds.employmentResponsibilitiesDetails shouldBe null
      updatedSupportNeeds.diversityDetails shouldBe null
      updatedSupportNeeds.anythingElseDetails shouldBe null
    }
  }

  @Nested
  @DisplayName("GET /bff/draft-referral/addition-support-needs/:referralId")
  inner class AdditionalSupportNeedsPageTest {

    @BeforeEach
    fun setup() {
      testDataCleaner.cleanAllTables()
      testUser = referralHelper.ensureReferralUser()
    }

    @Test
    fun `should return unauthorized if no token`() {
      assertUnauthorized(GET, "/bff/draft-referral/addition-support-needs/${UUID.randomUUID()}")
    }

    @Test
    fun `should return additional support needs for a draft referral`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)

      val person = referralHelper.createPerson()
      val communityServiceProvider = referralHelper.getCommunityServiceProvider()
      val referral = referralHelper.createDraftReferral(
        person = person,
        createdBy = testUser.id,
      )
      referralHelper.createProviderAssignment(referral, communityServiceProvider)

      val supportNeeds = PersonAdditionalSupportNeedsFactory()
        .withReferral(referral)
        .withPerson(person)
        .withNoAdditionalSupportNeeded(false)
        .withPhysicalHealthDetails("Wheelchair access required")
        .withCreatedBy(testUser.id)
        .create()

      personAdditionalSupportNeedsRepository.save(supportNeeds)

      webTestClient.get()
        .uri("/bff/draft-referral/additional-support-needs/${referral.id}")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBody<AdditionalSupportNeedsBffResponseDto>()
        .consumeWith { response ->
          val body = response.responseBody!!
          body.refereeName.firstName shouldBe person.firstName
          body.refereeName.lastName shouldBe person.lastName
          body.needsAdditionalSupport shouldBe true
          body.physicalHealth?.selected shouldBe true
          body.physicalHealth?.value shouldBe "Wheelchair access required"
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
      assertUnauthorized(GET, "/bff/referral-details-page/${referralHelper.communityServiceProviderId}")
    }

    @Test
    fun `should return forbidden if no role`() {
      assertForbiddenNoRole(GET, "/bff/referral-details-page/${referralHelper.communityServiceProviderId}")
    }

    @Test
    fun `should return forbidden if wrong role`() {
      assertForbiddenWrongRole(GET, "/bff/referral-details-page/${referralHelper.communityServiceProviderId}")
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

      val savedReferral = referralHelper.createReferral(person = person, submittedBy = testUser)

      val personDetailsTable = ReferralDetailsBffResponseDto.PersonDetailsTableDataDto(
        name = "${person.firstName} ${person.lastName}",
        crn = savedReferral.personIdentifier,
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
      assertNotFound(GET, "/bff/referral-details-page/${referralHelper.communityServiceProviderId}")
    }
  }

  @Nested
  @DisplayName("GET /bff/referral-details/{referralId}/progress")
  inner class ReferralProgressPageEndPoint {
    val referralId = UUID.randomUUID().toString()
    val referralCaseRef = "AA1234DD"

    @BeforeEach
    fun setup() {
      testDataCleaner.cleanAllTables()
      testUser = referralHelper.ensureReferralUser()
    }

    @Test
    fun `should return unauthorized if no token`() {
      assertUnauthorized(GET, "/bff/referral-details/$referralId/progress")
    }

    @Test
    fun `should return forbidden if no role`() {
      assertForbiddenNoRole(GET, "/bff/referral-details/$referralId/progress")
    }

    @Test
    fun `should return forbidden if wrong role`() {
      assertForbiddenWrongRole(GET, "/bff/referral-details/$referralId/progress")
    }

    @Test
    fun `should return 404 when referral does not exist`() {
      assertNotFound(GET, "/bff/referral-details/$referralId/progress")
    }

    @Test
    fun `should return an Referral Progress object with an empty appointments list when no appointments exist for referral`() {
      val person = referralHelper.createPerson()
      val referralUser = referralHelper.ensureReferralUser()
      val referral = referralHelper.createReferral(person, submittedBy = referralUser)

      webTestClient.get()
        .uri("/bff/referral-details/${referral.id}/progress")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBody<ReferralProgressDto>()
        .consumeWith { response ->
          val referralProgressDto = response.responseBody!!

          referralProgressDto.referralId shouldBe referral.id
          referralProgressDto.fullName shouldBe person.firstName + " " + person.lastName
          referralProgressDto.appointments.size shouldBe 0
        }
    }

    @Test
    fun `should return a referral Progress object when given a case reference`() {
      val person = referralHelper.createPerson()
      val referralUser = referralHelper.ensureReferralUser()
      val referral = referralHelper.createReferral(person, submittedBy = referralUser, referenceNumber = referralCaseRef)

      webTestClient.get()
        .uri("/bff/referral-details/$referralCaseRef/progress")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBody<ReferralProgressDto>()
        .consumeWith { response ->
          val referralProgressDto = response.responseBody!!

          referralProgressDto.referralId shouldBe referral.id
          referralProgressDto.fullName shouldBe person.firstName + " " + person.lastName
          referralProgressDto.appointments.size shouldBe 0
        }
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

      val ics = appointmentHelper.createAppointmentIcs(
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
        .expectBody<ReferralProgressDto>()
        .consumeWith { response ->
          val referralProgressDto = response.responseBody!!

          referralProgressDto.referralId shouldBe referral.id
          referralProgressDto.fullName shouldBe person.firstName + " " + person.lastName
          referralProgressDto.appointments.size shouldBe 1
          referralProgressDto.appointments[0].appointmentIcsId shouldBe ics.id
          referralProgressDto.appointments[0].dateTime shouldBe appointmentDateTime
          referralProgressDto.appointments[0].status shouldBe AppointmentStatusHistoryType.NEEDS_FEEDBACK
        }
    }
  }

  @Nested
  @DisplayName("GET /bff/referral-details/{caseReference}/ics")
  inner class GetICSDetailsEndPoint {
    val caseReference = "AA1234DD"

    @BeforeEach
    fun setup() {
      testDataCleaner.cleanAllTables()
      testUser = referralHelper.ensureReferralUser()
    }

    @Test
    fun `should return unauthorized if no token`() {
      assertUnauthorized(GET, "/bff/referral-details/$caseReference/ics")
    }

    @Test
    fun `should return forbidden if no role`() {
      assertForbiddenNoRole(GET, "/bff/referral-details/$caseReference/ics")
    }

    @Test
    fun `should return forbidden if wrong role`() {
      assertForbiddenWrongRole(GET, "/bff/referral-details/$caseReference/ics")
    }

    @Test
    fun `should return 404 when referral does not exist`() {
      assertNotFound(GET, "/bff/referral-details/$caseReference/ics")
    }

    @Test
    fun `should return 404 when referral has no appointments`() {
      val person = referralHelper.createPerson()
      val referralUser = referralHelper.ensureReferralUser()
      val referral = referralHelper.createReferral(person, submittedBy = referralUser, referenceNumber = caseReference)

      assertNotFound(GET, "/bff/referral-details/${referral.referenceNumber}/ics")
    }

    @Test
    fun `should return 200 when ICS details exists`() {
      val person = referralHelper.createPerson()
      val referralUser = referralHelper.ensureReferralUser()
      val referral = referralHelper.createReferral(person, submittedBy = referralUser, referenceNumber = caseReference)
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
        .uri("/bff/referral-details/$caseReference/ics")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBody<AppointmentIcsResponse>()
        .consumeWith { result ->
          val body = result.responseBody!!
          body.appointmentIcsId shouldBe savedIcs.id
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
  }

  @Nested
  @DisplayName("GET /bff/confirm-person-details/{personIdentifier}")
  inner class ConfirmPersonDetailsEndPoint {

    @BeforeEach
    fun setup() {
      testDataCleaner.cleanAllTables()
      testUser = referralHelper.ensureReferralUser()
    }

    @Test
    fun `should return 200 with person details for a valid CRN`() {
      val person = referralHelper.createPerson(identifier = CRN)

      stubFor(
        get(urlEqualTo("/person/probation/$CRN"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(cprProbationPersonJson(CRN)),
          ),
      )

      webTestClient.get()
        .uri("/bff/confirm-person-details/$CRN")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBody<ConfirmPersonDetailsBffDto>()
        .consumeWith { response ->
          val body = response.responseBody!!

          body.id shouldBe person.id
          body.personalDetails.firstName shouldBe "John"
          body.personalDetails.middleNames shouldBe "David"
          body.personalDetails.lastName shouldBe "Smith"
          body.personalDetails.crn shouldBe CRN
          body.personalDetails.dateOfBirth shouldBe LocalDate.of(1985, 1, 1)
          body.personalDetails.disabilities.allDisabilities shouldBe "None"
          body.equalityMonitoring.sex shouldBe "Male"
          body.equalityMonitoring.ethnicity shouldBe "White"
          body.equalityMonitoring.religionOrBelief shouldBe "Christian"
          body.equalityMonitoring.sexualOrientation shouldBe "Heterosexual"
          body.equalityMonitoring.nationalities shouldBe listOf("Argentine", "Brazilian")
          body.contactDetails.phoneNumber shouldBe "01234567890"
          body.contactDetails.mobileNumber shouldBe "07700900002"
          body.contactDetails.emailAddress shouldBe "john.smith@example.com"
          body.contactDetails.address.value shouldBe "1, Test Street, Testville, TE1 1ST"
          body.contactDetails.address.type shouldBe "Friends/Family (settled) (verified)"
          body.contactDetails.address.startAt shouldBe "2005-12-01"
          body.contactDetails.address.notes shouldBe "No notes"
        }
    }
  }

  @Nested
  @DisplayName("GET /bff/task-list-status/{referralId}")
  inner class TaskListStatusEndPoint {
    @BeforeEach
    fun setup() {
      testDataCleaner.cleanAllTables()
    }

    @Test
    fun `should return 200 with all task list statuses as false`() {
      webTestClient.get()
        .uri("/bff/task-list-status/${UUID.randomUUID()}")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBody<TaskListStatusResponseDto>()
        .consumeWith { response ->
          val body = response.responseBody!!

          body.confirmPersonalDetails shouldBe false
          body.checkRiskInformation shouldBe false
          body.selectThePersonsNeeds shouldBe false
          body.addDetailsOfAnyAdditionalSupportNeeds shouldBe false
          body.addDetailsOfMainPointOfContact shouldBe false
          body.checkAnswers shouldBe false
        }
    }
  }

  @Nested
  @DisplayName("GET /bff/referral-information/{caseReference}")
  inner class ReferralInformationEndPoint {
    val caseReference = "AA1234DD"

    @BeforeEach
    fun setup() {
      testDataCleaner.cleanAllTables()
      testUser = referralHelper.ensureReferralUser()
    }

    @Test
    fun `should return unauthorized if no token`() {
      assertUnauthorized(GET, "/bff/referral-information/$caseReference")
    }

    @Test
    fun `should return forbidden if no role`() {
      assertForbiddenNoRole(GET, "/bff/referral-information/$caseReference")
    }

    @Test
    fun `should return forbidden if wrong role`() {
      assertForbiddenWrongRole(GET, "/bff/referral-information/$caseReference")
    }

    @Test
    fun `should return 404 when referral does not exist`() {
      assertNotFound(GET, "/bff/referral-information/$caseReference")
    }

    @Test
    fun `should return 200 when referral information exists`() {
      val person = referralHelper.createPerson()
      val referralUser = referralHelper.ensureReferralUser()
      val communityServiceProvider = referralHelper.getCommunityServiceProvider()
      val referral = referralHelper.createReferral(person, submittedBy = referralUser)

      val providerAssignment = ReferralProviderAssignmentFactory()
        .withReferral(referral)
        .withCommunityServiceProvider(communityServiceProvider)
        .create()
      referralProviderAssignmentRepository.save(providerAssignment)

      webTestClient.get()
        .uri("/bff/referral-information/${referral.id}")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBody<ReferralInformationDto>()
        .consumeWith { result ->
          val body = result.responseBody!!
          body.referralId shouldBe referral.id
          body.personIdentifier shouldBe referral.personIdentifier
          body.firstName shouldBe person.firstName
          body.lastName shouldBe person.lastName
        }
    }
  }
}
