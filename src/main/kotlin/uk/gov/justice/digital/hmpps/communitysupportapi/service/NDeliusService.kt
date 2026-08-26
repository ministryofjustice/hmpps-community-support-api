package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.communitysupportapi.client.NDeliusClient
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.CommunityManagerDto
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonDetailsAndCircumstances

@Service
class NDeliusService(
  private val nDeliusClient: NDeliusClient,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getPersonDetailsAndCircumstancesByIdentifier(identifier: String): PersonDetailsAndCircumstances {
    log.debug("Fetching Circumstances for crn {}", identifier)
    val personCircumstances = nDeliusClient.getPersonDetailsAndCircumstancesByCrn(identifier)
    log.debug("Fetching HomeOffice Interest for crn {}", identifier)
    val homeOfficeInterest = nDeliusClient.getHomeOfficeInterestByCrn(identifier)

    return PersonDetailsAndCircumstances.from(personCircumstances, homeOfficeInterest)
  }

  fun getCommunityManagerByIdentifier(identifier: String): CommunityManagerDto {
    log.debug("Fetching Community Manager for crn {}", identifier)
    return nDeliusClient.getCommunityManagerByCrn(identifier)
  }
}
