package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration

@Component
@Profile("local")
class LocalWireMockFixtureClient(
  webClientBuilder: WebClient.Builder,
  @Value($$"${services.nDelius-api.base-url}") private val nDeliusBaseUrl: String,
) {
  companion object {
    private val wireMockTimeout = Duration.ofSeconds(5)
  }

  private val webClient = webClientBuilder.baseUrl(nDeliusBaseUrl).build()

  fun registerPersonalDetailsFixture(crn: String) {
    webClient.post()
      .uri("/__admin/mappings")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        mapOf(
          "name" to "Local appointment fixture personal details for $crn",
          "priority" to 1,
          "request" to mapOf(
            "method" to "GET",
            "urlPath" to "/case/$crn",
          ),
          "response" to mapOf(
            "status" to 200,
            "headers" to mapOf("Content-Type" to "application/json"),
            "jsonBody" to mapOf(
              "preferredLanguage" to mapOf(
                "code" to "EN",
                "description" to "English",
              ),
              "personalCircumstances" to listOf(
                mapOf(
                  "type" to mapOf(
                    "code" to "REL",
                    "description" to "Relationships",
                  ),
                  "subtype" to mapOf(
                    "code" to "REL_SUB",
                    "description" to "Relationships sub type",
                  ),
                  "updatedAt" to "2026-03-12T14:25:00Z",
                ),
              ),
              "disabilities" to listOf(
                mapOf(
                  "type" to mapOf(
                    "code" to "BLN",
                    "description" to "Blind",
                  ),
                  "updatedAt" to "2026-03-12T14:25:00Z",
                ),
              ),
              "offenderPersonalityDisorder" to mapOf(
                "status" to mapOf(
                  "code" to "NO",
                  "description" to "N/A",
                ),
              ),
            ),
          ),
        ),
      )
      .retrieve()
      .toBodilessEntity()
      .block(wireMockTimeout)
  }
}
