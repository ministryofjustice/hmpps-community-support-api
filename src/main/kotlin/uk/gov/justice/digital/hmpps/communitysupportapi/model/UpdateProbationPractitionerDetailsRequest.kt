package uk.gov.justice.digital.hmpps.communitysupportapi.model

import java.util.UUID

data class UpdateProbationPractitionerDetailsRequest(
  val name: String,
  val jobRole: String? = null,
  val emailAddress: String? = null,
  val pduId: UUID? = null,
  val probationOfficeId: Int? = null,
  val teamPhoneNumber: String? = null,
  val phoneNumber: String? = null,
  val ppDetailsFoundAndCorrect: Boolean? = null,
)
