package uk.gov.justice.digital.hmpps.communitysupportapi.client

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase

class DeliusClientIntegrationTest : IntegrationTestBase() {

  @Autowired
  lateinit var deliusClient: DeliusClient

  @Test
  fun `should return person when Delius API returns 200`() {
    val responseBody = """{"crn":"X123456"}"""
    wireMockServer.stubFor(
      get(urlEqualTo("/person/X123456"))
        .willReturn(
          aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(responseBody)
          .withStatus(200))
    )

    val result = deliusClient.getPersonByCrn("X123456")

    assertThat(result).isNotNull
    assertThat(result!!.crn).isEqualTo("X123456")
  }

  @Test
  fun `should return null when Delius API returns 404`() {
    wireMockServer.stubFor(
      get(urlEqualTo("/person/X999999"))
        .willReturn(aResponse().withStatus(404))
    )

    val result = deliusClient.getPersonByCrn("X999999")

    assertThat(result).isNull()
  }
}
