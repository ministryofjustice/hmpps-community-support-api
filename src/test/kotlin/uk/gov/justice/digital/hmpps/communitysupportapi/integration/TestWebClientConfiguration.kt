package uk.gov.justice.digital.hmpps.communitysupportapi.integration

import io.mockk.mockk
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.hmpps.kotlin.auth.healthWebClient
import java.time.Duration

@TestConfiguration
class TestWebClientConfiguration {

  @Bean
  @Qualifier("hmppsAuthHealthWebClient")
  fun hmppsAuthHealthWebClient(@Value("\${hmpps-auth.url}") hmppsAuthBaseUrl: String): WebClient = WebClient.builder()
    .healthWebClient(
      hmppsAuthBaseUrl,
      Duration.ofSeconds(2),
    )

  @Bean
  fun reactiveOAuth2AuthorizedClientManager(): ReactiveOAuth2AuthorizedClientManager = mockk(relaxed = true)

  @Bean
  @Qualifier("deliusWebClient")
  fun deliusWebClient(@Value("\${external-api.locations.delius.base-url}") deliusBaseUrl: String): WebClient = WebClient.builder().baseUrl(deliusBaseUrl).build()

  @Bean
  @Qualifier("nomisWebClient")
  fun nomisWebClient(@Value("\${external-api.locations.nomis.base-url}") nomisBaseUrl: String): WebClient = WebClient.builder().baseUrl(nomisBaseUrl).build()
}
