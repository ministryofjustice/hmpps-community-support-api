package uk.gov.justice.digital.hmpps.communitysupportapi.client

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.DeliusPersonDto

@Component
class DeliusClient(
  @Value("\${external-api.locations.delius.base-url}") private val baseUrl: String,
  private val restClient: RestClient,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getPersonByCrn(crn: String): DeliusPersonDto? {
    log.debug("Calling Delius to retrieve person details using CRN: {}", crn)

    val uri = UriComponentsBuilder
      .fromUriString(baseUrl) // e.g. http://localhost:8080
      .path("/search")
      .build()
      .toUri()

    return try {
      restClient.post()
        .uri(uri)
        .contentType(MediaType.APPLICATION_JSON)
        .body(mapOf("crn" to crn))
        .retrieve()
        .body<DeliusPersonDto>()
    } catch (e: HttpClientErrorException.NotFound) {
      log.info("Unable to find person in Delius with CRN: {}", crn)
      null
    }
  }
}
