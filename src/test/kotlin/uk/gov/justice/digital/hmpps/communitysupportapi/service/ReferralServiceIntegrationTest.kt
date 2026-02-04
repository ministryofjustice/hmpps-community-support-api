package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActorType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralEventType
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.model.CreateReferralRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.CommunityServiceProviderRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralProviderAssignmentRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.PersonFactory
import java.util.UUID

class ReferralServiceIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @Autowired
  private lateinit var personRepository: PersonRepository

  @Autowired
  private lateinit var communityServiceProviderRepository: CommunityServiceProviderRepository

  @Autowired
  private lateinit var referralProviderAssignmentRepository: ReferralProviderAssignmentRepository

  @Autowired
  private lateinit var referralService: ReferralService

  @Test
  fun `createReferral should save referral and referral events`() {
    // Given
    val createReferralRequest = setUpData()

    val result = referralService.createReferral("testUser", createReferralRequest)
    val savedReferral = result.referral

    assertThat(savedReferral.personId).isEqualTo(createReferralRequest.personId)
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
    assertThat(createdEvent.actorId).isEqualTo("testUser")
  }

  private fun setUpData(): CreateReferralRequest {
    val person = personRepository.saveAndFlush(
      PersonFactory()
        .withFirstName("John")
        .withLastName("Smith")
        .withIdentifier("X123456")
        .create(),
    )
    val communityServiceProvider = communityServiceProviderRepository.findById(UUID.fromString("bc852b9d-1997-4ce4-ba7f-cd1759e15d2b")).get()

    return CreateReferralRequest(
      personId = person.id,
      communityServiceProviderId = communityServiceProvider.id,
      crn = "X123456",
    )
  }
}
