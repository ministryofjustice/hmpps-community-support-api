package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.PersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActorType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralEventType
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.model.CreateReferralRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.CommunityServiceProviderRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralProviderAssignmentRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralUserRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.PersonAdditionalDetailsFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.PersonFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ReferralUserFactory
import java.time.LocalDate
import java.util.UUID

class ReferralServiceIntegrationTest : IntegrationTestBase() {

  private val testUserId: UUID = UUID.randomUUID()

  @Autowired
  private lateinit var communityServiceProviderRepository: CommunityServiceProviderRepository

  @Autowired
  private lateinit var referralProviderAssignmentRepository: ReferralProviderAssignmentRepository

  @Autowired
  private lateinit var personRepository: PersonRepository

  @Autowired
  private lateinit var referralService: ReferralService

  @Autowired
  private lateinit var referralUserRepository: ReferralUserRepository

  @Test
  fun `createReferral should save referral and referral events`() {
    ensureTestUser()
    // Given
    val createReferralRequest = setUpData()

    val result = referralService.createReferral(testUserId, createReferralRequest)
    val savedReferral = result.referral

    assertThat(savedReferral.personId).isEqualTo(result.person.id)
    assertThat(savedReferral.crn).isEqualTo(createReferralRequest.crn)
    assertThat(savedReferral.referralEvents.size).isEqualTo(1)
    assertThat(savedReferral.referenceNumber).isNull()

    // Check provider assignment was created
    val providerAssignments = referralProviderAssignmentRepository.findByReferralId(savedReferral.id)
    assertThat(providerAssignments).hasSize(1)
    assertThat(providerAssignments[0].communityServiceProvider.id).isEqualTo(createReferralRequest.communityServiceProviderId)

    val createdEvent = savedReferral.referralEvents.first { it.eventType == ReferralEventType.CREATED }
    assertThat(createdEvent).isNotNull
    assertThat(createdEvent.actorType).isEqualTo(ActorType.AUTH)
    assertThat(createdEvent.actorId).isEqualTo(testUserId)
  }

  @Test
  fun `createReferral should update existing person when identifier matches`() {
    ensureTestUser()
    val existingPerson = personRepository.save(
      PersonFactory()
        .withFirstName("Old")
        .withLastName("Name")
        .withIdentifier("X123456")
        .withDateOfBirth(LocalDate.of(1970, 1, 1))
        .withGender("Male")
        .create(),
    )

    val communityServiceProvider = communityServiceProviderRepository.findById(UUID.fromString("bc852b9d-1997-4ce4-ba7f-cd1759e15d2b")).get()

    val updatedPersonDto = PersonDto(
      id = UUID.randomUUID(),
      personIdentifier = "X123456",
      firstName = "John",
      lastName = "Smith",
      dateOfBirth = LocalDate.of(1980, 1, 1),
      sex = "Male",
      additionalDetails = null,
    )

    val request = CreateReferralRequest(
      personDetails = updatedPersonDto,
      communityServiceProviderId = communityServiceProvider.id,
      crn = "X123456",
    )

    val result = referralService.createReferral(testUserId, request)

    val persistedPerson = personRepository.findById(existingPerson.id).get()

    assertThat(result.referral.personId).isEqualTo(existingPerson.id)
    assertThat(persistedPerson.firstName).isEqualTo(updatedPersonDto.firstName)
    assertThat(persistedPerson.lastName).isEqualTo(updatedPersonDto.lastName)
    assertThat(persistedPerson.dateOfBirth).isEqualTo(updatedPersonDto.dateOfBirth)
  }

  @Test
  fun `createReferral should update existing person additional details when identifier matches`() {
    ensureTestUser()
    val existingPerson = personRepository.save(
      PersonFactory()
        .withFirstName("Old")
        .withLastName("Name")
        .withIdentifier("X999999")
        .withDateOfBirth(LocalDate.of(1975, 5, 5))
        .withGender("Male")
        .create(),
    )
    val existingDetails = PersonAdditionalDetailsFactory()
      .withPerson(existingPerson)
      .withEthnicity("OldEthnicity")
      .withPreferredLanguage("OldLang")
      .withSexualOrientation("OldOrientation")
      .create()
    existingPerson.additionalDetails = existingDetails
    personRepository.save(existingPerson)

    val communityServiceProvider = communityServiceProviderRepository.findById(UUID.fromString("bc852b9d-1997-4ce4-ba7f-cd1759e15d2b")).get()

    val updatedPersonDto = PersonDto(
      id = UUID.randomUUID(),
      personIdentifier = "X999999",
      firstName = "NewFirst",
      lastName = "NewLast",
      dateOfBirth = LocalDate.of(1985, 6, 6),
      sex = "Male",
      additionalDetails = uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonAdditionalDetails(
        ethnicity = "NewEthnicity",
        preferredLanguage = "NewLang",
        sexualOrientation = "NewOrientation",
      ),
    )

    val request = CreateReferralRequest(
      personDetails = updatedPersonDto,
      communityServiceProviderId = communityServiceProvider.id,
      crn = "X999999",
    )

    referralService.createReferral(testUserId, request)

    val persistedPerson = personRepository.findById(existingPerson.id).get()

    assertThat(persistedPerson.firstName).isEqualTo(updatedPersonDto.firstName)
    assertThat(persistedPerson.lastName).isEqualTo(updatedPersonDto.lastName)
    assertThat(persistedPerson.dateOfBirth).isEqualTo(updatedPersonDto.dateOfBirth)
    assertThat(persistedPerson.additionalDetails?.ethnicity).isEqualTo("NewEthnicity")
    assertThat(persistedPerson.additionalDetails?.preferredLanguage).isEqualTo("NewLang")
    assertThat(persistedPerson.additionalDetails?.sexualOrientation).isEqualTo("NewOrientation")
    assertThat(persistedPerson.additionalDetails?.id).isEqualTo(existingDetails.id)
  }

  private fun setUpData(): CreateReferralRequest {
    val communityServiceProvider = communityServiceProviderRepository.findById(UUID.fromString("bc852b9d-1997-4ce4-ba7f-cd1759e15d2b")).get()

    val personDto = PersonDto(
      id = UUID.randomUUID(),
      personIdentifier = "X123456",
      firstName = "John",
      lastName = "Smith",
      dateOfBirth = LocalDate.of(1980, 1, 1),
      sex = "Male",
      additionalDetails = null,
    )

    return CreateReferralRequest(
      personDetails = personDto,
      communityServiceProviderId = communityServiceProvider.id,
      crn = "X123456",
    )
  }

  private fun ensureTestUser() {
    if (!referralUserRepository.existsById(testUserId)) {
      referralUserRepository.save(
        ReferralUserFactory()
          .withId(testUserId)
          .withHmppsAuthUsername("test-user")
          .withHmppsAuthId("test-auth-id")
          .withAuthSource("auth")
          .create(),
      )
    }
  }
}
