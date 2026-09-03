package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.communitysupportapi.client.NDeliusClient
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.CommunityManagerDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.OffenceSentenceDto
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonDetailsAndCircumstances

@Service
class NDeliusService(
  private val nDeliusClient: NDeliusClient,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getPersonalDetailsAndCircumstancesByIdentifier(identifier: String): PersonDetailsAndCircumstances {
    log.debug("Fetching Circumstances for crn {}", identifier)
    val personalCircumstances = nDeliusClient.getPersonalDetailsAndCircumstancesByCrn(identifier)
    log.debug("Fetching HomeOffice Interest for crn {}", identifier)
    val homeOfficeInterest = nDeliusClient.getHomeOfficeInterestByCrn(identifier)

    return PersonDetailsAndCircumstances.from(personalCircumstances, homeOfficeInterest)
  }

  fun getCommunityManagerByIdentifier(identifier: String): CommunityManagerDto {
    log.debug("Fetching Community Manager for crn {}", identifier)
    return nDeliusClient.getCommunityManagerByCrn(identifier)
  }

  fun getOffenceSentenceByIdentifier(identifier: String): OffenceSentenceDto {
    log.debug("Fetching offence sentence for crn {}", identifier)
    return nDeliusClient.getOffenceSentenceByCrn(identifier)
  }
}
