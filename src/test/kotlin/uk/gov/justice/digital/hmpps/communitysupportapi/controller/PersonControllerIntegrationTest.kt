package uk.gov.justice.digital.hmpps.communitysupportapi.controller

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod.GET
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.PersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.CRN
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.PRISONER_NUMBER
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.createCprPrisonPersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.createCprProbationPersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.createHomeOfficeInterest
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.createPersonalDetailsAndCircumstances
import uk.gov.justice.digital.hmpps.communitysupportapi.util.toFormattedDateOfBirth
import uk.gov.justice.digital.hmpps.communitysupportapi.util.toJson
import java.time.LocalDate
import java.time.LocalDateTime

class PersonControllerIntegrationTest : IntegrationTestBase() {

  @Nested
  @DisplayName("GET /bff/person/{personIdentifier}")
  inner class PersonEndpoint {

    @Test
    fun `should return unauthorized if no token`() {
      assertUnauthorized(GET, "/bff/person/PERSONID")
    }

    @Test
    fun `should return forbidden if no role`() {
      assertForbiddenNoRole(GET, "/bff/person/PERSONID")
    }

    @Test
    fun `should return forbidden if wrong role`() {
      assertForbiddenWrongRole(GET, "/bff/person/PERSONID")
    }

    @Test
    fun `should return Bad Request with invalid person identifier`() {
      assertBadRequest(GET, "/bff/person/AA")
    }

    @Test
    fun `should return OK with valid prison number identifier`() {
      stubFor(
        get(urlEqualTo("/person/prison/$PRISONER_NUMBER"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(createCprPrisonPersonDto(PRISONER_NUMBER).toJson()),
          ),
      )
      setupNDeliusStubs(PRISONER_NUMBER)

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
          body.title shouldBe "Mr"
          body.firstName shouldBe "John"
          body.middleNames shouldBe "James"
          body.lastName shouldBe "Smith"
          body.dateOfBirth shouldBe LocalDate.of(1985, 1, 1).toFormattedDateOfBirth()
          body.sex shouldBe "Male"
          body.prisonNumbers shouldBe listOf(PRISONER_NUMBER)
          body.additionalDetails?.disability shouldBe false

          checkDefaultPersonDetailsAndCircumstances(body)
        }
    }

    @Test
    fun `should return OK with valid CRN identifier`() {
      stubFor(
        get(urlEqualTo("/person/probation/$CRN"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(createCprProbationPersonDto(CRN).toJson()),
          ),
      )
      setupNDeliusStubs(CRN)

      webTestClient.get()
        .uri("/bff/person/$CRN")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBody<PersonDto>()
        .consumeWith { response ->
          val body = response.responseBody!!

          body.id shouldNotBe null
          body.personIdentifier shouldBe CRN
          body.title shouldBe "Mr"
          body.firstName shouldBe "John"
          body.middleNames shouldBe "David"
          body.lastName shouldBe "Smith"
          body.dateOfBirth shouldBe LocalDate.of(1985, 1, 1).toFormattedDateOfBirth()
          body.sex shouldBe "Male"
          body.prisonNumbers shouldBe emptyList()
          body.additionalDetails?.disability shouldBe true

          checkDefaultPersonDetailsAndCircumstances(body)
        }
    }

    @Test
    fun `should return Not Found for valid prison number that does not exist`() {
      val unknownPrisonerNumber = "Z9786YX"

      stubFor(
        get(urlEqualTo("/person/prison/$unknownPrisonerNumber"))
          .willReturn(
            aResponse()
              .withStatus(404),
          ),
      )

      assertNotFound(GET, "/bff/person/$unknownPrisonerNumber")
    }

    @Test
    fun `should return Not Found for valid CRN that does not exist`() {
      val unknownCrn = "Z999999"

      stubFor(
        get(urlEqualTo("/person/probation/$unknownCrn"))
          .willReturn(
            aResponse()
              .withStatus(404),
          ),
      )

      assertNotFound(GET, "/bff/person/$unknownCrn")
    }

    private fun setupNDeliusStubs(identifier: String) {
      stubFor(
        get(urlEqualTo("/case/$identifier"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(createPersonalDetailsAndCircumstances()),
          ),
      )
      stubFor(
        get(urlEqualTo(("/case/$identifier/home-office-interest")))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(createHomeOfficeInterest()),
          ),
      )
    }

    private fun checkDefaultPersonDetailsAndCircumstances(body: PersonDto) {
      body.personDetailsAndCircumstances shouldNotBe null
      body.personDetailsAndCircumstances?.preferredLanguage shouldBe "English"
      body.personDetailsAndCircumstances?.personalCircumstances shouldNotBe null
      body.personDetailsAndCircumstances!!.personalCircumstances shouldHaveSize 3

      body.personDetailsAndCircumstances.personalCircumstances[0].type shouldBe "REL"
      body.personDetailsAndCircumstances.personalCircumstances[0].description shouldBe "Relationships"
      body.personDetailsAndCircumstances.personalCircumstances[0].subType shouldBe "REL_SUB"
      body.personDetailsAndCircumstances.personalCircumstances[0].subDescription shouldBe "Relationships sub type"
      body.personDetailsAndCircumstances.personalCircumstances[0].updatedAt shouldBe LocalDateTime.of(2026, 3, 12, 14, 25, 0)

      body.personDetailsAndCircumstances.personalCircumstances[1].type shouldBe "EMP"
      body.personDetailsAndCircumstances.personalCircumstances[1].description shouldBe "Employment"
      body.personDetailsAndCircumstances.personalCircumstances[1].subType shouldBe "EMP_SUB"
      body.personDetailsAndCircumstances.personalCircumstances[1].subDescription shouldBe "Employment sub type"
      body.personDetailsAndCircumstances.personalCircumstances[1].updatedAt shouldBe LocalDateTime.of(2026, 2, 12, 14, 25, 0)

      body.personDetailsAndCircumstances.personalCircumstances[2].type shouldBe "DEP"
      body.personDetailsAndCircumstances.personalCircumstances[2].description shouldBe "Dependants"
      body.personDetailsAndCircumstances.personalCircumstances[2].subType shouldBe "DEP_SUB"
      body.personDetailsAndCircumstances.personalCircumstances[2].subDescription shouldBe "Dependants sub type"
      body.personDetailsAndCircumstances.personalCircumstances[2].updatedAt shouldBe LocalDateTime.of(2026, 1, 12, 14, 25, 0)

      body.personDetailsAndCircumstances.disabilities shouldNotBe null
      body.personDetailsAndCircumstances.disabilities shouldHaveSize 1
      body.personDetailsAndCircumstances.disabilities[0].type shouldBe "BLN"
      body.personDetailsAndCircumstances.disabilities[0].description shouldBe "Blind"

      body.personDetailsAndCircumstances.offenderPersonalityDisorder shouldBe "N/A"
      body.personDetailsAndCircumstances.ofHomeOfficeInterest shouldBe true
      body.personDetailsAndCircumstances.homeOfficeInterestNotes shouldBe "Is of interest"
    }
  }
}
