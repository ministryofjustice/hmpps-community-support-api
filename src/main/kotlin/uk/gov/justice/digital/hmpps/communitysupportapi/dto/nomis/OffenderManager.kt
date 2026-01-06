package uk.gov.justice.digital.hmpps.communitysupportapi.dto.nomis

data class OffenderManager(
  val trustOfficer: TrustOfficer? = null,
  val staff: Staff? = null,
  val providerEmployee: ProviderEmployee? = null,
  val partitionArea: String? = null,
  val softDeleted: Boolean = false,
  val team: Team? = null,
  val probationArea: ProbationArea? = null,
  val fromDate: String? = null,
  val toDate: String? = null,
  val active: Boolean = true,
  val allocationReason: CodeDescription? = null,
)

data class TrustOfficer(
  val forenames: String? = null,
  val surname: String? = null,
)

data class Staff(
  val code: String? = null,
  val forenames: String? = null,
  val surname: String? = null,
  val unallocated: Boolean = false,
)

data class ProviderEmployee(
  val forenames: String? = null,
  val surname: String? = null,
)

data class Team(
  val code: String? = null,
  val description: String? = null,
  val telephone: String? = null,
  val localDeliveryUnit: CodeDescription? = null,
  val district: CodeDescription? = null,
  val borough: CodeDescription? = null,
)

data class ProbationArea(
  val probationAreaId: Int? = null,
  val code: String? = null,
  val description: String? = null,
  val nps: Boolean = false,
  val organisation: CodeDescription? = null,
  val institution: Institution? = null,
  val teams: List<Team> = emptyList(),
)

data class Institution(
  val institutionId: Int? = null,
  val isEstablishment: Boolean = true,
  val code: String? = null,
  val description: String? = null,
  val institutionName: String? = null,
  val establishmentType: CodeDescription? = null,
  val isPrivate: Boolean = true,
  val nomsPrisonInstitutionCode: String? = null,
)
