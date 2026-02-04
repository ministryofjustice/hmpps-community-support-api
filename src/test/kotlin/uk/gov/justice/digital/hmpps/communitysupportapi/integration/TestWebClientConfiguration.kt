package uk.gov.justice.digital.hmpps.communitysupportapi.integration

import org.mockito.Mockito.mock
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
  fun hmppsAuthHealthWebClient(
    @Value("\${services.hmpps-auth-api.base-url}") hmppsAuthBaseUrl: String,
  ): WebClient = WebClient.builder()
    .healthWebClient(hmppsAuthBaseUrl, Duration.ofSeconds(2))

  @Bean
  fun reactiveOAuth2AuthorizedClientManager(): ReactiveOAuth2AuthorizedClientManager = mock(ReactiveOAuth2AuthorizedClientManager::class.java)

  @Bean
  @Qualifier("deliusWebClient")
  fun deliusWebClient(
    @Value("\${services.ndelius-integration-api.base-url}") deliusBaseUrl: String,
  ): WebClient = WebClient.builder().baseUrl(deliusBaseUrl).build()

  @Bean
  @Qualifier("nomisWebClient")
  fun nomisWebClient(
    @Value("\${services.nomis-api.base-url}") nomisBaseUrl: String,
  ): WebClient = WebClient.builder().baseUrl(nomisBaseUrl).build()

  @Bean
  @Qualifier("manageUsersWebClient")
  fun manageUsersWebClient(
    @Value("\${services.manage-users-api.base-url}") manageUsersBaseUrl: String,
  ): WebClient = WebClient.builder().baseUrl(manageUsersBaseUrl).build()
}
