package uk.gov.justice.digital.hmpps.communitysupportapi.controller

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.communitysupportapi.authorization.UserMapper
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.AssignmentFailureDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CaseWorkerDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.UserType
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.model.AssignCaseWorkersRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.model.AssignCaseWorkersResult
import uk.gov.justice.digital.hmpps.communitysupportapi.model.CreateReferralRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.CommunityServiceProviderRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralUserAssignmentRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralUserRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.service.ReferralService
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ReferralUserFactory
import uk.gov.justice.hmpps.kotlin.auth.AuthSource
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

class ReferralUserAssignmentControllerTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @Autowired
  private lateinit var personRepository: PersonRepository

  @Autowired
  private lateinit var referralUserRepository: ReferralUserRepository

  @Autowired
  private lateinit var referralService: ReferralService

  @Autowired
  private lateinit var communityServiceProviderRepository: CommunityServiceProviderRepository

  @Autowired
  private lateinit var referralUserAssignmentRepository: ReferralUserAssignmentRepository

  @MockitoBean
  private lateinit var userMapper: UserMapper

  private val testUser = ReferralUserFactory()
    .withHmppsAuthUsername("test-user")
    .create()

  // kotlin
  @Nested
  @DisplayName("POST /bff/referral/{referralId}/assign")
  inner class AssignCaseWorkers {

    @BeforeEach
    fun setup() {
//      testDataCleaner.cleanAllTables()
    }

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.post()
        .uri("/bff/referral/${UUID.randomUUID()}/assign")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.post()
        .uri("/bff/referral/${UUID.randomUUID()}/assign")
        .headers(
          setAuthorisation(
            "AUTH_ADM",
            listOf(),
            listOf("read"),
          ),
        )
        .bodyValue(
          AssignCaseWorkersRequest(
            emails = listOf("alexsmith@email.com"),
          ),
        )
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.post()
        .uri("/bff/referral/${UUID.randomUUID()}/assign")
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .bodyValue(
          AssignCaseWorkersRequest(
            emails = listOf("alexsmith@email.com"),
          ),
        )
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return 404 indicating failure to assign case worker`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)

      val referral = setUpReferral()

      webTestClient.post()
        .uri("/bff/referral/${referral.id}/assign")
        .headers(setAuthorisation())
        .bodyValue(
          AssignCaseWorkersRequest(
            emails = listOf("alexsmith@email.com"),
          ),
        )
        .exchange()
        .expectStatus()
        .isBadRequest
        .expectBody(AssignCaseWorkersResult::class.java)
        .consumeWith { response ->
          run {
            val submitReferralResponseDto = AssignCaseWorkersResult(
              success = false,
              message = "Failed to assign case worker(s)",
              failureList = listOf(
                AssignmentFailureDto(
                  "alexsmith@email.com",
                  "Could not find a caseworker with that email address.",
                ),
              ),
            )
            response.responseBody shouldBe submitReferralResponseDto
          }
        }
    }

    @Test
    fun `should return OK indicating successful to assign case worker(s)`() {
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)

      val referral = setUpReferral()
      var assigner = setupAssigner(testUser)
      val user = setupUser("johnsmith@email.com")

      webTestClient.post()
        .uri("/bff/referral/${referral.id}/assign")
        .headers(setAuthorisation())
        .bodyValue(
          AssignCaseWorkersRequest(
            emails = listOf("johnsmith@email.com"),
          ),
        )
        .exchange()
        .expectStatus()
        .isOk
        .expectBody(AssignCaseWorkersResult::class.java)
        .consumeWith { response ->
          run {
            val submitReferralResponseDto = AssignCaseWorkersResult(
              success = true,
              message = "The case has been assigned to a caseworker.",
              succeededList = listOf(
                CaseWorkerDto(
                  userType = UserType.INTERNAL,
                  user.id,
                  fullName = user.fullName,
                  emailAddress = user.emailAddress,
                ),
              ),
            )
            response.responseBody shouldBe submitReferralResponseDto
          }
        }
    }
  }

  private fun setUpReferral(): Referral {
    val personDetails = Person(
      id = UUID.randomUUID(),
      firstName = "John",
      lastName = "Smith",
      identifier = "X123456",
      dateOfBirth = LocalDate.of(1980, 1, 1),
      gender = "Male",
      createdAt = OffsetDateTime.now(),
    )

    val person = personRepository.saveAndFlush(personDetails)
    val communityServiceProvider =
      communityServiceProviderRepository.findById(UUID.fromString("bc852b9d-1997-4ce4-ba7f-cd1759e15d2b")).get()

    val createReferralRequest = CreateReferralRequest(
      personId = person.id,
      communityServiceProviderId = communityServiceProvider.id,
      crn = "X123456",
    )

    val result = referralService.createReferral("testUser", createReferralRequest)
    val savedReferral = result.referral

    return savedReferral
  }

  private fun setupUser(userName: String): ReferralUser {
    val user = referralUserRepository.findByEmailAddressIgnoreCase(userName)
      ?: referralUserRepository.saveAndFlush(
        ReferralUser(
          id = UUID.randomUUID(),
          hmppsAuthId = "hmppsAuthId",
          hmppsAuthUsername = userName,
          authSource = AuthSource.AUTH.source,
          fullName = "Victoria Smith",
          emailAddress = userName,
        ),
      )
    return user
  }

  private fun setupAssigner(assigner: ReferralUser): ReferralUser = referralUserRepository.saveAndFlush(assigner)
}
