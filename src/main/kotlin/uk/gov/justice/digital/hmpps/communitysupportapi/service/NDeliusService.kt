package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.communitysupportapi.client.NDeliusClient
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonCircumstances

@Service
class NDeliusService(
  private val nDeliusClient: NDeliusClient,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getPersonCircumstancesByCrn(crn: String): PersonCircumstances {
    log.debug("Fetching Circumstances for crn {}", crn)
    val personCircumstances = nDeliusClient.getPersonDetailsAndCircumstancesByCrn(crn)
    log.debug("Fetching HomeOffice Interest for crn {}", crn)
    val homeOfficeInterest = nDeliusClient.getHomeOfficeInterestByCrn(crn)

    return PersonCircumstances.from(personCircumstances, homeOfficeInterest)
  }
}
