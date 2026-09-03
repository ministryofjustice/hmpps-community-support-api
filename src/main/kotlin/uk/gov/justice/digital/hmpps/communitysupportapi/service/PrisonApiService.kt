package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.communitysupportapi.client.PrisonApiClient
import java.time.LocalDate

@Service
class PrisonApiService(
  private val prisonApiClient: PrisonApiClient,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getExpectedReleaseDateByPrisonNumber(prisonNumber: String): LocalDate? {
    log.debug("Fetching expected release date for prison number {}", prisonNumber)
    val person = prisonApiClient.getPersonByPrisonNumber(prisonNumber)

    // Prefer the confirmed release date when available; otherwise use releaseDate.
    return person.confirmedReleaseDate ?: person.releaseDate
  }
}
