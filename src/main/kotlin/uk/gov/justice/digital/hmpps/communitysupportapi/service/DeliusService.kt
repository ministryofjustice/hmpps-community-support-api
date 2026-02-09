package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.communitysupportapi.client.DeliusClient
import uk.gov.justice.digital.hmpps.communitysupportapi.mapper.toAdditionalDetails
import uk.gov.justice.digital.hmpps.communitysupportapi.mapper.toPerson
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonAggregate

@Service
class DeliusService(
  private val deliusClient: DeliusClient,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
  fun getPersonDetailsByCrn(crn: String): PersonAggregate {
    val deliusPersonDto = deliusClient.getPersonByCrn(crn)
    return PersonAggregate(
      person = deliusPersonDto.toPerson(),
      additionalDetails = deliusPersonDto.toAdditionalDetails(),
    )
  }
}
