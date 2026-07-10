package uk.gov.justice.digital.hmpps.communitysupportapi.client

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.arns.ArnsRoshRiskDto
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException

@Component
class AssessRisksAndNeedsClient(
  @Qualifier("assessRisksAndNeedsWebClient") private val webClient: WebClient,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getRoshRisksByCrn(crn: String): ArnsRoshRiskDto {
    log.debug("Calling Assess Risks and Needs for CRN: {}", crn)

    return webClient.get()
      .uri("/risks/crn/$crn")
      .accept(MediaType.APPLICATION_JSON)
      .exchangeToMono { response ->
        when {
          response.statusCode() == HttpStatus.NOT_FOUND ->
            Mono.error(NotFoundException("ROSH risks not found in Assess Risks and Needs for CRN: $crn"))
          response.statusCode().is4xxClientError ->
            Mono.error(RuntimeException("Client error from Assess Risks and Needs: ${response.statusCode()}"))
          response.statusCode().is5xxServerError ->
            Mono.error(RuntimeException("Server error from Assess Risks and Needs: ${response.statusCode()}"))
          else -> response.bodyToMono<ArnsRoshRiskDto>()
        }
      }
      .doOnError { e -> log.error("Error calling Assess Risks and Needs API for CRN $crn", e) }
      .block()!!
  }
}
