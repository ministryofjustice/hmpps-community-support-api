package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonAdditionalDetails
import java.time.LocalDate
import java.util.UUID

data class PersonDto(
  val id: UUID,
  val personIdentifier: String?,
  val firstName: String,
  val lastName: String,
  val dateOfBirth: LocalDate,
  val sex: String?,
  val additionalDetails: PersonAdditionalDetails?,
)
