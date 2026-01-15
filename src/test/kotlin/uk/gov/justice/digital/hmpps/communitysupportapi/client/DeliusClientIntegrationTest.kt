package uk.gov.justice.digital.hmpps.communitysupportapi.client

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.containing
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.CRN
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.deliusPersonJson
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.deliusPersonNotFoundJson

class DeliusClientIntegrationTest : IntegrationTestBase() {

  @Autowired
  lateinit var deliusClient: DeliusClient

  @Test
  fun `should return person when Delius API returns 200`() {
    wireMockServer.stubFor(
      post(urlPathEqualTo("/search"))
        .withHeader("Content-Type", containing("application/json"))
        .withRequestBody(matchingJsonPath("$.crn", equalTo(CRN)))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(deliusPersonJson(CRN)),
        ),
    )

    val result = deliusClient.getPersonByCrn(CRN)

    assertThat(result).isNotNull()
    assertThat(result!!.otherIds?.crn).isEqualTo(CRN)
  }

  @Test
  fun `should return null when Delius API returns 404`() {
    wireMockServer.stubFor(
      post(urlPathEqualTo("/search"))
        .withHeader("Content-Type", containing("application/json"))
        .withRequestBody(matchingJsonPath("$.crn", equalTo("Unknown")))
        .willReturn(
          aResponse()
            .withStatus(404)
            .withHeader("Content-Type", "application/json")
            .withBody(deliusPersonNotFoundJson()),
        ),
    )

    val result = deliusClient.getPersonByCrn("Unknown")

    assertThat(result).isNull()
  }
}
