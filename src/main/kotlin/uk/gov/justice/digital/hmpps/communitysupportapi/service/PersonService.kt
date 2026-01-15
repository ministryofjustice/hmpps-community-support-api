package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.PersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.mapper.toEntity
import uk.gov.justice.digital.hmpps.communitysupportapi.mapper.toPersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonIdentifier
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.validation.PersonIdentifierValidator

@Service
class PersonService(
  private val deliusService: DeliusService,
  private val nomisService: NomisService,
  private val identifierValidator: PersonIdentifierValidator,
  private val personRepository: PersonRepository,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional
  fun getPerson(crnOrPrisonerNumber: String): PersonDto {
    log.info("Received person lookup request for identifier: {}", crnOrPrisonerNumber)

    val personAggregate = when (
      val identifier = identifierValidator.validate(crnOrPrisonerNumber)
    ) {
      is PersonIdentifier.Crn ->
        deliusService.getPersonDetailsByCrn(identifier.value)

      is PersonIdentifier.PrisonerNumber ->
        nomisService.getPersonDetailsByPrisonerNumber(identifier.value)
    }

    val person = personAggregate.toEntity()

    personRepository.save(person)

    return personAggregate.toPersonDto()
  }
}
