package uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CodeDescriptionDto
import java.time.LocalDate

data class OffenderProfileDto(
  val ethnicity: String? = null,
  val nationality: String? = null,
  val secondaryNationality: String? = null,
  val notes: String? = null,
  val immigrationStatus: String? = null,
  val offenderLanguages: OffenderLanguagesDto? = null,
  val religion: String? = null,
  val sexualOrientation: String? = null,
  val offenderDetails: String? = null,
  val remandStatus: String? = null,
  val previousConviction: PreviousConvictionDto? = null,
  val riskColour: String? = null,
  @JsonSetter(nulls = Nulls.AS_EMPTY)
  val disabilities: List<DisabilityDto> = emptyList(),
  @JsonSetter(nulls = Nulls.AS_EMPTY)
  val provisions: List<ProvisionDto> = emptyList(),
)

data class OffenderLanguagesDto(
  val primaryLanguage: String? = null,
  @JsonSetter(nulls = Nulls.AS_EMPTY)
  val otherLanguages: List<String> = emptyList(),
  val languageConcerns: String? = null,
  val requiresInterpreter: Boolean? = null,
)

data class PreviousConvictionDto(
  val convictionDate: String? = null,
  @JsonSetter(nulls = Nulls.AS_EMPTY)
  val detail: Map<String, String> = emptyMap(),
)

data class DisabilityDto(
  val disabilityId: Int? = null,
  val disabilityType: CodeDescriptionDto? = null,
  val condition: CodeDescriptionDto? = null,
  val startDate: LocalDate? = null,
  val endDate: LocalDate? = null,
  val notes: String? = null,
)

data class ProvisionDto(
  val provisionId: Int? = null,
  val provisionType: CodeDescriptionDto? = null,
  val category: CodeDescriptionDto? = null,
  val startDate: LocalDate? = null,
  val endDate: LocalDate? = null,
  val notes: String? = null,
)
