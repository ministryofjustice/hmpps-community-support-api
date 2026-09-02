package uk.gov.justice.digital.hmpps.communitysupportapi.model

data class UpdateProbationPractitionerDetailsRequest(
  val name: String,
  val jobRole: String? = null,
  val emailAddress: String? = null,
  val pdu: String? = null,
  val probationOffice: String? = null,
  val teamPhoneNumber: String? = null,
  val ppDetailsFoundAndCorrect: Boolean? = null,
)
