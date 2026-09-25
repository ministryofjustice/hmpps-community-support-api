package uk.gov.justice.digital.hmpps.communitysupportapi.util

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person

class PersonPlaceholder(
  private val person: Person,
) : Placeholders {

  override fun resolve(tokens: Set<String>): Map<String, String> = buildMap {
    if (FIRST_NAME in tokens) put(FIRST_NAME, person.firstName)
    if (LAST_NAME in tokens) put(LAST_NAME, person.lastName)
    if (FULL_NAME in tokens) {
      put(
        FULL_NAME,
        listOf(person.firstName, person.lastName)
          .filter { it.isNotBlank() }
          .joinToString(" "),
      )
    }
  }

  companion object {
    const val FIRST_NAME = "firstName"
    const val LAST_NAME = "lastName"
    const val FULL_NAME = "fullName"

    val TOKENS = setOf(FIRST_NAME, LAST_NAME, FULL_NAME)

    fun neededFor(tokens: Set<String>): Boolean = tokens.any { it in TOKENS }
  }
}
