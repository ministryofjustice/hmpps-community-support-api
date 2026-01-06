package uk.gov.justice.digital.hmpps.communitysupportapi.controller

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.PersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.util.toJson

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
        .uri("/person/PERSONID")
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return OK with valid person identifier`() {
      val nomisPerson = ExternalApiResponse.nomisPerson("A1234BC")

      wireMockServer.stubFor(
        get(urlEqualTo("/prisoner/A1234BC"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(nomisPerson.toJson())
          )
      )

      val expectedPersonResult = PersonDto("A1234BC")

      webTestClient.get()
        .uri("/person/A1234BC")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBody(PersonDto::class.java)
        .consumeWith { response ->
          response.responseBody shouldBe expectedPersonResult
        }
    }

    @Test
    fun `should return Bad Request with invalid person identifier`() {
      webTestClient.get()
        .uri("/person/A")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isBadRequest
    }

    @Test
    fun `should return Not Found for valid person identifier that does not exist`() {
      val prisonerNumber = "Z9786YX"

      wireMockServer.stubFor(
        get(urlEqualTo("/prisoner/$prisonerNumber"))
          .willReturn(
            aResponse()
              .withStatus(404)
          )
      )

      webTestClient.get()
        .uri("/person/$prisonerNumber")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isNotFound
    }
  }
}
