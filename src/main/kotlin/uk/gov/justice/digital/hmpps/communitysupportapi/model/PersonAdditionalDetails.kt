package uk.gov.justice.digital.hmpps.communitysupportapi.model

data class PersonAdditionalDetails(
  val ethnicity: String? = null,
  val preferredLanguage: String? = null,
  val neurodiverseConditions: String? = null,
  val religionOrBelief: String? = null,
  val transgender: String? = null,
  val sexualOrientation: String? = null,
  val address: String? = null,
  val phoneNumber: String? = null,
  val emailAddress: String? = null,
)
