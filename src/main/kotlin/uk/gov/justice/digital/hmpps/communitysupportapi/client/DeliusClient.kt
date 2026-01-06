package uk.gov.justice.digital.hmpps.communitysupportapi.client

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.DeliusPersonDto

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

    val uri = "$baseUrl/person/$crn"

    return try {
      restClient.get()
        .uri(uri)
        .retrieve()
        .body<DeliusPersonDto>()
    } catch (e: HttpClientErrorException.NotFound) {
      log.info("No Delius person found for prisoner number {}", crn)
      null
    }
  }
}
