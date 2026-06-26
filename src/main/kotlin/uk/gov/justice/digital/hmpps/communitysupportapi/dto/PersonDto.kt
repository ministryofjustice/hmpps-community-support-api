package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonAdditionalDetails
import java.util.UUID

data class PersonDto(
  val id: UUID,
  val personIdentifier: String?,
  val title: String? = null,
  val firstName: String,
  val middleNames: String? = null,
  val lastName: String,
  val dateOfBirth: String,
  val sex: String?,
  val prisonNumbers: List<String> = emptyList(),
  val additionalDetails: PersonAdditionalDetails?,
)
