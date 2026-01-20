package uk.gov.justice.digital.hmpps.communitysupportapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import uk.gov.justice.hmpps.kotlin.auth.healthWebClient
import uk.gov.justice.hmpps.kotlin.auth.reactiveAuthorisedWebClient
import java.time.Duration

@Configuration
@Profile("!test")
class WebClientConfiguration(
  @param:Value("\${hmpps-auth.url}") val hmppsAuthBaseUri: String,
  @param:Value("\${api.health-timeout:2s}") val healthTimeout: Duration,
) {
  // HMPPS Auth health ping is required if your service calls HMPPS Auth to get a token to call other services
  @Bean
  fun hmppsAuthHealthWebClient(builder: WebClient.Builder): WebClient = builder.healthWebClient(hmppsAuthBaseUri, healthTimeout)

  @Bean
  fun reactiveOAuth2AuthorizedClientManager(
    clientRegistrations: ReactiveClientRegistrationRepository,
    authorizedClientService: ReactiveOAuth2AuthorizedClientService,
  ): ReactiveOAuth2AuthorizedClientManager = AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
    clientRegistrations,
    authorizedClientService,
  )

  fun WebClient.Builder.reactiveAuthorisedWebClient(
    authorizedClientManager: ReactiveOAuth2AuthorizedClientManager,
    registrationId: String,
    url: String,
    timeout: Duration = Duration.ofSeconds(5),
  ): WebClient {
    val oauth2Filter = ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Filter.setDefaultClientRegistrationId(registrationId)

    return this
      .baseUrl(url)
      .clientConnector(ReactorClientHttpConnector(HttpClient.create().responseTimeout(timeout)))
      .filter(oauth2Filter)
      .build()
  }

  @Bean("deliusWebClient")
  fun deliusWebClient(
    builder: WebClient.Builder,
    authorizedClientManager: ReactiveOAuth2AuthorizedClientManager,
    @Value("\${external-api.locations.delius.base-url}") deliusBaseUrl: String,
  ): WebClient = builder.reactiveAuthorisedWebClient(
    authorizedClientManager = authorizedClientManager,
    registrationId = "community-support-api-client",
    url = deliusBaseUrl,
  )

  @Bean("nomisWebClient")
  fun nomisWebClient(
    builder: WebClient.Builder,
    authorizedClientManager: ReactiveOAuth2AuthorizedClientManager,
    @Value("\${external-api.locations.nomis.base-url}") nomisBaseUrl: String,
  ): WebClient = builder.reactiveAuthorisedWebClient(
    authorizedClientManager = authorizedClientManager,
    registrationId = "community-support-api-client",
    url = nomisBaseUrl,
  )
}
