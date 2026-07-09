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
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.CRN
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.arnsRoshRiskJson
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.arnsRoshRiskNotFoundJson

class AssessRisksAndNeedsClientIntegrationTest : IntegrationTestBase() {

  @Autowired
  lateinit var assessRisksAndNeedsClient: AssessRisksAndNeedsClient

  @Test
  fun `should return ROSH risks when ARNS API returns 200`() {
    stubFor(
      get(urlEqualTo("/risks/crn/$CRN"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(arnsRoshRiskJson()),
        ),
    )

    val result = assessRisksAndNeedsClient.getRoshRisksByCrn(CRN)

    assertThat(result).isNotNull
    assertThat(result.summary.overallRiskLevel).isEqualTo("HIGH")
    assertThat(result.summary.whoIsAtRisk).isEqualTo("Staff and public are at risk")
    assertThat(result.riskToSelf.suicide?.risk).isEqualTo("YES")
    assertThat(result.otherRisks.controlIssuesDisruptiveBehaviour).isEqualTo("YES")
  }

  @Test
  fun `should throw NotFoundException when ARNS API returns 404`() {
    stubFor(
      get(urlEqualTo("/risks/crn/UNKNOWN"))
        .willReturn(
          aResponse()
            .withStatus(404)
            .withHeader("Content-Type", "application/json")
            .withBody(arnsRoshRiskNotFoundJson()),
        ),
    )

    assertThrows(NotFoundException::class.java) {
      assessRisksAndNeedsClient.getRoshRisksByCrn("UNKNOWN")
    }
  }

  @Test
  fun `should throw RuntimeException when ARNS API returns 500`() {
    stubFor(
      get(urlEqualTo("/risks/crn/$CRN"))
        .willReturn(
          aResponse()
            .withStatus(500),
        ),
    )

    assertThrows(RuntimeException::class.java) {
      assessRisksAndNeedsClient.getRoshRisksByCrn(CRN)
    }
  }
}
