package uk.gov.justice.digital.hmpps.communitysupportapi.model

data class AdditionalSupportNeedsRequest(
  val physicalHealth: String? = null,
  val mentalEmotionalHealth: String? = null,
  val neurodiversity: String? = null,
  val locationTravel: String? = null,
  val caringResponsibilities: String? = null,
  val employmentResponsibilities: String? = null,
  val diversity: String? = null,
  val anythingElse: String? = null,
  val needsAdditionalSupport: Boolean = false,
) {
  /**
   * When `needsAdditionalSupport` is `false` then all other fields should be set to `null`, so we are
   * not holding conflicting information (e.g. a person cannot *not* have additional support needs _and_
   * and caring responsibilities)
   */
  fun normaliseAgainstNeedsAdditionalSupport(): AdditionalSupportNeedsRequest = if (needsAdditionalSupport) {
    this
  } else {
    copy(
      physicalHealth = null,
      mentalEmotionalHealth = null,
      neurodiversity = null,
      locationTravel = null,
      caringResponsibilities = null,
      employmentResponsibilities = null,
      diversity = null,
      anythingElse = null,
    )
  }
}
