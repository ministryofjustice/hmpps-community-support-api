package uk.gov.justice.digital.hmpps.communitysupportapi.integration

import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.communitysupportapi.authorization.UserMapper
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.CommunityServiceProvider
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUserAssignment
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ServiceProvider
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.CommunityServiceProviderRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralProviderAssignmentRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralUserAssignmentRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralUserRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ServiceProviderRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.PersonFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ReferralFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ReferralProviderAssignmentFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ReferralUserFactory
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import java.time.OffsetDateTime
import java.util.UUID

class CaseListTestFixture(
  private val personRepository: PersonRepository,
  private val referralRepository: ReferralRepository,
  private val referralProviderAssignmentRepository: ReferralProviderAssignmentRepository,
  private val referralUserRepository: ReferralUserRepository,
  private val referralUserAssignmentRepository: ReferralUserAssignmentRepository,
  private val serviceProviderRepository: ServiceProviderRepository,
  private val communityServiceProviderRepository: CommunityServiceProviderRepository,
  private val userMapper: UserMapper,
) {
  lateinit var serviceProvider: ServiceProvider
    private set

  lateinit var communityServiceProvider: CommunityServiceProvider
    private set

  fun initialiseProviders() {
    serviceProvider = serviceProviderRepository.findAll().first()
    communityServiceProvider = communityServiceProviderRepository.findAll().first {
      it.serviceProvider.id == serviceProvider.id
    }
  }

  fun createTestUser(username: String = "test-user"): ReferralUser {
    val userId = UUID.randomUUID()
    val hmppsAuthId = UUID.randomUUID().toString()
    val testUser = ReferralUserFactory()
      .withId(userId)
      .withHmppsAuthId(hmppsAuthId)
      .withHmppsAuthUsername(username)
      .withAuthSource("auth")
      .create()

    ensureReferralUser(userId, hmppsAuthId, testUser.hmppsAuthUsername)

    whenever(userMapper.fromToken(org.mockito.kotlin.any<HmppsAuthenticationHolder>())).thenReturn(testUser)
    return testUser
  }

  private fun ensureReferralUser(id: UUID, hmppsAuthId: String, username: String) {
    if (!referralUserRepository.existsById(id)) {
      referralUserRepository.save(
        ReferralUserFactory()
          .withId(id)
          .withHmppsAuthId(hmppsAuthId)
          .withHmppsAuthUsername(username)
          .withAuthSource("auth")
          .create(),
      )
    }
  }

  fun createReferral(
    person: Person,
    referenceNumber: String,
    submittedBy: ReferralUser,
    createdAt: OffsetDateTime = OffsetDateTime.now(),
  ): Referral = referralRepository.save(
    ReferralFactory()
      .withPersonId(person.id)
      .withCrn(person.identifier)
      .withReferenceNumber(referenceNumber)
      .withCreatedAt(createdAt)
      .withSubmittedEvent(actorId = submittedBy.id, createdAt = createdAt)
      .create(),
  )

  fun createPerson(
    firstName: String,
    lastName: String,
    crn: String,
  ): Person = personRepository.save(
    PersonFactory()
      .withFirstName(firstName)
      .withLastName(lastName)
      .withIdentifier(crn)
      .create(),
  )

  fun createPersons(
    count: Int = 3,
    firstNamePrefix: String = "FirstName",
    lastNamePrefix: String = "LastName",
    crnPrefix: String = "CRN0000",
  ): List<Person> = (1..count).map { index ->
    createPerson(
      firstName = "$firstNamePrefix$index",
      lastName = "$lastNamePrefix$index",
      crn = "$crnPrefix$index",
    )
  }

  fun createCaseWorkers(vararg names: String): List<ReferralUser> = names.map { name ->
    referralUserRepository.save(
      ReferralUserFactory()
        .withFullName(name)
        .create(),
    )
  }

  fun assignCaseWorkers(referral: Referral, caseWorkers: List<ReferralUser>) {
    referralUserAssignmentRepository.saveAll(
      caseWorkers.map {
        ReferralUserAssignment(referral = referral, user = it)
      },
    )
  }

  fun assignToCommunityServiceProvider(referral: Referral) {
    referralProviderAssignmentRepository.save(
      ReferralProviderAssignmentFactory()
        .withReferral(referral)
        .withCommunityServiceProvider(communityServiceProvider)
        .create(),
    )
  }

  fun createInProgressReferral(
    person: Person,
    referenceNumber: String,
    submittedBy: ReferralUser,
    caseWorkers: List<ReferralUser>,
    createdAt: OffsetDateTime,
  ): Referral {
    val referral = createReferral(
      person,
      referenceNumber,
      submittedBy,
      createdAt,
    )
    assignToCommunityServiceProvider(referral)
    assignCaseWorkers(referral, caseWorkers)
    return referral
  }
}
