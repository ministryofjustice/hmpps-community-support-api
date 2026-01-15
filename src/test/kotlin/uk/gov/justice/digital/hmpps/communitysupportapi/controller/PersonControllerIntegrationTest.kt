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
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.PRISONER_NUMBER
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.createNomisPersonAdditionalDetails
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.createNomisPersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.util.toJson

class PersonControllerIntegrationTest : IntegrationTestBase() {

  @Nested
  @DisplayName("GET /person/bff/{personIdentifier}")
  inner class PersonEndpoint {

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri("/person/bff/PERSONID")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri("/person/bff/PERSONID")
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
        .uri("/person/bff/PERSONID")
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return OK with valid person identifier`() {
      val nomisPerson = createNomisPersonDto(PRISONER_NUMBER)

      wireMockServer.stubFor(
        get(urlEqualTo("/prisoner/$PRISONER_NUMBER"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(nomisPerson.toJson()),
          ),
      )

      val expectedPersonResult = PersonDto(
        PRISONER_NUMBER,
        firstName = nomisPerson.firstName,
        lastName = nomisPerson.lastName,
        dateOfBirth = nomisPerson.dateOfBirth,
        sex = nomisPerson.gender,
        additionalDetails = createNomisPersonAdditionalDetails(),
      )

      webTestClient.get()
        .uri("/person/bff/$PRISONER_NUMBER")
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
        .uri("/person/bff/A")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isBadRequest
    }

    @Test
    fun `should return Not Found for valid person identifier that does not exist`() {
      val unknownPrisonerNumber = "Z9786YX"

      wireMockServer.stubFor(
        get(urlEqualTo("/prisoner/$unknownPrisonerNumber"))
          .willReturn(
            aResponse()
              .withStatus(404),
          ),
      )

      webTestClient.get()
        .uri("/person/bff/$unknownPrisonerNumber")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isNotFound
    }
  }
}
