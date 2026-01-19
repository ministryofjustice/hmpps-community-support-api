package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.model.CreateReferralRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.CommunityServiceProviderRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import java.time.LocalDate
import java.time.OffsetDateTime
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

    val result = referralService.createReferral("testUser", createReferralRequest)
    val savedReferral = result.referral

    assertThat(savedReferral.personId).isEqualTo(createReferralRequest.personId)
    assertThat(savedReferral.communityServiceProviderId).isEqualTo(createReferralRequest.communityServiceProviderId)
    assertThat(savedReferral.crn).isEqualTo(createReferralRequest.crn)
    assertThat(savedReferral.referralEvents.size).isEqualTo(1)
    assertThat(savedReferral.referenceNumber).isNotNull

    val submittedEvent = savedReferral.submittedEvent
    assertThat(submittedEvent).isNotNull
    assertThat(submittedEvent?.eventType).isEqualTo("SUBMITTED")
    assertThat(submittedEvent?.actorType).isEqualTo("AUTH")
    assertThat(submittedEvent?.actorId).isEqualTo("testUser")
  }

  private fun setUpData(): CreateReferralRequest {
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
    val communityServiceProvider = communityServiceProviderRepository.findById(UUID.fromString("bc852b9d-1997-4ce4-ba7f-cd1759e15d2b")).get()

    return CreateReferralRequest(
      personId = person.id,
      communityServiceProviderId = communityServiceProvider.id,
      crn = "X123456",
    )
  }
}
