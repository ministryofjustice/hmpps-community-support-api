package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.communitysupportapi.client.NomisClient
import uk.gov.justice.digital.hmpps.communitysupportapi.mapper.toAdditionalDetails
import uk.gov.justice.digital.hmpps.communitysupportapi.mapper.toPerson
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonAggregate

@Service
class NomisService(
  private val nomisClient: NomisClient,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
  fun getPersonDetailsByPrisonerNumber(prisonerNumber: String): PersonAggregate {
    val nomisPersonDto = nomisClient.getPersonByPrisonerNumber(prisonerNumber)
    return PersonAggregate(
      person = nomisPersonDto.toPerson(),
      additionalDetails = nomisPersonDto.toAdditionalDetails(),
    )
  }
}
