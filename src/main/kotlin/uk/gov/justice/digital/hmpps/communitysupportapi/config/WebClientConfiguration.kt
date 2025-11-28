package uk.gov.justice.digital.hmpps.communitysupportapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Configuration
class WebClientConfiguration(
  @param:Value("\${hmpps-auth.url}") val hmppsAuthBaseUri: String,
  @param:Value("\${api.health-timeout:2s}") val healthTimeout: Duration,
  @param:Value("\${api.timeout:20s}") val timeout: Duration,
) {

  @Bean
  @ConditionalOnMissingBean(WebClient.Builder::class)
  fun webClientBuilder(): WebClient.Builder = WebClient.builder()

  @Bean
  fun hmppsAuthHealthWebClient(builder: WebClient.Builder): WebClient = builder.baseUrl(hmppsAuthBaseUri)
    .clientConnector(ReactorClientHttpConnector(HttpClient.create().responseTimeout(healthTimeout)))
    .build()
}
