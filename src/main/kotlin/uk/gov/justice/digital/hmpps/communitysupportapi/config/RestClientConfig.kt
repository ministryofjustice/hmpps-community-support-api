package uk.gov.justice.digital.hmpps.communitysupportapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient

@Configuration
class RestClientConfig(
  @Value("\${external-api.auth.token}") private val token: String,
) {
  @Bean
  fun restClient(): RestClient = RestClient.builder()
    .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer $token")
    .build()
}
