package uk.gov.justice.digital.hmpps.communitysupportapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.hmpps.kotlin.auth.healthWebClient
import uk.gov.justice.hmpps.kotlin.auth.reactiveAuthorisedWebClient
import java.time.Duration

@Configuration
class WebClientConfiguration(
  @Value("\${hmpps-auth.url}") private val hmppsAuthBaseUri: String,
  @Value("\${api.health-timeout:2s}") private val healthTimeout: Duration,
  @Value("\${services.ndelius-integration-api.base-url}") private val deliusBaseUrl: String,
  @Value("\${services.nomis-api.base-url}") private val nomisBaseUrl: String,
  private val authorizedClientManager: ReactiveOAuth2AuthorizedClientManager,
) {
  companion object {
    const val COMMUNITY_SUPPORT_API_CLIENT_ID = "community-support-api-client"
  }

  // HMPPS Auth health ping is required if your service calls HMPPS Auth to get a token to call other services
  @Bean("hmppsAuthHealthWebClient")
  @ConditionalOnMissingBean(name = ["hmppsAuthHealthWebClient"])
  fun hmppsAuthHealthWebClient(builder: WebClient.Builder): WebClient = builder.healthWebClient(hmppsAuthBaseUri, healthTimeout)

  @Bean
  @ConditionalOnMissingBean
  fun reactiveOAuth2AuthorizedClientManager(
    clientRegistrations: ReactiveClientRegistrationRepository,
    authorizedClientService: ReactiveOAuth2AuthorizedClientService,
  ): ReactiveOAuth2AuthorizedClientManager = AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
    clientRegistrations,
    authorizedClientService,
  )

  @Bean("deliusWebClient")
  @ConditionalOnMissingBean(name = ["deliusWebClient"])
  fun deliusWebClient(builder: WebClient.Builder): WebClient = builder.reactiveAuthorisedWebClient(
    authorizedClientManager,
    COMMUNITY_SUPPORT_API_CLIENT_ID,
    deliusBaseUrl,
  )

  @Bean("nomisWebClient")
  @ConditionalOnMissingBean(name = ["nomisWebClient"])
  fun nomisWebClient(builder: WebClient.Builder): WebClient = builder.reactiveAuthorisedWebClient(
    authorizedClientManager,
    COMMUNITY_SUPPORT_API_CLIENT_ID,
    nomisBaseUrl,
  )
}
