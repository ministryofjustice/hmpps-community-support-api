package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonAdditionalDetails
import java.time.LocalDate

data class PersonDto(
  val personIdentifier: String?,
  val firstName: String,
  val lastName: String,
  val dateOfBirth: LocalDate,
  val sex: String?,
  val additionalDetails: PersonAdditionalDetails?,
)
