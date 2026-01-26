package uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius

data class ProbationStatusDto(
  val status: String? = null,
  val previouslyKnownTerminationDate: String? = null,
  val inBreach: Boolean? = null,
  val preSentenceActivity: Boolean? = null,
  val awaitingPsr: Boolean? = null,
)
