package uk.gov.justice.digital.hmpps.communitysupportapi.util

/**
 * A functional interface for resolving placeholder tokens in template strings
 * to their corresponding values.
 */
fun interface Placeholders {
  fun resolve(tokens: Set<String>): Map<String, String>
}
