package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.PersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.mapper.toEntity
import uk.gov.justice.digital.hmpps.communitysupportapi.mapper.toPersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonAggregate
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
  fun getPerson(personIdentifier: String): Mono<PersonDto> {
    val identifier = identifierValidator.validate(personIdentifier)

    val personAggregate: Mono<PersonAggregate> = when (identifier) {
      is PersonIdentifier.Crn ->
        deliusService.getPersonDetailsByCrn(identifier.value)
      is PersonIdentifier.PrisonerNumber ->
        nomisService.getPersonDetailsByPrisonerNumber(identifier.value)
    }

    return personAggregate.flatMap { aggregate ->
      Mono.fromCallable {
        personRepository.save(aggregate.toEntity())
        aggregate.toPersonDto()
      }.subscribeOn(Schedulers.boundedElastic())
    }
  }
}
