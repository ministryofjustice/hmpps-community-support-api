package uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius

import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CodeDescriptionDto

data class OffenderManagerDto(
  val trustOfficer: TrustOfficerDto? = null,
  val staff: StaffDto? = null,
  val providerEmployee: ProviderEmployeeDto? = null,
  val partitionArea: String? = null,
  val softDeleted: Boolean = false,
  val team: TeamDto? = null,
  val probationArea: ProbationAreaDto? = null,
  val fromDate: String? = null,
  val toDate: String? = null,
  val active: Boolean = true,
  val allocationReason: CodeDescriptionDto? = null,
)

data class TrustOfficerDto(
  val forenames: String? = null,
  val surname: String? = null,
)

data class ProviderEmployeeDto(
  val forenames: String? = null,
  val surname: String? = null,
)

data class TeamDto(
  val code: String? = null,
  val description: String? = null,
  val telephone: String? = null,
  val localDeliveryUnit: CodeDescriptionDto? = null,
  val district: CodeDescriptionDto? = null,
  val borough: CodeDescriptionDto? = null,
)

data class ProbationAreaDto(
  val probationAreaId: Int? = null,
  val code: String? = null,
  val description: String? = null,
  val nps: Boolean = false,
  val organisation: CodeDescriptionDto? = null,
  val institution: InstitutionDto? = null,
  val teams: List<ProbationAreaTeamDto> = emptyList(),
)

data class InstitutionDto(
  val institutionId: Int? = null,
  val isEstablishment: Boolean = true,
  val code: String? = null,
  val description: String? = null,
  val institutionName: String? = null,
  val establishmentType: CodeDescriptionDto? = null,
  val isPrivate: Boolean = true,
  val nomsPrisonInstitutionCode: String? = null,
)

data class ProbationAreaTeamDto(
  val providerTeamId: Int? = null,
  val teamId: Int? = null,
  val code: String? = null,
  val description: String? = null,
  val name: String? = null,
  val isPrivate: Boolean = true,
  val externalProvider: CodeDescriptionDto? = null,
  val localDeliveryUnit: CodeDescriptionDto? = null,
  val district: CodeDescriptionDto? = null,
  val borough: CodeDescriptionDto? = null,
)
