package uk.gov.justice.digital.hmpps.communitysupportapi.client

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.DeliusPersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException

@Component
class DeliusClient(
  @Qualifier("deliusWebClient") private val webClient: WebClient,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getPersonByCrn(crn: String): Mono<DeliusPersonDto> {
    log.debug("Calling Delius for CRN: {}", crn)

    val requestBody = mapOf("crn" to crn)

    return webClient.post()
      .uri("/search")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(requestBody)
      .retrieve()
      .onStatus({ status -> status.is4xxClientError }) { response ->
        when (response.statusCode()) {
          HttpStatus.NOT_FOUND -> Mono.error(NotFoundException("Person not found in Delius with CRN: $crn"))
          else -> Mono.error(RuntimeException("Client error from Delius: ${response.statusCode()}"))
        }
      }
      .onStatus({ status -> status.is5xxServerError }) { response ->
        Mono.error(RuntimeException("Server error from Delius: ${response.statusCode()}"))
      }
      .bodyToMono<DeliusPersonDto>()
      .doOnError { e -> log.error("Error calling Delius API for CRN $crn", e) }
  }
}
