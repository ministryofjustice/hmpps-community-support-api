package uk.gov.justice.digital.hmpps.communitysupportapi.service

import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.communitysupportapi.client.NDeliusClient
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.CommunityManagerDto
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonAggregate
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonDetailsAndCircumstances
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonIdentifier
import uk.gov.justice.digital.hmpps.communitysupportapi.service.DraftReferralService.Companion.logger

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

  fun getPersonalDetailsAndCircumstances(cprPerson: PersonAggregate): PersonDetailsAndCircumstances = when(val identifier = cprPerson.person.identifier){
        is PersonIdentifier.Crn -> getPersonalDetailsAndCircumstancesByIdentifier(identifier.value)
        is PersonIdentifier.PrisonerNumber -> {
          if (cprPerson.person.knownCrns.isNotEmpty()) {
            val crn = cprPerson.person.knownCrns.first()
            getPersonalDetailsAndCircumstancesByIdentifier(crn)
          } else {
            log.warn("No known CRN found for person with prison identifier {}", identifier.value)
            PersonDetailsAndCircumstances()
          }
        }
    }

  fun getCommunityManagerByIdentifier(identifier: String): CommunityManagerDto? {
    log.debug("Fetching Community Manager for crn {}", identifier)
    try {
      return nDeliusClient.getCommunityManagerByCrn(identifier)
    } catch (_: NotFoundException) {
      log.warn("Unable to find community manager for crn {}", identifier)
      return null
    } catch (_: ValidationException) {
      log.warn("Unable to find community manager for crn {}, ValidationException returned", identifier)
      return null
    }
  }
}
