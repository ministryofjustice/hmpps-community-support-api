package uk.gov.justice.digital.hmpps.communitysupportapi.integration

import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.communitysupportapi.authorization.UserMapper
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.CommunityServiceProvider
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.PersonAdditionalDetails
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ServiceProvider
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.CommunityServiceProviderRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralProviderAssignmentRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralUserAssignmentRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralUserRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ServiceProviderRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.PersonAdditionalDetailsFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.PersonFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ReferralFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ReferralProviderAssignmentFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ReferralUserAssignmentFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ReferralUserFactory
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@Component
class ReferralTestSupport(
  private val personRepository: PersonRepository,
  private val referralRepository: ReferralRepository,
  private val referralProviderAssignmentRepository: ReferralProviderAssignmentRepository,
  private val referralUserRepository: ReferralUserRepository,
  private val referralUserAssignmentRepository: ReferralUserAssignmentRepository,
  private val serviceProviderRepository: ServiceProviderRepository,
  private val communityServiceProviderRepository: CommunityServiceProviderRepository,
  private val userMapper: UserMapper,
) {
  val communityServiceProviderId: UUID = UUID.fromString("bc852b9d-1997-4ce4-ba7f-cd1759e15d2b")

  fun getCommunityServiceProvider(): CommunityServiceProvider = communityServiceProviderRepository.findById(communityServiceProviderId).get()

  fun getProviders(): Pair<ServiceProvider, CommunityServiceProvider> {
    val serviceProvider = serviceProviderRepository.findAll().first()
    val communityServiceProvider =
      communityServiceProviderRepository.findAll().first {
        it.serviceProvider.id == serviceProvider.id
      }

    return serviceProvider to communityServiceProvider
  }

  fun createTestUser(
    id: UUID = UUID.randomUUID(),
    hmppsAuthId: UUID = UUID.randomUUID(),
    username: String = "test-user",
  ): ReferralUser {
    val testUser = createReferralUser(userId = id, hmppsAuthId = hmppsAuthId.toString(), username = username)

    whenever(userMapper.fromToken(any<HmppsAuthenticationHolder>()))
      .thenReturn(testUser)

    return testUser
  }

  fun createReferralUser(
    userId: UUID = UUID.randomUUID(),
    hmppsAuthId: String = "test-auth-id",
    username: String = "test-user",
    fullName: String = "fullname",
    authSource: String = "auth",
  ): ReferralUser = referralUserRepository.findById(userId).orElseGet {
    referralUserRepository.save(
      ReferralUserFactory()
        .withId(userId)
        .withHmppsAuthId(hmppsAuthId)
        .withHmppsAuthUsername(username)
        .withFullName(fullName)
        .withAuthSource(authSource)
        .create(),
    )
  }

  fun createReferral(
    person: Person,
    referenceNumber: String = "REF-001",
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
    firstName: String = "John",
    lastName: String = "Smith",
    identifier: String = "X123456",
    dateOfBirth: LocalDate = LocalDate.of(1980, 1, 1),
    gender: String = "Male",
  ): Person = personRepository.save(
    PersonFactory()
      .withFirstName(firstName)
      .withLastName(lastName)
      .withIdentifier(identifier)
      .withDateOfBirth(dateOfBirth)
      .withGender(gender)
      .withCreatedAt(OffsetDateTime.now())
      .create(),
  )

  fun createPersonAdditionalDetails(
    person: Person,
    ethnicity: String = "OldEthnicity",
    preferredLanguage: String = "OldLang",
    sexualOrientation: String = "OldOrientation",
  ): PersonAdditionalDetails = PersonAdditionalDetailsFactory()
    .withPerson(person)
    .withEthnicity(ethnicity)
    .withPreferredLanguage(preferredLanguage)
    .withSexualOrientation(sexualOrientation)
    .create()

  fun createPersons(
    count: Int = 3,
    firstNamePrefix: String = "FirstName",
    lastNamePrefix: String = "LastName",
    crnPrefix: String = "CRN0000",
  ): List<Person> = (1..count).map { index ->
    createPerson(
      firstName = "$firstNamePrefix$index",
      lastName = "$lastNamePrefix$index",
      identifier = "$crnPrefix$index",
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
    caseWorkers.forEach {
      referralUserAssignmentRepository.save(
        ReferralUserAssignmentFactory()
          .withReferral(referral)
          .withUser(it)
          .create(),
      )
    }
  }

  fun assignToCommunityServiceProvider(referral: Referral, communityServiceProvider: CommunityServiceProvider) {
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
    communityServiceProvider: CommunityServiceProvider,
    createdAt: OffsetDateTime,
  ): Referral {
    val referral = createReferral(person, referenceNumber, submittedBy, createdAt)
    assignToCommunityServiceProvider(referral, communityServiceProvider = communityServiceProvider)
    assignCaseWorkers(referral, caseWorkers)

    return referral
  }
}
