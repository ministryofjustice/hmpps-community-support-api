package uk.gov.justice.digital.hmpps.communitysupportapi.client

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.cpr.CprPersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException

@Component
class CorePersonRecordClient(
  @Qualifier("corePersonRecordWebClient") private val webClient: WebClient,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getPersonByCrn(crn: String): CprPersonDto {
    log.debug("Calling Core Person Record for CRN: {}", crn)

    return webClient.get()
      .uri("/person/probation/$crn")
      .accept(MediaType.APPLICATION_JSON)
      .exchangeToMono { response ->
        when {
          response.statusCode() == HttpStatus.NOT_FOUND ->
            Mono.error(NotFoundException("Person not found in Core Person Record with CRN: $crn"))
          response.statusCode().is4xxClientError ->
            Mono.error(RuntimeException("Client error from Core Person Record: ${response.statusCode()}"))
          response.statusCode().is5xxServerError ->
            Mono.error(RuntimeException("Server error from Core Person Record: ${response.statusCode()}"))
          else -> response.bodyToMono<CprPersonDto>()
        }
      }
      .doOnError { e -> log.error("Error calling Core Person Record API for CRN $crn", e) }
      .block()!!
  }

  fun getPersonByPrisonNumber(prisonNumber: String): CprPersonDto {
    log.debug("Calling Core Person Record for prison number: {}", prisonNumber)

    return webClient.get()
      .uri("/person/prison/$prisonNumber")
      .accept(MediaType.APPLICATION_JSON)
      .exchangeToMono { response ->
        when {
          response.statusCode() == HttpStatus.NOT_FOUND ->
            Mono.error(NotFoundException("Person not found in Core Person Record with prison number: $prisonNumber"))
          response.statusCode().is4xxClientError ->
            Mono.error(RuntimeException("Client error from Core Person Record: ${response.statusCode()}"))
          response.statusCode().is5xxServerError ->
            Mono.error(RuntimeException("Server error from Core Person Record: ${response.statusCode()}"))
          else -> response.bodyToMono<CprPersonDto>()
        }
      }
      .doOnError { e -> log.error("Error calling Core Person Record API for prison number $prisonNumber", e) }
      .block()!!
  }
}
