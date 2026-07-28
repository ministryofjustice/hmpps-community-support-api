package uk.gov.justice.digital.hmpps.communitysupportapi.model

data class NeedsInterpreterRequest(
  val needsInterpreter: Boolean? = null,
  val language: String? = null,
) {
  fun normaliseAgainstNeedsInterpreter(): NeedsInterpreterRequest = if (needsInterpreter == true) {
    this
  } else {
    copy(
      language = null,
    )
  }
}
