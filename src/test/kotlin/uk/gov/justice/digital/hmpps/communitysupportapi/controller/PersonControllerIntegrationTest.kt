package uk.gov.justice.digital.hmpps.communitysupportapi.controller

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.PersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.PRISONER_NUMBER
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.createNomisPersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.util.toJson
import java.time.LocalDate

class PersonControllerIntegrationTest : IntegrationTestBase() {

  @Nested
  @DisplayName("GET /bff/person/{personIdentifier}")
  inner class PersonEndpoint {

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri("/bff/person/PERSONID")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri("/bff/person/PERSONID")
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
        .uri("/bff/person/PERSONID")
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return OK with valid person identifier`() {
      stubFor(
        get(urlEqualTo("/prisoner/$PRISONER_NUMBER"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(createNomisPersonDto(PRISONER_NUMBER).toJson()),
          ),
      )

      webTestClient.get()
        .uri("/bff/person/$PRISONER_NUMBER")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBody<PersonDto>()
        .consumeWith { response ->
          val body = response.responseBody!!

          body.id shouldNotBe null
          body.personIdentifier shouldBe PRISONER_NUMBER
          body.firstName shouldBe "John"
          body.lastName shouldBe "Smith"
          body.dateOfBirth shouldBe LocalDate.of(1985, 1, 1)
          body.sex shouldBe "Male"
        }
    }

    @Test
    fun `should return Bad Request with invalid person identifier`() {
      webTestClient.get()
        .uri("/bff/person/A")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isBadRequest
    }

    @Test
    fun `should return Not Found for valid person identifier that does not exist`() {
      val unknownPrisonerNumber = "Z9786YX"

      stubFor(
        get(urlEqualTo("/prisoner/$unknownPrisonerNumber"))
          .willReturn(
            aResponse()
              .withStatus(404),
          ),
      )

      webTestClient.get()
        .uri("/bff/person/$unknownPrisonerNumber")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isNotFound
    }
  }
}
