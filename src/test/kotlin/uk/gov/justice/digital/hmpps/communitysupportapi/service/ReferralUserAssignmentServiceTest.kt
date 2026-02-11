package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CaseWorkerDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.UserType
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.CommunityServiceProviderRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralUserAssignmentRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralUserRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.PersonFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ReferralFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ReferralUserFactory
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
    val assigner: ReferralUser = setupAssigner()
    val referral: Referral = setUpReferral(assigner.id)
    val user: ReferralUser = setupUser("victoriasmith@email.com")

    val emailsList = listOf("victoriasmith@email.com")

    val caseWorkers = emailsList
      .map { email ->
        CaseWorkerDto(userType = UserType.EXTERNAL, emailAddress = email.trim().lowercase())
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
    val assigner: ReferralUser = setupAssigner()
    val referral: Referral = setUpReferral(assignerId = assigner.id)
    val emailsList = listOf("alexsmith@email.com")

    val caseWorkers = emailsList
      .map { email ->
        CaseWorkerDto(userType = UserType.EXTERNAL, emailAddress = email.trim().lowercase())
      }

    val result = referralAssignmentService.assignCaseWorkers(assigner, referral.id, caseWorkers)
    assertThat(result?.success).isFalse()
    assertThat(result?.message).isEqualTo("Failed to assign case worker(s)")
    assertThat(result?.succeededList).isEmpty()
    assertThat(result?.failureList?.get(0)?.emailAddress).isEqualTo("alexsmith@email.com")
    assertThat(result?.failureList?.get(0)?.reason).isEqualTo("Could not find a caseworker with that email address.")
  }

  @Test
  fun `getAssignedCaseWorkers should return assigned case workers`() {
    val assigner: ReferralUser = setupAssigner()
    val referral: Referral = setUpReferral(assigner.id)
    val user1: ReferralUser = setupUser("assigntestuser1@email.com")
    val user2: ReferralUser = setupUser("assigntestuser2@email.com")

    val emailsList = listOf("assigntestuser1@email.com", "assigntestuser2@email.com")

    val caseWorkers = emailsList
      .map { email ->
        CaseWorkerDto(userType = UserType.EXTERNAL, emailAddress = email.trim().lowercase())
      }

    val result = referralAssignmentService.assignCaseWorkers(assigner, referral.id, caseWorkers)
    val assignedCaseWorkers = referralAssignmentService.getAssignedCaseWorkers(referral.id)

    assertThat(assignedCaseWorkers?.size).isEqualTo(2)
    assertThat(assignedCaseWorkers?.get(0)?.emailAddress).isEqualTo(user1.hmppsAuthUsername)
    assertThat(assignedCaseWorkers?.get(1)?.emailAddress).isEqualTo(user2.hmppsAuthUsername)
  }

  private fun setUpReferral(assignerId: UUID): Referral {
    val person = personRepository.save(
      PersonFactory()
        .withFirstName("John")
        .withLastName("Doe")
        .withIdentifier("CRN12345")
        .create(),
    )

    val savedReferral = referralRepository.save(
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
          .withHmppsAuthUsername(userName)
          .withFullName("Victoria Smith")
          .create(),
      )
    return user
  }

  private fun setupAssigner(): ReferralUser {
    val userName = "testassigner@email.com"
    val user = referralUserRepository.findByHmppsAuthUsernameIgnoreCase(userName)
      ?: referralUserRepository.saveAndFlush(
        ReferralUserFactory()
          .withHmppsAuthId(UUID.randomUUID().toString())
          .withHmppsAuthUsername(userName)
          .withFullName("Test User")
          .create(),
      )
    return user
  }
}
