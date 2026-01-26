package uk.gov.justice.digital.hmpps.communitysupportapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.hmpps.kotlin.auth.healthWebClient
import java.time.Duration

@Configuration
@Profile("!test")
class WebClientConfiguration(
  @Value("\${hmpps-auth.url}") private val hmppsAuthBaseUri: String,
  @Value("\${api.health-timeout:2s}") private val healthTimeout: Duration,
  @Value("\${services.ndelius-integration-api.base-url}") private val deliusBaseUrl: String,
  @Value("\${services.nomis-api.base-url}") private val nomisBaseUrl: String,
) {
  companion object {
    const val COMMUNITY_SUPPORT_API_CLIENT_ID = "community-support-api-client"
  }

  // HMPPS Auth health ping is required if your service calls HMPPS Auth to get a token to call other services
  @Bean("hmppsAuthHealthWebClient")
  @ConditionalOnMissingBean(name = ["hmppsAuthHealthWebClient"])
  fun hmppsAuthHealthWebClient(builder: WebClient.Builder): WebClient = builder.healthWebClient(hmppsAuthBaseUri, healthTimeout)

  @Bean
  fun authorizedClientManager(
    clientRegistrationRepository: ClientRegistrationRepository,
    authorizedClientService: OAuth2AuthorizedClientService,
  ): OAuth2AuthorizedClientManager {
    val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
      .clientCredentials()
      .build()

    return AuthorizedClientServiceOAuth2AuthorizedClientManager(
      clientRegistrationRepository,
      authorizedClientService,
    ).apply {
      setAuthorizedClientProvider(authorizedClientProvider)
    }
  }

  @Bean("deliusWebClient")
  fun deliusWebClient(
    builder: WebClient.Builder,
    authorizedClientManager: OAuth2AuthorizedClientManager,
  ): WebClient {
    val oauth2Filter = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Filter.setDefaultClientRegistrationId(COMMUNITY_SUPPORT_API_CLIENT_ID)

    return builder
      .baseUrl(deliusBaseUrl)
      .filter(oauth2Filter)
      .build()
  }

  @Bean("nomisWebClient")
  fun nomisWebClient(
    builder: WebClient.Builder,
    authorizedClientManager: OAuth2AuthorizedClientManager,
  ): WebClient {
    val oauth2Filter = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Filter.setDefaultClientRegistrationId(COMMUNITY_SUPPORT_API_CLIENT_ID)

    return builder
      .baseUrl(nomisBaseUrl)
      .filter(oauth2Filter)
      .build()
  }
}
