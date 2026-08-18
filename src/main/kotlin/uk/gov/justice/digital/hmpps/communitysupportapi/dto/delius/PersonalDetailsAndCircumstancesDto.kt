package uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius

import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CodeDescriptionDto
import java.time.LocalDate

data class PersonalDetailsAndCircumstancesDto(
  val preferredLanguage: CodeDescriptionDto? = null,
  val personalCircumstances: List<PersonalCircumstanceDto> = emptyList(),
  val disabilities: List<DisabilitiesDto> = emptyList(),
  val offenderPersonalityDisorder: OffenderPersonalityDisorderDto? = null,
)

data class PersonalCircumstanceDto(
  val type: CodeDescriptionDto? = null,
  val subtype: CodeDescriptionDto? = null,
  val updatedAt: LocalDate? = null,
)

data class DisabilitiesDto(
  val type: CodeDescriptionDto? = null,
  val updatedAt: LocalDate? = null,
)

data class OffenderPersonalityDisorderDto(
  val status: CodeDescriptionDto? = null,
)

data class HomeOfficeInterestDto(
  val exists: Boolean? = null,
  val notes: String? = null,
)
