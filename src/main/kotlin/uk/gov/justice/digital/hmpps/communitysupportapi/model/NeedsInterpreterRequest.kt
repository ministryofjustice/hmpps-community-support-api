package uk.gov.justice.digital.hmpps.communitysupportapi.model

data class NeedsInterpreterRequest(
  val needsInterpreter: Boolean = false,
  val language: String? = null,
) {
  fun normaliseAgainstNeedsInterpreter(): NeedsInterpreterRequest = if (needsInterpreter) {
    this
  } else {
    copy(
      language = null,
    )
  }
}
