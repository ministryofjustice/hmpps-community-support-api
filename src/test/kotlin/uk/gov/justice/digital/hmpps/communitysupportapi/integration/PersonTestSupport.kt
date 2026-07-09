package uk.gov.justice.digital.hmpps.communitysupportapi.integration

import org.springframework.stereotype.Component

@Component
class PersonTestSupport {
  /** CRNs have the format Letter + 6 numbers, e.g. "A123456" */
  fun generateCrn(): String {
    val letter = ('a'..'z').random().toString()
    val numbers = (1..6).map { (0..9).random() }.joinToString("")
    return letter.uppercase() + numbers
  }
}
