package uk.gov.justice.digital.hmpps.communitysupportapi.service.placeholder

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.util.PersonPlaceholder
import uk.gov.justice.digital.hmpps.communitysupportapi.util.Placeholders

@Component
class PlaceholderFactory {
  fun getForPerson(tokens: Set<String>, person: Person): Array<Placeholders> = buildList {
    if (PersonPlaceholder.neededFor(tokens)) {
      add(PersonPlaceholder(person))
    }
  }.toTypedArray()
}
