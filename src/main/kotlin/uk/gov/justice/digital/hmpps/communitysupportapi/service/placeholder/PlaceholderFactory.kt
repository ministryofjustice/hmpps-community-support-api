package uk.gov.justice.digital.hmpps.communitysupportapi.service.placeholder

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.PersonRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.util.PersonPlaceholder
import uk.gov.justice.digital.hmpps.communitysupportapi.util.Placeholders

@Component
class PlaceholderFactory(
  private val personRepository: PersonRepository,
) {
  fun forReferral(tokens: Set<String>, referral: Referral): Array<Placeholders> {
    if (tokens.isEmpty()) return emptyArray()

    return buildList {
      if (PersonPlaceholder.neededFor(tokens)) {
        val person = personRepository.findById(referral.personId)
          .orElseThrow { NotFoundException("Person not found for referral ${referral.id}") }
        add(PersonPlaceholder(person))
      }
    }.toTypedArray()
  }
}
