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
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.PRISONER_NUMBER
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.cprPersonNotFoundJson
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.cprPrisonPersonJson
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.ExternalApiResponse.cprProbationPersonJson

class CorePersonRecordClientIntegrationTest : IntegrationTestBase() {

  @Autowired
  lateinit var corePersonRecordClient: CorePersonRecordClient

  @Test
  fun `should return person when CPR probation API returns 200`() {
    stubFor(
      get(urlEqualTo("/person/probation/$CRN"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(cprProbationPersonJson(CRN)),
        ),
    )

    val result = corePersonRecordClient.getPersonByCrn(CRN)

    assertThat(result).isNotNull
    assertThat(result.identifiers.crns).contains(CRN)
  }

  @Test
  fun `should throw NotFoundException when CPR probation API returns 404`() {
    stubFor(
      get(urlEqualTo("/person/probation/UNKNOWN"))
        .willReturn(
          aResponse()
            .withStatus(404)
            .withHeader("Content-Type", "application/json")
            .withBody(cprPersonNotFoundJson()),
        ),
    )

    assertThrows(NotFoundException::class.java) {
      corePersonRecordClient.getPersonByCrn("UNKNOWN")
    }
  }

  @Test
  fun `should return person when CPR prison API returns 200`() {
    stubFor(
      get(urlEqualTo("/person/prison/$PRISONER_NUMBER"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(cprPrisonPersonJson(PRISONER_NUMBER)),
        ),
    )

    val result = corePersonRecordClient.getPersonByPrisonNumber(PRISONER_NUMBER)

    assertThat(result).isNotNull
    assertThat(result.identifiers.prisonNumbers).contains(PRISONER_NUMBER)
  }

  @Test
  fun `should throw NotFoundException when CPR prison API returns 404`() {
    stubFor(
      get(urlEqualTo("/person/prison/UNKNOWN"))
        .willReturn(
          aResponse()
            .withStatus(404)
            .withHeader("Content-Type", "application/json")
            .withBody(cprPersonNotFoundJson()),
        ),
    )

    assertThrows(NotFoundException::class.java) {
      corePersonRecordClient.getPersonByPrisonNumber("UNKNOWN")
    }
  }
}
