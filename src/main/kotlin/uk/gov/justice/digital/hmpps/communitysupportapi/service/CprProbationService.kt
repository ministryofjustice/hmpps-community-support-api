package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.communitysupportapi.client.CorePersonRecordClient
import uk.gov.justice.digital.hmpps.communitysupportapi.mapper.toAdditionalDetails
import uk.gov.justice.digital.hmpps.communitysupportapi.mapper.toPrisonPerson
import uk.gov.justice.digital.hmpps.communitysupportapi.mapper.toProbationPerson
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonAggregate

@Service
class CprProbationService(
  private val corePersonRecordClient: CorePersonRecordClient,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getPersonDetailsByCrn(crn: String): PersonAggregate {
    log.debug("Fetching probation person from Core Person Record for CRN: {}", crn)
    val cprPersonDto = corePersonRecordClient.getPersonByCrn(crn)
    return PersonAggregate(
      person = cprPersonDto.toProbationPerson(),
      additionalDetails = cprPersonDto.toAdditionalDetails(),
    )
  }

  fun getPersonDetailsByPrisonNumber(prisonNumber: String): PersonAggregate {
    log.debug("Fetching prison person from Core Person Record for prison number: {}", prisonNumber)
    val cprPersonDto = corePersonRecordClient.getPersonByPrisonNumber(prisonNumber)
    return PersonAggregate(
      person = cprPersonDto.toPrisonPerson(),
      additionalDetails = cprPersonDto.toAdditionalDetails(),
    )
  }
}
