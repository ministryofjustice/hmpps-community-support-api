package uk.gov.justice.digital.hmpps.communitysupportapi.dto.nomis

data class OffenderProfile(
  val ethnicity: String? = null,
  val nationality: String? = null,
  val secondaryNationality: String? = null,
  val notes: String? = null,
  val immigrationStatus: String? = null,
  val offenderLanguages: OffenderLanguages? = null,
  val religion: String? = null,
  val sexualOrientation: String? = null,
  val offenderDetails: String? = null,
  val remandStatus: String? = null,
  val previousConviction: PreviousConviction? = null,
  val riskColour: String? = null,
  val disabilities: List<Disability> = emptyList(),
  val provisions: List<Provision> = emptyList()
)

data class OffenderLanguages(
  val primaryLanguage: String? = null,
  val otherLanguages: List<String> = emptyList(),
  val languageConcerns: String? = null,
  val requiresInterpreter: Boolean = false
)

data class PreviousConviction(
  val convictionDate: String? = null,
  val detail: Map<String, String> = emptyMap()
)

data class Disability(
  val disabilityId: Int? = null,
  val disabilityType: CodeDescription? = null,
  val condition: CodeDescription? = null,
  val startDate: String? = null,
  val endDate: String? = null,
  val notes: String? = null
)

data class Provision(
  val provisionId: Int? = null,
  val provisionType: CodeDescription? = null,
  val category: CodeDescription? = null,
  val startDate: String? = null,
  val endDate: String? = null,
  val notes: String? = null
)
