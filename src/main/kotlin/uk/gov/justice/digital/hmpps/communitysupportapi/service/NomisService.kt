package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.communitysupportapi.client.NomisClient
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.nomis.NomisPersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException

@Service
class NomisService(
  private val nomisClient: NomisClient,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getPersonDetailsByPrisonerNumber(id: String): NomisPersonDto {
    log.info("Received Prisoner Number: $id, will call Nomis client to retrieve person details")

    return nomisClient.getPersonByPrisonerNumber(id)
      ?: throw NotFoundException("Person not found in Nomis with identifier: $id")
  }
}
