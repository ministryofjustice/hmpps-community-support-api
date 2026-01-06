package uk.gov.justice.digital.hmpps.communitysupportapi.client

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse


class NomisClientIntegrationTest : IntegrationTestBase() {

  @Autowired
  lateinit var nomisClient: NomisClient

  @Test
  fun `should return person when Nomis API returns 200`() {
    wireMockServer.stubFor(
      get(urlEqualTo("/prisoner/A1234BC"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(ExternalApiResponse.nomisPersonJson("A1234BC"))
        )
    )

    val result = nomisClient.getPersonByPrisonerNumber("A1234BC")

    assertThat(result).isNotNull()
    assertThat(result!!.prisonerNumber).isEqualTo("A1234BC")
  }

  @Test
  fun `should return null when Nomis API returns 404`() {
    wireMockServer.stubFor(
      get(urlEqualTo("/prisoner/UNKNOWN"))
        .willReturn(
          aResponse()
            .withStatus(404)
            .withHeader("Content-Type", "application/json")
            .withBody(ExternalApiResponse.nomisPersonNotFoundJson())
        )
    )

    val result = nomisClient.getPersonByPrisonerNumber("UNKNOWN")

    assertThat(result).isNull()
  }
}
