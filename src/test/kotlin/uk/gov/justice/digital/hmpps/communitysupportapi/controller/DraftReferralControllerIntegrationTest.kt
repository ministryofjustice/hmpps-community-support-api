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
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.NeedsInterpreterBffResponseDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.TaskListStatusItem
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.TaskListStatusResponseDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.ReferralTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.model.AdditionalSupportNeedsRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.model.NeedsInterpreterRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonAdditionalSupportNeedsRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.PersonAdditionalDetailsFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.PersonAdditionalSupportNeedsFactory
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import java.util.UUID

class DraftReferralControllerIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var personRepository: PersonRepository

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @Autowired
  private lateinit var personAdditionalSupportNeedsRepository: PersonAdditionalSupportNeedsRepository

  @Autowired
  private lateinit var referralHelper: ReferralTestSupport

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
        }
    }
  }
}
