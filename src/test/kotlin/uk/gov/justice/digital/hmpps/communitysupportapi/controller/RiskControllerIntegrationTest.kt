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
import org.springframework.http.HttpMethod.GET
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.arns.CommunitySupportRiskDto
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.CRN
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.arnsRoshRiskJson
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.arnsRoshRiskNotFoundJson
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.arnsStaleRoshRiskJson
import uk.gov.justice.digital.hmpps.communitysupportapi.util.toFormattedAssessmentDate
import java.time.LocalDateTime

class RiskControllerIntegrationTest : IntegrationTestBase() {

  @Nested
  @DisplayName("GET /bff/risk/rosh/{crn}")
  inner class RoshRisksEndpoint {

    @Test
    fun `should return unauthorized if no token`() {
      assertUnauthorized(GET, "/bff/risk/rosh/$CRN")
    }

    @Test
    fun `should return forbidden if no role`() {
      assertForbiddenNoRole(GET, "/bff/risk/rosh/$CRN")
    }

    @Test
    fun `should return forbidden if wrong role`() {
      assertForbiddenWrongRole(GET, "/bff/risk/rosh/$CRN")
    }

    @Test
    fun `should return full risk data when assessment is within 12 months`() {
      val assessedOn = LocalDateTime.now().minusDays(30)
      stubFor(
        get(urlEqualTo("/risks/crn/$CRN"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(arnsRoshRiskJson(assessedOn)),
          ),
      )

      webTestClient.get()
        .uri("/bff/risk/rosh/$CRN")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBody<CommunitySupportRiskDto>()
        .consumeWith { response ->
          val body = response.responseBody!!

          body.assessmentWithin12Months shouldBe true
          body.assessedOn shouldBe assessedOn.toFormattedAssessmentDate()
          body.summary shouldNotBe null
          body.summary?.overallRiskLevel shouldBe "HIGH"
          body.summary?.whoIsAtRisk shouldBe "Staff and public are at risk"
          body.summary?.riskInCommunity?.get("HIGH") shouldBe listOf("Public", "Known Adult")
          body.summary?.riskInCustody?.get("VERY_HIGH") shouldBe listOf("Staff")
          body.riskToSelf shouldNotBe null
          body.riskToSelf?.suicide?.riskIndicator shouldBe "YES"
          body.riskToSelf?.suicide?.currentConcernsReason shouldBe "Current suicide concerns"
          body.riskToSelf?.vulnerability?.currentConcernsReason shouldBe "Vulnerability concerns noted"
        }
    }

    @Test
    fun `should return blank response with assessmentWithin12Months false when assessment is older than 12 months`() {
      stubFor(
        get(urlEqualTo("/risks/crn/$CRN"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(arnsStaleRoshRiskJson()),
          ),
      )

      webTestClient.get()
        .uri("/bff/risk/rosh/$CRN")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBody<CommunitySupportRiskDto>()
        .consumeWith { response ->
          val body = response.responseBody!!

          body.assessmentWithin12Months shouldBe false
          body.assessedOn shouldBe null
          body.riskToSelf shouldBe null
          body.summary shouldBe null
        }
    }

    @Test
    fun `should return Not Found when CRN does not exist`() {
      val unknownCrn = "Z999999"

      stubFor(
        get(urlEqualTo("/risks/crn/$unknownCrn"))
          .willReturn(
            aResponse()
              .withStatus(404)
              .withHeader("Content-Type", "application/json")
              .withBody(arnsRoshRiskNotFoundJson()),
          ),
      )

      assertNotFound(GET, "/bff/risk/rosh/$unknownCrn")
    }

    @Test
    fun `should return 500 when ARNS service is unavailable`() {
      stubFor(
        get(urlEqualTo("/risks/crn/$CRN"))
          .willReturn(
            aResponse()
              .withStatus(500),
          ),
      )

      assertServerError(GET, "/bff/risk/rosh/$CRN")
    }
  }
}
