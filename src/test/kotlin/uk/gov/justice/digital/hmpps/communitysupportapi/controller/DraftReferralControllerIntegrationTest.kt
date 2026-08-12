package uk.gov.justice.digital.hmpps.communitysupportapi.controller

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.PATCH
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.communitysupportapi.authorization.UserMapper
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.AdditionalSupportNeedsBffResponseDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.AreaConfirmationBffResponseDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CommunityServiceProviderBffResponseDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.NeedsInterpreterBffResponseDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralCriminogenicNeedsDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.TaskListStatusItem
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.TaskListStatusResponseDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralCriminogenicNeeds
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.ReferralTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.model.AdditionalSupportNeedsRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.model.CommunityServiceProviderRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.model.CriminogenicNeedsRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.model.NeedsInterpreterRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.CommunityServiceProviderRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PduRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonAdditionalSupportNeedsRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralCriminogenicNeedsRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralProviderAssignmentRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.RiskInformationRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.PersonAdditionalDetailsFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.PersonAdditionalSupportNeedsFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.RiskInformationFactory
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import java.time.OffsetDateTime
import java.util.UUID

class DraftReferralControllerIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var personRepository: PersonRepository

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @Autowired
  private lateinit var personAdditionalSupportNeedsRepository: PersonAdditionalSupportNeedsRepository

  @Autowired
  private lateinit var riskInformationRepository: RiskInformationRepository

  @Autowired
  private lateinit var pduRepository: PduRepository

  @Autowired
  private lateinit var referralCriminogenicNeedsRepository: ReferralCriminogenicNeedsRepository

  @Autowired
  private lateinit var referralHelper: ReferralTestSupport

  @Autowired
  private lateinit var communityServiceProviderRepository: CommunityServiceProviderRepository

  @Autowired
  private lateinit var referralProviderAssignmentRepository: ReferralProviderAssignmentRepository

  @MockitoBean
  private lateinit var userMapper: UserMapper

  private lateinit var testUser: ReferralUser

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

      supportNeeds.additionalSupportNeeded shouldBe true
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

      supportNeeds.additionalSupportNeeded shouldBe true
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

      updatedSupportNeeds.additionalSupportNeeded shouldBe false
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
        .withAdditionalSupportNeeded(true)
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
          body.physicalHealth.selected shouldBe true
          body.physicalHealth.value shouldBe "Wheelchair access required"
        }
    }
  }

  @Nested
  @DisplayName("PATCH /draft-referral/needs-interpreter/:referralId")
  inner class NeedsInterpreterTest {

    @BeforeEach
    fun setup() {
      testDataCleaner.cleanAllTables()
      testUser = referralHelper.ensureReferralUser()
    }

    @Test
    fun `should return unauthorized if no token`() {
      assertUnauthorized(PATCH, "/draft-referral/needs-interpreter/${UUID.randomUUID()}")
    }

    @Test
    fun `should return OK and updated needs-interpreter for a draft referral`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)

      val person = referralHelper.createPerson()
      val communityServiceProvider = referralHelper.getCommunityServiceProvider()
      val referral = referralHelper.createDraftReferral(
        person = person,
        createdBy = testUser.id,
      )
      referralHelper.createProviderAssignment(referral, communityServiceProvider)

      val request = NeedsInterpreterRequest(
        needsInterpreter = true,
        language = "Italian",
      )

      webTestClient.patch()
        .uri("/draft-referral/needs-interpreter/${referral.id}")
        .headers(setAuthorisation())
        .bodyValue(request)
        .exchange()
        .expectStatus().isOk
        .expectBody<NeedsInterpreterBffResponseDto>()
        .consumeWith { response ->
          val body = response.responseBody!!
          body.refereeName.firstName shouldBe person.firstName
          body.refereeName.lastName shouldBe person.lastName
          body.language?.selected shouldBe true
          body.language?.value shouldBe "Italian"
          body.needsInterpreter shouldBe true
        }
    }

    @Test
    fun `should return OK and interpreter needs for a draft referral - no interpreter needed`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)

      val person = referralHelper.createPerson()
      val communityServiceProvider = referralHelper.getCommunityServiceProvider()
      val referral = referralHelper.createDraftReferral(
        person = person,
        createdBy = testUser.id,
      )
      referralHelper.createProviderAssignment(referral, communityServiceProvider)

      val request = NeedsInterpreterRequest(
        needsInterpreter = true,
        language = "German",
      )

      webTestClient.patch()
        .uri("/draft-referral/needs-interpreter/${referral.id}")
        .headers(setAuthorisation())
        .bodyValue(request)
        .exchange()
        .expectStatus().isOk
        .expectBody<NeedsInterpreterBffResponseDto>()

      val needs = personAdditionalSupportNeedsRepository.findByReferralId(referral.id)!!
      needs.interpreterLanguage shouldBe "German"

      val updateRequest = NeedsInterpreterRequest(
        needsInterpreter = false,
      )

      webTestClient.patch()
        .uri("/draft-referral/needs-interpreter/${referral.id}")
        .headers(setAuthorisation())
        .bodyValue(updateRequest)
        .exchange()
        .expectStatus().isOk
        .expectBody<NeedsInterpreterBffResponseDto>()
        .consumeWith { response ->
          val body = response.responseBody!!
          body.refereeName.firstName shouldBe person.firstName
          body.refereeName.lastName shouldBe person.lastName
          body.language?.selected shouldBe false
          body.language?.value shouldBe null
          body.needsInterpreter shouldBe false
        }
    }
  }

  @Nested
  @DisplayName("GET /bff/draft-referral/community-service-provider/:providerId")
  inner class AreaConfirmationTest {

    @BeforeEach
    fun setup() {
      testDataCleaner.cleanAllTables()
      testUser = referralHelper.ensureReferralUser()
    }

    @Test
    fun `should return unauthorized if no token`() {
      assertUnauthorized(GET, "/bff/draft-referral/community-service-provider/${UUID.randomUUID()}")
    }

    @Test
    fun `should return 404 when community service provider does not exist`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)

      webTestClient.get()
        .uri("/bff/draft-referral/community-service-provider/${UUID.randomUUID()}")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `should return community service provider details`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)

      val communityServiceProvider = referralHelper.getCommunityServiceProvider()
      val expectedAssociatedPdus = pduRepository.findByContractAreaId(communityServiceProvider.contractArea.id)
        .map { it.name }
        .sorted()

      webTestClient.get()
        .uri("/bff/draft-referral/community-service-provider/${communityServiceProvider.id}")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBody<AreaConfirmationBffResponseDto>()
        .consumeWith { response ->
          val body = response.responseBody!!
          body.contractArea shouldBe communityServiceProvider.contractArea.area
          body.deliveryPartner shouldBe communityServiceProvider.serviceProvider.name
          body.associatedPdus shouldBe expectedAssociatedPdus
        }
    }
  }

  @Nested
  @DisplayName("PATCH /draft-referral/community-service-provider/:referralId")
  inner class CommunityServiceProviderTest {

    @BeforeEach
    fun setup() {
      testDataCleaner.cleanAllTables()
      testUser = referralHelper.ensureReferralUser()
    }

    @Test
    fun `should return unauthorized if no token`() {
      assertUnauthorized(PATCH, "/draft-referral/community-service-provider/${UUID.randomUUID()}")
    }

    @Test
    fun `should return OK and update the community service provider for a draft referral`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)

      val person = referralHelper.createPerson()
      val communityServiceProvider = referralHelper.getCommunityServiceProvider()
      val referral = referralHelper.createDraftReferral(
        person = person,
        createdBy = testUser.id,
      )
      referralHelper.createProviderAssignment(referral, communityServiceProvider)

      val newCommunityServiceProvider = communityServiceProviderRepository.findAll()
        .first { it.id != communityServiceProvider.id }

      val request = CommunityServiceProviderRequest(
        communityServiceProviderId = newCommunityServiceProvider.id,
      )

      webTestClient.patch()
        .uri("/draft-referral/community-service-provider/${referral.id}")
        .headers(setAuthorisation())
        .bodyValue(request)
        .exchange()
        .expectStatus().isOk
        .expectBody<CommunityServiceProviderBffResponseDto>()
        .consumeWith { response ->
          val body = response.responseBody!!
          body.referralId shouldBe referral.id
          body.communityServiceProviderId shouldBe newCommunityServiceProvider.id
          body.communityServiceProviderName shouldBe newCommunityServiceProvider.name
        }

      val assignments = referralProviderAssignmentRepository.findByReferralId(referral.id)
      assignments.size shouldBe 1
      assignments.first().communityServiceProvider.id shouldBe newCommunityServiceProvider.id
    }

    @Test
    fun `should return 404 when referral does not exist`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)

      val communityServiceProvider = referralHelper.getCommunityServiceProvider()

      val request = CommunityServiceProviderRequest(
        communityServiceProviderId = communityServiceProvider.id,
      )

      webTestClient.patch()
        .uri("/draft-referral/community-service-provider/${UUID.randomUUID()}")
        .headers(setAuthorisation())
        .bodyValue(request)
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `should return 404 when community service provider does not exist`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)

      val person = referralHelper.createPerson()
      val communityServiceProvider = referralHelper.getCommunityServiceProvider()
      val referral = referralHelper.createDraftReferral(
        person = person,
        createdBy = testUser.id,
      )
      referralHelper.createProviderAssignment(referral, communityServiceProvider)

      val request = CommunityServiceProviderRequest(
        communityServiceProviderId = UUID.randomUUID(),
      )

      webTestClient.patch()
        .uri("/draft-referral/community-service-provider/${referral.id}")
        .headers(setAuthorisation())
        .bodyValue(request)
        .exchange()
        .expectStatus().isNotFound
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
      val testUser = referralHelper.createTestUser()
      val person = referralHelper.createPerson(identifier = "CRN12345")
      val additionalDetails = PersonAdditionalDetailsFactory()
        .withPerson(person)
        .withEthnicity("White")
        .withPreferredLanguage("English")
        .withNeurodiverseConditions("None")
        .withReligionOrBelief("None")
        .withAddress("123 Test Street /n Test Town /n Testshire")
        .withPhoneNumber("0191 234 5678")
        .withEmailAddress("test@test.com")
        .create()

      person.additionalDetails = additionalDetails
      personRepository.save(person)

      val savedReferral = referralHelper.createReferral(person = person, submittedBy = testUser, targetServiceCompletionDate = null, targetServiceCompletionDateReason = null)
      referralRepository.save(savedReferral)

      webTestClient.get()
        .uri("/bff/task-list-status/${savedReferral.id}")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBody<TaskListStatusResponseDto>()
        .consumeWith { response ->
          val body = response.responseBody!!

          body.fullName shouldBe "John Smith"
          body.confirmPersonalDetailsCompleted shouldBe TaskListStatusItem.notStarted()
          body.checkRiskInformationCompleted shouldBe TaskListStatusItem.notStarted()
          body.selectThePersonsNeedsCompleted shouldBe TaskListStatusItem.notStarted()
          body.addDetailsOfAnyAdditionalSupportNeedsCompleted shouldBe TaskListStatusItem.notStarted()
          body.addDetailsOfMainPointOfContactCompleted shouldBe TaskListStatusItem.notStarted()
          body.selectAnAreaForReferralCompleted shouldBe TaskListStatusItem.notStarted()
          body.addAdditionalInformationCompleted shouldBe TaskListStatusItem.notStarted()
        }
    }

    @Test
    fun `should return inProgress for addDetailsOfAnyAdditionalSupportNeedsCompleted when additionalSupportNeeds is partially complete`() {
      val testUser = referralHelper.createTestUser()
      val person = referralHelper.createPerson(identifier = "CRN12345")
      val savedReferral = referralHelper.createReferral(person = person, submittedBy = testUser)
      referralRepository.save(savedReferral)

      val supportNeeds = PersonAdditionalSupportNeedsFactory()
        .withReferral(savedReferral)
        .withPerson(person)
        .withAdditionalSupportNeeded(true)
        .withCreatedBy(testUser.id)
        .create()
      personAdditionalSupportNeedsRepository.save(supportNeeds)

      webTestClient.get()
        .uri("/bff/task-list-status/${savedReferral.id}")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBody<TaskListStatusResponseDto>()
        .consumeWith { response ->
          val body = response.responseBody!!

          body.addDetailsOfAnyAdditionalSupportNeedsCompleted shouldBe TaskListStatusItem.inProgress()
        }
    }

    @Test
    fun `should return completed for addDetailsOfAnyAdditionalSupportNeedsCompleted when additionalSupportNeeds is fully complete`() {
      val testUser = referralHelper.createTestUser()
      val person = referralHelper.createPerson(identifier = "CRN12345")
      val savedReferral = referralHelper.createReferral(person = person, submittedBy = testUser)
      referralRepository.save(savedReferral)

      val supportNeeds = PersonAdditionalSupportNeedsFactory()
        .withReferral(savedReferral)
        .withPerson(person)
        .withAdditionalSupportNeeded(true)
        .withInterpreterNeeded(true)
        .withCreatedBy(testUser.id)
        .create()
      personAdditionalSupportNeedsRepository.save(supportNeeds)

      webTestClient.get()
        .uri("/bff/task-list-status/${savedReferral.id}")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBody<TaskListStatusResponseDto>()
        .consumeWith { response ->
          val body = response.responseBody!!

          body.addDetailsOfAnyAdditionalSupportNeedsCompleted shouldBe TaskListStatusItem.completed()
        }
    }

    @Test
    fun `should return completed for checkRiskInformationCompleted when risk info exists`() {
      val testUser = referralHelper.createTestUser()
      val person = referralHelper.createPerson(identifier = "CRN12345")
      val savedReferral = referralHelper.createReferral(person = person, submittedBy = testUser)
      referralRepository.save(savedReferral)

      val riskInfo = RiskInformationFactory()
        .withReferral(savedReferral)
        .withUpdatedBy(testUser.id)
        .create()
      riskInformationRepository.save(riskInfo)

      webTestClient.get()
        .uri("/bff/task-list-status/${savedReferral.id}")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBody<TaskListStatusResponseDto>()
        .consumeWith { response ->
          val body = response.responseBody!!

          body.checkRiskInformationCompleted shouldBe TaskListStatusItem.completed()
        }
    }

    @Test
    fun `should return completed for selectAnAreaForReferralCompleted when community service provider is assigned`() {
      val testUser = referralHelper.createTestUser()
      val person = referralHelper.createPerson(identifier = "CRN12345")
      val savedReferral = referralHelper.createReferral(person = person, submittedBy = testUser)
      referralRepository.save(savedReferral)

      val communityServiceProvider = referralHelper.getCommunityServiceProvider()
      referralHelper.createProviderAssignment(savedReferral, communityServiceProvider)

      webTestClient.get()
        .uri("/bff/task-list-status/${savedReferral.id}")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBody<TaskListStatusResponseDto>()
        .consumeWith { response ->
          val body = response.responseBody!!

          body.selectAnAreaForReferralCompleted shouldBe TaskListStatusItem.completed()
        }
    }

    @Test
    fun `should return completed for addAdditionalInformationCompleted when target date and reason exist`() {
      val testUser = referralHelper.createTestUser()
      val person = referralHelper.createPerson(identifier = "CRN12345")
      val savedReferral = referralHelper.createReferral(
        person = person,
        submittedBy = testUser,
      )
      referralRepository.save(savedReferral)

      webTestClient.get()
        .uri("/bff/task-list-status/${savedReferral.id}")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBody<TaskListStatusResponseDto>()
        .consumeWith { response ->
          val body = response.responseBody!!

          body.addAdditionalInformationCompleted shouldBe TaskListStatusItem.completed()
        }
    }
  }

  @Nested
  @DisplayName("PATCH /draft-referral/person-needs/:referralId")
  inner class CriminogenicNeedsPatchTest {

    @BeforeEach
    fun setup() {
      testDataCleaner.cleanAllTables()
      testUser = referralHelper.ensureReferralUser()
    }

    @Test
    fun `should return unauthorized if no token`() {
      assertUnauthorized(PATCH, "/draft-referral/person-needs/${UUID.randomUUID()}")
    }

    @Test
    fun `should return bad request when selected criminogenic need has no details`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)

      val person = referralHelper.createPerson()
      val communityServiceProvider = referralHelper.getCommunityServiceProvider()
      val referral = referralHelper.createDraftReferral(person = person, createdBy = testUser.id)

      referralHelper.createProviderAssignment(referral, communityServiceProvider)

      val request = CriminogenicNeedsRequest(hasAccommodationNeeds = true)

      webTestClient.patch()
        .uri("/draft-referral/person-needs/${referral.id}")
        .headers(setAuthorisation())
        .bodyValue(request)
        .exchange()
        .expectStatus().isBadRequest

      referralCriminogenicNeedsRepository.findByReferralId(referral.id) shouldBe null
    }

    @Test
    fun `should return OK and create criminogenic needs for a draft referral`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)

      val person = referralHelper.createPerson()
      val communityServiceProvider = referralHelper.getCommunityServiceProvider()
      val referral = referralHelper.createDraftReferral(person = person, createdBy = testUser.id)

      referralHelper.createProviderAssignment(referral, communityServiceProvider)

      val request = CriminogenicNeedsRequest(
        hasAccommodationNeeds = true,
        accommodationDetails = "Needs emergency housing",
        hasDrugUseNeeds = false,
      )

      webTestClient.patch()
        .uri("/draft-referral/person-needs/${referral.id}")
        .headers(setAuthorisation())
        .bodyValue(request)
        .exchange()
        .expectStatus().isOk
        .expectBody<ReferralCriminogenicNeedsDto>()
        .consumeWith { response ->
          val body = response.responseBody!!
          body.referralId shouldBe referral.id
          body.refereeName.firstName shouldBe person.firstName
          body.refereeName.lastName shouldBe person.lastName
          body.hasAccommodationNeeds shouldBe true
          body.accommodationDetails shouldBe "Needs emergency housing"
          body.hasDrugUseNeeds shouldBe false
          body.updatedBy shouldBe testUser.id
        }

      val savedCriminogenicNeedsRecord = referralCriminogenicNeedsRepository.findByReferralId(referral.id)
      savedCriminogenicNeedsRecord shouldNotBe null
      savedCriminogenicNeedsRecord!!.hasAccommodationNeeds shouldBe true
      savedCriminogenicNeedsRecord.accommodationDetails shouldBe "Needs emergency housing"
      savedCriminogenicNeedsRecord.hasDrugUseNeeds shouldBe false
      savedCriminogenicNeedsRecord.updatedBy shouldBe testUser.id
    }

    @Test
    fun `should return OK and update existing criminogenic needs for a draft referral`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)

      val person = referralHelper.createPerson()
      val communityServiceProvider = referralHelper.getCommunityServiceProvider()
      val referral = referralHelper.createDraftReferral(person = person, createdBy = testUser.id)
      referralHelper.createProviderAssignment(referral, communityServiceProvider)

      val existing = referralCriminogenicNeedsRepository.save(
        ReferralCriminogenicNeeds(
          id = UUID.randomUUID(),
          referral = referral,
          hasAccommodationNeeds = false,
          updatedAt = OffsetDateTime.now().minusDays(1),
          updatedBy = testUser.id,
        ),
      )

      val request = CriminogenicNeedsRequest(hasAccommodationNeeds = true, accommodationDetails = "Updated accommodation details")

      webTestClient.patch()
        .uri("/draft-referral/person-needs/${referral.id}")
        .headers(setAuthorisation())
        .bodyValue(request)
        .exchange()
        .expectStatus().isOk
        .expectBody<ReferralCriminogenicNeedsDto>()
        .consumeWith { response ->
          val body = response.responseBody!!
          body.id shouldBe existing.id
          body.refereeName.firstName shouldBe person.firstName
          body.refereeName.lastName shouldBe person.lastName
          body.hasAccommodationNeeds shouldBe true
          body.accommodationDetails shouldBe "Updated accommodation details"
        }

      val savedCriminogenicNeedsRecord = referralCriminogenicNeedsRepository.findByReferralId(referral.id)
      savedCriminogenicNeedsRecord shouldNotBe null
      savedCriminogenicNeedsRecord!!.id shouldBe existing.id
      savedCriminogenicNeedsRecord.hasAccommodationNeeds shouldBe true
      savedCriminogenicNeedsRecord.accommodationDetails shouldBe "Updated accommodation details"
    }

    @Test
    fun `should clear previously saved needs when patch payload omits those fields`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)

      val person = referralHelper.createPerson()
      val communityServiceProvider = referralHelper.getCommunityServiceProvider()
      val referral = referralHelper.createDraftReferral(person = person, createdBy = testUser.id)
      referralHelper.createProviderAssignment(referral, communityServiceProvider)

      val existing = referralCriminogenicNeedsRepository.save(
        ReferralCriminogenicNeeds(
          id = UUID.randomUUID(),
          referral = referral,
          hasAccommodationNeeds = true,
          accommodationDetails = "Needs emergency housing",
          hasFinancialNeeds = true,
          financialDetails = "Needs debt support",
          updatedAt = OffsetDateTime.now().minusDays(1),
          updatedBy = testUser.id,
        ),
      )

      val request = CriminogenicNeedsRequest(
        hasFinancialNeeds = true,
        financialDetails = "Updated debt support",
      )

      webTestClient.patch()
        .uri("/draft-referral/person-needs/${referral.id}")
        .headers(setAuthorisation())
        .bodyValue(request)
        .exchange()
        .expectStatus().isOk
        .expectBody<ReferralCriminogenicNeedsDto>()
        .consumeWith { response ->
          val body = response.responseBody!!
          body.id shouldBe existing.id
          body.refereeName.firstName shouldBe person.firstName
          body.refereeName.lastName shouldBe person.lastName
          body.hasAccommodationNeeds shouldBe null
          body.accommodationDetails shouldBe null
          body.hasFinancialNeeds shouldBe true
          body.financialDetails shouldBe "Updated debt support"
        }

      val savedCriminogenicNeedsRecord = referralCriminogenicNeedsRepository.findByReferralId(referral.id)
      savedCriminogenicNeedsRecord shouldNotBe null
      savedCriminogenicNeedsRecord!!.id shouldBe existing.id
      savedCriminogenicNeedsRecord.hasAccommodationNeeds shouldBe null
      savedCriminogenicNeedsRecord.accommodationDetails shouldBe null
      savedCriminogenicNeedsRecord.hasFinancialNeeds shouldBe true
      savedCriminogenicNeedsRecord.financialDetails shouldBe "Updated debt support"
    }
  }

  @Nested
  @DisplayName("GET /bff/draft-referral/person-needs/:referralId")
  inner class CriminogenicNeedsGetTest {

    @BeforeEach
    fun setup() {
      testDataCleaner.cleanAllTables()
      testUser = referralHelper.ensureReferralUser()
    }

    @Test
    fun `should return unauthorized if no token`() {
      assertUnauthorized(GET, "/bff/draft-referral/person-needs/${UUID.randomUUID()}")
    }

    @Test
    fun `should return not found when criminogenic needs do not exist for referral`() {
      val person = referralHelper.createPerson()
      val communityServiceProvider = referralHelper.getCommunityServiceProvider()
      val referral = referralHelper.createDraftReferral(person = person, createdBy = testUser.id)
      referralHelper.createProviderAssignment(referral, communityServiceProvider)

      assertNotFound(GET, "/bff/draft-referral/person-needs/${referral.id}")
    }

    @Test
    fun `should return criminogenic needs for a referral`() {
      val person = referralHelper.createPerson()
      val communityServiceProvider = referralHelper.getCommunityServiceProvider()
      val referral = referralHelper.createDraftReferral(person = person, createdBy = testUser.id)
      referralHelper.createProviderAssignment(referral, communityServiceProvider)

      referralCriminogenicNeedsRepository.save(
        ReferralCriminogenicNeeds(
          id = UUID.randomUUID(),
          referral = referral,
          hasFinancialNeeds = true,
          financialDetails = "Needs debt management support",
          updatedAt = OffsetDateTime.now(),
          updatedBy = testUser.id,
        ),
      )

      webTestClient.get()
        .uri("/bff/draft-referral/person-needs/${referral.id}")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBody<ReferralCriminogenicNeedsDto>()
        .consumeWith { response ->
          val body = response.responseBody!!
          body.referralId shouldBe referral.id
          body.refereeName.firstName shouldBe person.firstName
          body.refereeName.lastName shouldBe person.lastName
          body.hasFinancialNeeds shouldBe true
          body.financialDetails shouldBe "Needs debt management support"
          body.updatedBy shouldBe testUser.id
        }
    }
  }
}
