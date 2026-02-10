package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.PersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.mapper.toPersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonIdentifier
import uk.gov.justice.digital.hmpps.communitysupportapi.validation.PersonIdentifierValidator

@Service
class PersonService(
  private val deliusService: DeliusService,
  private val nomisService: NomisService,
  private val identifierValidator: PersonIdentifierValidator,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
  fun getPerson(personIdentifier: String): PersonDto {
    log.info("Received person lookup request for identifier: {}", personIdentifier)

    val identifier = identifierValidator.validate(personIdentifier)

    val personAggregate = requireNotNull(
      when (identifier) {
        is PersonIdentifier.Crn -> deliusService.getPersonDetailsByCrn(identifier.value)
        is PersonIdentifier.PrisonerNumber -> nomisService.getPersonDetailsByPrisonerNumber(identifier.value)
      },
    )
    return personAggregate.toPersonDto()
  }
}
