package uk.gov.justice.digital.hmpps.communitysupportapi.controller

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CheckReferralInformationRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CreateReferralRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ReferralInformationDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.CommunityServiceProviderRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralRepository
import java.time.LocalDateTime
import java.util.*

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
        .uri("/referrals/bc852b9d-1997-4ce4-ba7f-cd1759e15d2b")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri("/referrals/bc852b9d-1997-4ce4-ba7f-cd1759e15d2b")
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
        .uri("/referrals/bc852b9d-1997-4ce4-ba7f-cd1759e15d2b")
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
        createdAt = LocalDateTime.now(),
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

      referralRepository.save(referral)

      val referralDto = ReferralDto(
        id = referral.id,
        crn = referral.crn,
        referenceNumber = referral.referenceNumber,
      )

      webTestClient.get()
        .uri("/referrals/${referral.id}")
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
        .uri("/referrals/bc852b9d-1997-4ce4-ba7f-cd1759e15d2b")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isNotFound
    }
  }

  @Nested
  @DisplayName("POST /check-referral-information")
  inner class CheckReferralInformationEndPoint {

    @BeforeEach
    fun setup() {
      testDataCleaner.cleanAllTables()
    }

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.post()
        .uri("/check-referral-information")
        .bodyValue(setUpData())
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.post()
        .uri("/check-referral-information")
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
        .uri("/check-referral-information")
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .bodyValue(setUpData())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return OK with valid referral information`() {
      val referralInformationDto = ReferralInformationDto(
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

      webTestClient.post()
        .uri("/check-referral-information")
        .headers(setAuthorisation())
        .bodyValue(setUpData())
        .exchange()
        .expectStatus()
        .isOk
        .expectBody(ReferralInformationDto::class.java)
        .consumeWith { response ->
          response.responseBody shouldBe referralInformationDto
        }
    }

    @Test
    fun `should return Not Found with invalid person identifier`() {
      webTestClient.post()
        .uri("/check-referral-information")
        .headers(setAuthorisation())
        .bodyValue(
          CheckReferralInformationRequest(
            personId = "bc852b9d-1997-4ce4-ba7f-cd1759e15d2b".let { UUID.fromString(it) },
            communityServiceProviderId = "bc852b9d-1997-4ce4-ba7f-cd1759e15d2b".let { UUID.fromString(it) },
            crn = "X123456",
          ),
        )
        .exchange()
        .expectStatus()
        .isNotFound
    }

    private fun setUpData(): CheckReferralInformationRequest {
      val personDetails = Person(
        id = UUID.fromString("bc852b9d-1997-4ce4-ba7f-cd1759e15d2c"),
        firstName = "John",
        lastName = "Smith",
        createdAt = LocalDateTime.now(),
        sex = "Male",
      )

      val person = personRepository.save(personDetails)
      val communityServiceProvider = communityServiceProviderRepository.findById(UUID.fromString("bc852b9d-1997-4ce4-ba7f-cd1759e15d2b")).get()

      return CheckReferralInformationRequest(
        personId = person.id,
        communityServiceProviderId = communityServiceProvider.id,
        crn = "X123456",
      )
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
        .uri("/referrals")
        .bodyValue(setUpData())
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.post()
        .uri("/referrals")
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
        .uri("/referrals")
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .bodyValue(setUpData())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return OK with valid referral information`() {
      webTestClient.post()
        .uri("/referrals")
        .headers(setAuthorisation())
        .bodyValue(setUpData())
        .exchange()
        .expectStatus()
        .isOk
        .expectBody(ReferralDto::class.java)
        .consumeWith { response ->
          run {
            val referral = referralRepository.findAll().firstOrNull()

            val referralDto = ReferralDto(
              id = referral!!.id,
              crn = referral.crn,
              referenceNumber = referral.referenceNumber,
            )
            response.responseBody shouldBe referralDto
          }
        }
    }

    @Test
    fun `should return Not Found with invalid referral identifier`() {
      webTestClient.post()
        .uri("/referrals/")
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
        id = UUID.fromString("bc852b9d-1997-4ce4-ba7f-cd1759e15d2c"),
        firstName = "John",
        lastName = "Smith",
        createdAt = LocalDateTime.now(),
        sex = "Male",
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
