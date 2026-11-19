package uk.gov.justice.digital.hmpps.communitysupportapi.integration.health

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.wiremock.HmppsAuthApiExtension.Companion.hmppsAuth

class HealthCheckTest : IntegrationTestBase() {

  @Value("\${services.hmpps-auth-api.base-url}")
  private lateinit var authBaseUrl: String

  @Value("\${spring.security.oauth2.client.provider.hmpps-auth.token-uri}")
  private lateinit var tokenUri: String

  @Test
  fun `auth mock URLs use the running server`() {
    assertThat(authBaseUrl).isEqualTo("http://localhost:${hmppsAuth.port()}/auth")
    assertThat(tokenUri).isEqualTo("$authBaseUrl/oauth/token")

    WebTestClient.bindToServer().build().post()
      .uri(tokenUri)
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("access_token").isEqualTo("ABCDE")
  }

  @Test
  fun `Health page reports ok`() {
    stubPingWithResponse(200)

    webTestClient.get()
      .uri("/health")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("status").isEqualTo("UP")
  }

  @Test
  fun `Health page reports down`() {
    stubPingWithResponse(503)

    webTestClient.get()
      .uri("/health")
      .exchange()
      .expectStatus()
      .is5xxServerError
      .expectBody()
      .jsonPath("status").isEqualTo("DOWN")
      .jsonPath("components.hmppsAuth.status").isEqualTo("DOWN")
  }

  @Test
  fun `Health ping page is accessible`() {
    webTestClient.get()
      .uri("/health/ping")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("status").isEqualTo("UP")
  }

  @Test
  fun `readiness reports ok`() {
    webTestClient.get()
      .uri("/health/readiness")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("status").isEqualTo("UP")
  }

  @Test
  fun `liveness reports ok`() {
    webTestClient.get()
      .uri("/health/liveness")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("status").isEqualTo("UP")
  }
}
