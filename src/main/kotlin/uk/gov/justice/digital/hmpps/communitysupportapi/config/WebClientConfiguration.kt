package uk.gov.justice.digital.hmpps.communitysupportapi.config

import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import uk.gov.justice.hmpps.kotlin.auth.healthWebClient
import java.time.Duration

@Configuration
@Profile("!test")
class WebClientConfiguration(
  @Value($$"${services.hmpps-auth-api.base-url}") private val hmppsAuthBaseUri: String,
  @Value($$"${api.health-timeout:2s}") private val healthTimeout: Duration,
  @Value($$"${services.ndelius-integration-api.base-url}") private val deliusBaseUrl: String,
  @Value($$"${services.nomis-api.base-url}") private val nomisBaseUrl: String,
  @Value($$"${services.manage-users-api.base-url}") private val manageUsersAuthBaseUrl: String,
  @Value($$"${webclient.read-timeout-seconds}") private val readTimeoutSeconds: Int,
  @Value($$"${webclient.connect-timeout-seconds}") private val authConnectTimeoutSeconds: Long,
  @Value($$"${webclient.write-timeout-seconds}") private val writeTimeoutSeconds: Int,

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
  ): WebClient = createWebClient(builder, authorizedClientManager, deliusBaseUrl)

  @Bean("nomisWebClient")
  fun nomisWebClient(
    builder: WebClient.Builder,
    authorizedClientManager: OAuth2AuthorizedClientManager,
  ): WebClient = createWebClient(builder, authorizedClientManager, nomisBaseUrl)

  @Bean("manageUsersWebClient")
  fun manageUsersWebClient(
    builder: WebClient.Builder,
    authorizedClientManager: OAuth2AuthorizedClientManager,
  ): WebClient = createWebClient(builder, authorizedClientManager, manageUsersAuthBaseUrl)

  private fun createWebClient(
    builder: WebClient.Builder,
    authorizedClientManager: OAuth2AuthorizedClientManager,
    baseUrl: String,
  ): WebClient {
    val oauth2Filter = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Filter.setDefaultClientRegistrationId(COMMUNITY_SUPPORT_API_CLIENT_ID)

    val httpClient = HttpClient.create()
      .doOnConnected {
        it
          .addHandlerLast(ReadTimeoutHandler(readTimeoutSeconds))
          .addHandlerLast(WriteTimeoutHandler(writeTimeoutSeconds))
      }
      .responseTimeout(Duration.ofSeconds(authConnectTimeoutSeconds))

    return builder
      .clientConnector(ReactorClientHttpConnector(httpClient))
      .baseUrl(baseUrl)
      .filter(oauth2Filter)
      .build()
  }
}
