package uk.gov.justice.digital.hmpps.communitysupportapi.controller

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralInformationDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.model.CreateReferralRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.CommunityServiceProviderRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.UUID

class ReferralControllerIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  @Autowired
  private lateinit var personRepository: PersonRepository

  @Autowired
  private lateinit var communityServiceProviderRepository: CommunityServiceProviderRepository

  @Nested
  @DisplayName("GET /referrals/{referralId}")
  inner class ReferralEndPoint {

    @BeforeEach
    fun setup() {
      testDataCleaner.cleanAllTables()
    }

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri("/bff/referrals/bc852b9d-1997-4ce4-ba7f-cd1759e15d2b")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri("/bff/referrals/bc852b9d-1997-4ce4-ba7f-cd1759e15d2b")
        .headers(
          setAuthorisation(
            "AUTH_ADM",
            listOf(),
            listOf("read"),
          ),
        )
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri("/bff/referrals/bc852b9d-1997-4ce4-ba7f-cd1759e15d2b")
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return OK with valid referral information`() {
      // given some referral data are there
      val personDetails = Person(
        id = UUID.randomUUID(),
        firstName = "John",
        lastName = "Smith",
        identifier = "X123456",
        dateOfBirth = LocalDate.of(1980, 1, 1),
        gender = "Male",
        createdAt = OffsetDateTime.now(),
      )

      val person = personRepository.save(personDetails)
      val communityServiceProvider = communityServiceProviderRepository.findById(UUID.fromString("bc852b9d-1997-4ce4-ba7f-cd1759e15d2b")).get()

      val referral = Referral(
        id = UUID.randomUUID(),
        personId = person.id,
        communityServiceProviderId = communityServiceProvider.id,
        crn = "X123456",
        referenceNumber = "REF123456",
        createdAt = LocalDateTime.now(),
      )

      val savedReferral = referralRepository.save(referral)

      val referralDto = ReferralDto(
        id = referral.id!!,
        crn = referral.crn,
        referenceNumber = referral.referenceNumber,
      )

      webTestClient.get()
        .uri("/bff/referrals/${savedReferral.id}")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isOk
        .expectBody(ReferralDto::class.java)
        .consumeWith { response ->
          response.responseBody shouldBe referralDto
        }
    }

    @Test
    fun `should return Not Found with invalid referral identifier`() {
      webTestClient.get()
        .uri("/bff/referrals/bc852b9d-1997-4ce4-ba7f-cd1759e15d2b")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isNotFound
    }
  }

  @Nested
  @DisplayName("POST /referrals")
  inner class CreateReferrals {

    @BeforeEach
    fun setup() {
      testDataCleaner.cleanAllTables()
    }

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.post()
        .uri("/bff/referrals")
        .bodyValue(setUpData())
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.post()
        .uri("/bff/referrals")
        .headers(
          setAuthorisation(
            "AUTH_ADM",
            listOf(),
            listOf("read"),
          ),
        )
        .bodyValue(setUpData())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.post()
        .uri("/bff/referrals")
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .bodyValue(setUpData())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return OK with valid referral information`() {
      webTestClient.post()
        .uri("/bff/referrals")
        .headers(setAuthorisation())
        .bodyValue(setUpData())
        .exchange()
        .expectStatus()
        .isOk
        .expectBody(ReferralInformationDto::class.java)
        .consumeWith { response ->
          run {
            val referral = referralRepository.findAll().firstOrNull()!!
            val person = personRepository.findById(referral.personId).get()
            val communityServiceProvider = communityServiceProviderRepository.findById(referral.communityServiceProviderId).get()

            val referralInfo = ReferralInformationDto(
              personId = referral.personId,
              firstName = person.firstName,
              lastName = person.lastName,
              sex = person.additionalDetails?.sexualOrientation,
              crn = referral.crn,
              communityServiceProviderId = referral.communityServiceProviderId,
              communityServiceProviderName = communityServiceProvider.name,
              region = communityServiceProvider.contractArea.region.name,
              deliveryPartner = communityServiceProvider.providerName,
              referenceNumber = referral.referenceNumber,
            )
            response.responseBody shouldBe referralInfo
          }
        }
    }

    @Test
    fun `should return Not Found with invalid referral identifier`() {
      webTestClient.post()
        .uri("/bff/referrals/")
        .headers(setAuthorisation())
        .bodyValue(
          CreateReferralRequest(
            personId = "bc852b9d-1997-4ce4-ba7f-cd1759e15d2b".let { UUID.fromString(it) },
            communityServiceProviderId = "bc852b9d-1997-4ce4-ba7f-cd1759e15d2b".let { UUID.fromString(it) },
            crn = "X123456",
          ),
        )
        .exchange()
        .expectStatus()
        .isNotFound
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

      val person = personRepository.save(personDetails)
      val communityServiceProvider = communityServiceProviderRepository.findById(UUID.fromString("bc852b9d-1997-4ce4-ba7f-cd1759e15d2b")).get()

      return CreateReferralRequest(
        personId = person.id,
        communityServiceProviderId = communityServiceProvider.id,
        crn = "X123456",
      )
    }
  }
}
