package uk.gov.justice.digital.hmpps.communitysupportapi.util

fun interface Placeholders {
  fun resolve(tokens: Set<String>): Map<String, String>
}
