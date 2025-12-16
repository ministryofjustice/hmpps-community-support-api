package uk.gov.justice.digital.hmpps.communitysupportapi.integration

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.PersonDto

class PersonControllerIntegrationTest : IntegrationTestBase() {

  @Nested
  @DisplayName("GET /person/{personIdentifier}")
  inner class PersonEndpoint {

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri("/person/PERSONID")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri("/person/PERSONID")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri("/person/PERSONID")
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return OK with valid person identifier`() {
      val expectedPersonResult = PersonDto("PERSONID")
      webTestClient.get()
        .uri("/person/PERSONID")
        .headers(setAuthorisation(roles = listOf("ROLE_IPB_FRONTEND_RW")))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody(PersonDto::class.java)
        .consumeWith { response ->
          response.responseBody shouldBe expectedPersonResult
        }
    }

    @Test
    fun `should return Not Found with invalid person identifier`() {
      webTestClient.get()
        .uri("/person/A")
        .headers(setAuthorisation(roles = listOf("ROLE_IPB_FRONTEND_RW")))
        .exchange()
        .expectStatus()
        .isNotFound
    }
  }
}
