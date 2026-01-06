package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.communitysupportapi.client.DeliusClient
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.DeliusPersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException

@Service
class DeliusService(
  private val deliusClient: DeliusClient,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getPersonDetailsByCrn(crn: String): DeliusPersonDto {
    log.info("Received CRN identifier: $crn, will call Delius client to retrieve person details")

    return deliusClient.getPersonByCrn(crn)
      ?: throw NotFoundException("Person not found in Delius with identifier: $crn")
  }
}
