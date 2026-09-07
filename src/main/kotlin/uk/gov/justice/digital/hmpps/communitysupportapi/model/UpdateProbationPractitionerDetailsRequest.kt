package uk.gov.justice.digital.hmpps.communitysupportapi.model

import java.util.UUID

data class UpdateProbationPractitionerDetailsRequest(
  val name: String,
  val jobRole: String? = null,
  val emailAddress: String? = null,
  val pdu: UUID? = null,
  val probationOffice: String? = null,
  val teamPhoneNumber: String? = null,
  val phoneNumber: String? = null,
  val ppDetailsFoundAndCorrect: Boolean? = null,
)
