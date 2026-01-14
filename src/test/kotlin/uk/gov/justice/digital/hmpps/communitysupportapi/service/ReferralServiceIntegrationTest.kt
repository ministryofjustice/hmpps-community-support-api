package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralInformationDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.CommunityServiceProviderRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import java.time.LocalDateTime
import java.util.UUID

class ReferralServiceIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @Autowired
  private lateinit var personRepository: PersonRepository

  @Autowired
  private lateinit var communityServiceProviderRepository: CommunityServiceProviderRepository

  @Autowired
  private lateinit var referralService: ReferralService

  @BeforeEach
  fun setup() {
    testDataCleaner.cleanAllTables()
  }

  @Test
  fun `createReferral should save referral and referral events`() {
    // Given
    val createReferralRequest = setUpData()

    val savedReferral = referralService.createReferral("testUser", createReferralRequest)
    assertThat(savedReferral.personId).isEqualTo(createReferralRequest.personId)
    assertThat(savedReferral.communityServiceProviderId).isEqualTo(createReferralRequest.communityServiceProviderId)
    assertThat(savedReferral.crn).isEqualTo(createReferralRequest.crn)
    assertThat(savedReferral.referralEvents.size).isEqualTo(1)
    assertThat(savedReferral.referenceNumber).isNotNull

    val submittedEvent = savedReferral.submittedEvent
    assertThat(submittedEvent).isNotNull
    assertThat(submittedEvent?.eventType).isEqualTo("SUBMITTED")
    assertThat(submittedEvent?.actorType).isEqualTo("auth")
    assertThat(submittedEvent?.actorId).isEqualTo("testUser")
  }

  @Test
  fun `check all referral information should return the required referral information`() {
    // Given
    val checkAllInformationRequest = setUpData()

    val expectedReferralInformationDto = ReferralInformationDto(
      personId = UUID.fromString("bc852b9d-1997-4ce4-ba7f-cd1759e15d2c"),
      firstName = "John",
      lastName = "Smith",
      communityServiceProviderId = UUID.fromString("bc852b9d-1997-4ce4-ba7f-cd1759e15d2b"),
      communityServiceProviderName = "Community Support Service in Cleveland",
      crn = "X123456",
      sex = "Male",
      region = "North East",
      deliveryPartner = "Access 2 Advice",
    )

    val referralInformationDto = referralService.checkReferralInformation(checkAllInformationRequest)
    assertThat(referralInformationDto).isEqualTo(expectedReferralInformationDto)
  }

  @Test
  fun `check all referral information throws exception where person is not present`() {
    // Given
    val checkAllInformationRequest = ReferralRequest(
      personId = UUID.fromString("bc852b9d-1997-4ce4-ba7f-cd1759e15d2a"),
      communityServiceProviderId = UUID.fromString("bc852b9d-1997-4ce4-ba7f-cd1759e15d2c"),
      crn = "X123456",
    )

    val exception = assertThrows<NotFoundException> {
      referralService.checkReferralInformation(checkAllInformationRequest)
    }

    assertThat(exception).hasMessageContaining("Person not found for id bc852b9d-1997-4ce4-ba7f-cd1759e15d2a")
  }

  @Test
  fun `check all referral information throws exception where community service provider is not present`() {
    // Given
    val personDetails = Person(
      id = UUID.fromString("bc852b9d-1997-4ce4-ba7f-cd1759e15d2c"),
      firstName = "John",
      lastName = "Smith",
      createdAt = LocalDateTime.now(),
      sex = "Male",
    )

    val person = personRepository.save(personDetails)

    val checkAllInformationRequest = ReferralRequest(
      personId = person.id,
      communityServiceProviderId = UUID.fromString("bc852b9d-1997-4ce4-ba7f-cd1759e15d2a"),
      crn = "X123456",
    )

    val exception = assertThrows<NotFoundException> {
      referralService.checkReferralInformation(checkAllInformationRequest)
    }

    assertThat(exception).hasMessageContaining("Community Service Provider not found for id bc852b9d-1997-4ce4-ba7f-cd1759e15d2a")
  }

  private fun setUpData(): ReferralRequest {
    val personDetails = Person(
      id = UUID.fromString("bc852b9d-1997-4ce4-ba7f-cd1759e15d2c"),
      firstName = "John",
      lastName = "Smith",
      createdAt = LocalDateTime.now(),
      sex = "Male",
    )

    val person = personRepository.save(personDetails)
    val communityServiceProvider = communityServiceProviderRepository.findById(UUID.fromString("bc852b9d-1997-4ce4-ba7f-cd1759e15d2b")).get()

    return ReferralRequest(
      personId = person.id,
      communityServiceProviderId = communityServiceProvider.id,
      crn = "X123456",
    )
  }
}
