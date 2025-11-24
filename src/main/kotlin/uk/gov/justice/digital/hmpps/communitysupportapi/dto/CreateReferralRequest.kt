package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import java.time.LocalDate

data class CreateReferralRequest(
  val firstName: String? = null,
  val lastName: String? = null,
  val crn: String,
  val referenceNumber: String,
  val sex: String? = null,
  val dateOfBirth: LocalDate? = null,
  val ethnicity: String? = null,
)