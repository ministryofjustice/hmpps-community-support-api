package uk.gov.justice.digital.hmpps.communitysupportapi.client

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.nomis.NomisPersonDto

@Component
class NomisClient(
  @Value("\${external-api.locations.nomis.base-url}") private val baseUrl: String,
  private val restClient: RestClient,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getPersonByPrisonerNumber(prisonerNumber: String): NomisPersonDto? {
    log.debug("Calling Nomis to retrieve person details using prisoner number: {}", prisonerNumber)

    val uri = UriComponentsBuilder
      .fromUriString(baseUrl)
      .pathSegment("prisoner", prisonerNumber)
      .build()
      .toUri()

    return try {
      restClient.get()
        .uri(uri)
        .retrieve()
        .body<NomisPersonDto>()
    } catch (e: HttpClientErrorException.NotFound) {
      log.info("Unable to find person in Nomis with prisoner number {}", prisonerNumber)
      null
    }
  }
}
