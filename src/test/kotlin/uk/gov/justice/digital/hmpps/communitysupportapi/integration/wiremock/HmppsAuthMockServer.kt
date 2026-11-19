package uk.gov.justice.digital.hmpps.communitysupportapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.time.LocalDateTime
import java.time.ZoneOffset

class HmppsAuthApiExtension :
  BeforeAllCallback,
  BeforeEachCallback {
  companion object {
    @JvmField
    val hmppsAuth = HmppsAuthMockServer()
  }

  override fun beforeAll(context: ExtensionContext) {
    context.root.getStore(ExtensionContext.Namespace.create(HmppsAuthApiExtension::class.java))
      .getOrComputeIfAbsent("hmppsAuth") {
        hmppsAuth.start()
        ExtensionContext.Store.CloseableResource { hmppsAuth.stop() }
      }
  }

  override fun beforeEach(context: ExtensionContext) {
    hmppsAuth.resetRequests()
  }
}

class HmppsAuthMockServer : WireMockServer(WireMockConfiguration.options().dynamicPort()) {
  fun stubGrantToken() {
    stubFor(
      post(urlEqualTo("/auth/oauth/token"))
        .willReturn(
          aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody(
              """
                {
                  "token_type": "bearer",
                  "access_token": "ABCDE",
                  "expires_in": ${LocalDateTime.now().plusHours(2).toEpochSecond(ZoneOffset.UTC)}
                }
              """.trimIndent(),
            ),
        ),
    )
  }

  fun stubHealthPing(status: Int) {
    stubFor(
      get("/auth/health/ping").willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(if (status == 200) """{"status":"UP"}""" else """{"status":"DOWN"}""")
          .withStatus(status),
      ),
    )
  }
}
