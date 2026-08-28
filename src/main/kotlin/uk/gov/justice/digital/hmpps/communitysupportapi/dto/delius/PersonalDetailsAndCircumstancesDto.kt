package uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius

import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CodeDescriptionDto
import java.time.OffsetDateTime

data class PersonalDetailsAndCircumstancesDto(
  val preferredLanguage: CodeDescriptionDto? = null,
  val personalCircumstances: List<PersonalCircumstanceDto> = emptyList(),
  val disabilities: List<DisabilitiesDto> = emptyList(),
  val offenderPersonalityDisorder: OffenderPersonalityDisorderDto? = null,
)

data class PersonalCircumstanceDto(
  val type: CodeDescriptionDto? = null,
  val subType: CodeDescriptionDto? = null,
  val updatedAt: OffsetDateTime? = null,
)

data class DisabilitiesDto(
  val type: CodeDescriptionDto? = null,
  val updatedAt: OffsetDateTime? = null,
)

data class OffenderPersonalityDisorderDto(
  val status: CodeDescriptionDto? = null,
)

data class HomeOfficeInterestDto(
  val exists: Boolean? = null,
  val notes: String? = null,
)
