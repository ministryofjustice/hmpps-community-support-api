package uk.gov.justice.digital.hmpps.communitysupportapi.client

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.nomisPersonJson
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.nomisPersonNotFoundJson

class NomisClientIntegrationTest : IntegrationTestBase() {

  @Autowired
  lateinit var nomisClient: NomisClient

  private val prisonerNumber = "A1234BC"

  @Test
  fun `should return person when Nomis API returns 200`() {
    stubFor(
      get(urlEqualTo("/prisoner/$prisonerNumber"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(nomisPersonJson(prisonerNumber)),
        ),
    )

    val result = nomisClient.getPersonByPrisonerNumber(prisonerNumber)

    assertThat(result.prisonerNumber).isEqualTo(prisonerNumber)
  }

  @Test
  fun `should throw error when Nomis API returns 404`() {
    val unknownPrisoner = "UNKNOWN"

    stubFor(
      get(urlEqualTo("/prisoner/$unknownPrisoner"))
        .willReturn(
          aResponse()
            .withStatus(404)
            .withHeader("Content-Type", "application/json")
            .withBody(nomisPersonNotFoundJson()),
        ),
    )

    assertThrows(NotFoundException::class.java) {
      nomisClient.getPersonByPrisonerNumber(unknownPrisoner)
    }
  }
}
