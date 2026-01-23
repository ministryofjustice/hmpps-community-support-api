package uk.gov.justice.digital.hmpps.communitysupportapi.client

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.nomis.NomisPersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException

@Component
class NomisClient(
  @Qualifier("nomisWebClient") private val webClient: WebClient,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getPersonByPrisonerNumber(prisonerNumber: String): Mono<NomisPersonDto> {
    log.debug("Calling Nomis for Prisoner Number: {}", prisonerNumber)

    return webClient.get()
      .uri("/prisoner/$prisonerNumber")
      .accept(MediaType.APPLICATION_JSON)
      .retrieve()
      .onStatus({ status -> status.is4xxClientError }) { response ->
        if (response.statusCode() == HttpStatus.NOT_FOUND) {
          Mono.error(NotFoundException("Person not found in Nomis with identifier: $prisonerNumber"))
        } else {
          Mono.error(RuntimeException("Client error: ${response.statusCode()}"))
        }
      }
      .onStatus({ status -> status.is5xxServerError }) { response ->
        Mono.error(RuntimeException("Server error from Nomis: ${response.statusCode()}"))
      }
      .bodyToMono<NomisPersonDto>()
      .doOnError { e -> log.error("Error calling Nomis API for Prisoner Number $prisonerNumber", e) }
  }
}
