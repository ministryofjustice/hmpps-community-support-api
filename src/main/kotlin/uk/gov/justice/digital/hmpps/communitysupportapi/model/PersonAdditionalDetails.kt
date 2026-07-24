package uk.gov.justice.digital.hmpps.communitysupportapi.model

import java.time.LocalDate

data class PersonAdditionalDetails(
  val ethnicity: String? = null,
  val preferredLanguage: String? = null,
  val neurodiverseConditions: String? = null,
  val religionOrBelief: String? = null,
  val transgender: String? = null,
  val sexualOrientation: String? = null,
  val genderIdentity: String? = null,
  val nationalities: List<String> = emptyList(),
  val interestToImmigration: Boolean? = null,
  val address: String? = null,
  val addressType: String? = null,
  val addressTypeVerified: Boolean = false,
  val addressStartDate: LocalDate? = null,
  val addressNotes: String? = null,
  val noFixedAbode: Boolean? = null,
  val phoneNumber: String? = null,
  val mobileNumber: String? = null,
  val emailAddress: String? = null,
  val disability: Boolean? = null,
)
