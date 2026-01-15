package uk.gov.justice.digital.hmpps.communitysupportapi.client

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.PRISONER_NUMBER
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.nomisPersonJson
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.nomisPersonNotFoundJson

class NomisClientIntegrationTest : IntegrationTestBase() {

  @Autowired
  lateinit var nomisClient: NomisClient

  @Test
  fun `should return person when Nomis API returns 200`() {
    wireMockServer.stubFor(
      get(urlEqualTo("/prisoner/$PRISONER_NUMBER"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(
              nomisPersonJson(PRISONER_NUMBER),
            ),
        ),
    )

    val result = nomisClient.getPersonByPrisonerNumber(PRISONER_NUMBER)

    assertThat(result).isNotNull()
    assertThat(result!!.prisonerNumber).isEqualTo(PRISONER_NUMBER)
  }

  @Test
  fun `should return null when Nomis API returns 404`() {
    wireMockServer.stubFor(
      get(urlEqualTo("/prisoner/UNKNOWN"))
        .willReturn(
          aResponse()
            .withStatus(404)
            .withHeader("Content-Type", "application/json")
            .withBody(nomisPersonNotFoundJson()),
        ),
    )

    val result = nomisClient.getPersonByPrisonerNumber("Unknown")

    assertThat(result).isNull()
  }
}
