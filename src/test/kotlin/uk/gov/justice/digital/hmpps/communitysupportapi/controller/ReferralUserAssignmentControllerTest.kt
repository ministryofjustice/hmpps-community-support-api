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
import org.springframework.core.ParameterizedTypeReference
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.communitysupportapi.authorization.UserMapper
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.AssignmentFailureDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CaseWorkerDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUserAssignment
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.UserType
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.model.AssignCaseWorkersRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.model.AssignCaseWorkersResult
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.CommunityServiceProviderRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralUserAssignmentRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralUserRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.service.ReferralService
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.PersonFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ReferralFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ReferralUserFactory
import uk.gov.justice.hmpps.kotlin.auth.AuthSource
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import java.time.LocalDateTime
import java.util.UUID

class ReferralUserAssignmentControllerTest : IntegrationTestBase() {

  private val testUserId: UUID = UUID.randomUUID()

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
    .withHmppsAuthId(UUID.randomUUID().toString())
    .withHmppsAuthUsername("testuser1@email.com")
    .withFullName("Test User 1")
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

      var assigner = setupAssigner(testUser)
      val referral = setUpReferral(assigner.id)

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
      val assigner = setupAssigner(testUser)
      whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(assigner)

      val referral: Referral = setUpReferral(assigner.id)
      val user: ReferralUser = setupUser("johnsmith@email.com")

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
                  emailAddress = user.hmppsAuthUsername.trim().lowercase(),
                ),
              ),
            )
            response.responseBody shouldBe submitReferralResponseDto
          }
        }
    }
  }

  @Test
  fun `should return OK and return assigned case worker(s)`() {
    whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>())).thenReturn(testUser)

    var assigner = setupAssigner(testUser)
    val referral = setUpReferral(assigner.id)
    val user1 = setupUser("assigntestuser1@email.com")
    val user2 = setupUser("assigntestuser2@email.com")

    val caseWorkers: List<CaseWorkerDto> = setupAssignments(referral, assigner, listOf(user1, user2))

    val response = webTestClient.get()
      .uri("/bff/referral-assignments/${referral.id}")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isOk
      .expectBody(object : ParameterizedTypeReference<List<CaseWorkerDto>>() {})
      .returnResult().responseBody!!

    assertThat(response).hasSize(2)
    response.forEach { caseWorkerDto ->
      assertThat(caseWorkerDto.userId).isNotNull()
      assertThat(caseWorkerDto.fullName).isNotBlank()
      assertThat(caseWorkerDto.emailAddress).startsWith("assigntestuser")
      assertThat(caseWorkerDto.userType).isNotNull()
    }
  }

  private fun setUpReferral(assignerId: UUID): Referral {
    val person = personRepository.save(
      PersonFactory()
        .withFirstName("John")
        .withLastName("Doe")
        .withIdentifier("CRN12345")
        .create(),
    )

    val savedReferral = referralRepository.saveAndFlush(
      ReferralFactory()
        .withPersonId(person.id)
        .withCrn(person.identifier)
        .withReferenceNumber("REF-001")
        .withSubmittedEvent(actorId = assignerId)
        .create(),
    )
    return savedReferral
  }

  private fun setupUser(userName: String): ReferralUser {
    val user = referralUserRepository.findByHmppsAuthUsernameIgnoreCase(userName)
      ?: referralUserRepository.saveAndFlush(
        ReferralUserFactory()
          .withHmppsAuthId(UUID.randomUUID().toString())
          .withHmppsAuthUsername(userName.trim().lowercase())
          .create(),
      )
    return user
  }

  private fun setupAssigner(assigner: ReferralUser): ReferralUser {
    val user = referralUserRepository.findByHmppsAuthUsernameIgnoreCase(assigner.hmppsAuthUsername)
      ?: referralUserRepository.saveAndFlush(assigner)
    return user
  }

  private fun setupAssignments(referral: Referral, assigner: ReferralUser, caseWorkers: List<ReferralUser>): List<CaseWorkerDto> {
    val results: List<CaseWorkerDto> = caseWorkers.map {
      referralUserAssignmentRepository.saveAndFlush(
        ReferralUserAssignment(
          UUID.randomUUID(),
          referral,
          it,
          LocalDateTime.now(),
          assigner,
        ),
      )
      CaseWorkerDto(
        if (it.authSource == AuthSource.AUTH.source) UserType.INTERNAL else UserType.EXTERNAL,
        it.id,
        it.fullName,
        it.hmppsAuthUsername.trim().lowercase(),
      )
    }

    return results
  }
}
