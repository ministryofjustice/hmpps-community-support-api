package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CaseWorkerDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.UserType
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.model.CreateReferralRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.CommunityServiceProviderRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralUserAssignmentRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralUserRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.service.ReferralService
import uk.gov.justice.hmpps.kotlin.auth.AuthSource
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

class ReferralUserAssignmentServiceTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @Autowired
  private lateinit var personRepository: PersonRepository

  @Autowired
  private lateinit var communityServiceProviderRepository: CommunityServiceProviderRepository

  @Autowired
  private lateinit var referralUserAssignmentRepository: ReferralUserAssignmentRepository

  @Autowired
  private lateinit var referralUserRepository: ReferralUserRepository

  @Autowired
  private lateinit var referralService: ReferralService

  @Autowired
  private lateinit var referralAssignmentService: ReferralAssignmentService

  @Test
  fun `assignCaseWorker should save case worker assignments`() {
    val referral: Referral = setUpReferral()
    val assigner: ReferralUser = setupAssigner()
    val user = setupUser("victoriasmith@email.com")

    val emailsList = listOf("victoriasmith@email.com")

    val caseWorkers = emailsList
      .map { email ->
        CaseWorkerDto(userType = UserType.UNDEFINED, emailAddress = email.trim().lowercase())
      }

    val result = referralAssignmentService.assignCaseWorkers(assigner, referral.id, caseWorkers)
    assertThat(result?.success).isTrue()
    assertThat(result?.message).isEqualTo("The case has been assigned to a caseworker.")
    assertThat(result?.succeededList?.size).isEqualTo(1)
    assertThat(result?.succeededList?.get(0)?.emailAddress).isEqualTo("victoriasmith@email.com")
    assertThat(result?.failureList).isEmpty()
  }

  @Test
  fun `assignCaseWorker that could not find a caseworker with that email address`() {
    val referral: Referral = setUpReferral()
    val assigner: ReferralUser = setupAssigner()
    val emailsList = listOf("alexsmith@email.com")

    val caseWorkers = emailsList
      .map { email ->
        CaseWorkerDto(userType = UserType.UNDEFINED, emailAddress = email.trim().lowercase())
      }

    val result = referralAssignmentService.assignCaseWorkers(assigner, referral.id, caseWorkers)
    assertThat(result?.success).isFalse()
    assertThat(result?.message).isEqualTo("Failed to assign case worker(s)")
    assertThat(result?.succeededList).isEmpty()
    assertThat(result?.failureList?.get(0)?.emailAddress).isEqualTo("alexsmith@email.com")
    assertThat(result?.failureList?.get(0)?.reason).isEqualTo("Could not find a caseworker with that email address.")
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

  private fun setupAssigner(): ReferralUser {
    val user = referralUserRepository.findByEmailAddressIgnoreCase("testassigner@email.com")
      ?: referralUserRepository.saveAndFlush(
        ReferralUser(
          id = UUID.randomUUID(),
          hmppsAuthId = "hmppsAuthId",
          hmppsAuthUsername = "testassigner@email.com",
          authSource = AuthSource.AUTH.source,
          fullName = "Test Assigner",
          emailAddress = "testassigner@email.com",
        ),
      )
    return user
  }
}
