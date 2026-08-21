package uk.gov.justice.digital.hmpps.communitysupportapi.client

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.HomeOfficeInterestDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.PersonDetailsAndCircumstancesDto
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException

@Component
class NDeliusClient(
  @Qualifier("nDeliusWebClient") private val webClient: WebClient,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getPersonDetailsAndCircumstancesByCrn(crn: String): PersonDetailsAndCircumstancesDto {
    log.debug("Retrieving Circumstances for crn {}", crn)

    return webClient.get()
      .uri("/case/$crn")
      .accept(MediaType.APPLICATION_JSON)
      .exchangeToMono { response ->
        when {
          response.statusCode() == HttpStatus.NOT_FOUND ->
            Mono.error(NotFoundException("Person not found in nDelius with CRN: $crn"))

          response.statusCode().is4xxClientError ->
            Mono.error(RuntimeException("Client error from nDelius: ${response.statusCode()}"))

          response.statusCode().is5xxServerError ->
            Mono.error(RuntimeException("Server error from nDelius: ${response.statusCode()}"))

          else -> response.bodyToMono<PersonDetailsAndCircumstancesDto>()
        }
      }
      .doOnError { e -> log.error("Error calling nDelius for CRN: $crn", e) }
      .block()!!
  }

  fun getHomeOfficeInterestByCrn(crn: String): HomeOfficeInterestDto {
    log.debug("Retrieving Home office interest for crn {}", crn)

    return webClient.get()
      .uri("/case/$crn/home-office-interest")
      .accept(MediaType.APPLICATION_JSON)
      .exchangeToMono { response ->
        when {
          response.statusCode() == HttpStatus.NOT_FOUND ->
            Mono.error(NotFoundException("Person not found in nDelius with CRN: $crn"))

          response.statusCode().is4xxClientError ->
            Mono.error(RuntimeException("Client error from nDelius: ${response.statusCode()}"))

          response.statusCode().is5xxServerError ->
            Mono.error(RuntimeException("Server error from nDelius: ${response.statusCode()}"))

          else -> response.bodyToMono<HomeOfficeInterestDto>()
        }
      }
      .doOnError { e -> log.error("Error calling delius for CRN: $crn", e) }
      .block()!!
  }
}
