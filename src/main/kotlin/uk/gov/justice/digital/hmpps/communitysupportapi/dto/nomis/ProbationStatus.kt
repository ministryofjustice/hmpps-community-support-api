package uk.gov.justice.digital.hmpps.communitysupportapi.dto.nomis

data class ProbationStatus(
  val status: String? = null,
  val previouslyKnownTerminationDate: String? = null,
  val inBreach: Boolean = false,
  val preSentenceActivity: Boolean = false,
  val awaitingPsr: Boolean = false
)
