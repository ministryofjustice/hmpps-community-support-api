package uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius

import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CodeDescriptionDto
import java.time.LocalDateTime

data class PersonDetailsAndCircumstancesDto(
  val preferredLanguage: CodeDescriptionDto? = null,
  val personCircumstances: List<PersonCircumstanceDto> = emptyList(),
  val disabilities: List<DisabilitiesDto> = emptyList(),
  val offenderPersonalityDisorder: OffenderPersonalityDisorderDto? = null,
)

data class PersonCircumstanceDto(
  val type: CodeDescriptionDto? = null,
  val subtype: CodeDescriptionDto? = null,
  val updatedAt: LocalDateTime? = null,
)

data class DisabilitiesDto(
  val type: CodeDescriptionDto? = null,
  val updatedAt: LocalDateTime? = null,
)

data class OffenderPersonalityDisorderDto(
  val status: CodeDescriptionDto? = null,
)

data class HomeOfficeInterestDto(
  val exists: Boolean? = null,
  val notes: String? = null,
)
