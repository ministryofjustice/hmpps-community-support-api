package uk.gov.justice.digital.hmpps.communitysupportapi.dto.cpr

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls

data class CprPersonDto(
  val cprUUID: String? = null,
  val firstName: String? = null,
  val middleNames: String? = null,
  val lastName: String? = null,
  val dateOfBirth: String? = null,
  val disability: Boolean? = null,
  val interestToImmigration: Boolean? = null,
  val title: CprCodeDescriptionDto? = null,
  val sex: CprCodeDescriptionDto? = null,
  val sexualOrientation: CprCodeDescriptionDto? = null,
  val religion: CprCodeDescriptionDto? = null,
  val ethnicity: CprCodeDescriptionDto? = null,
  @JsonSetter(nulls = Nulls.AS_EMPTY)
  val aliases: List<CprAliasDto> = emptyList(),
  @JsonSetter(nulls = Nulls.AS_EMPTY)
  val nationalities: List<CprCodeDescriptionDto> = emptyList(),
  @JsonSetter(nulls = Nulls.AS_EMPTY)
  val addresses: List<CprAddressDto> = emptyList(),
  val identifiers: CprIdentifiersDto,
)

data class CprCodeDescriptionDto(
  val code: String? = null,
  val description: String? = null,
)

data class CprAliasDto(
  val firstName: String? = null,
  val lastName: String? = null,
  val middleNames: String? = null,
  val title: CprCodeDescriptionDto? = null,
  val sex: CprCodeDescriptionDto? = null,
)

data class CprAddressDto(
  val cprAddressId: String,
  val noFixedAbode: Boolean? = null,
  val startDate: String? = null,
  val endDate: String? = null,
  val postcode: String? = null,
  val subBuildingName: String? = null,
  val buildingName: String? = null,
  val buildingNumber: String? = null,
  val thoroughfareName: String? = null,
  val dependentLocality: String? = null,
  val postTown: String? = null,
  val county: String? = null,
  val country: String? = null,
  val countryCode: String? = null,
  val uprn: String? = null,
  val status: CprCodeDescriptionDto? = null,
  val comment: String? = null,
  val typeVerified: Boolean? = null,
  @JsonSetter(nulls = Nulls.AS_EMPTY)
  val usages: List<CprAddressUsageDto> = emptyList(),
  @JsonSetter(nulls = Nulls.AS_EMPTY)
  val contacts: List<CprContactDto> = emptyList(),
)

data class CprContactDto(
  val type: CprCodeDescriptionDto? = null,
  val value: String? = null,
  val extension: String? = null,
)

data class CprAddressUsageDto(
  val code: String? = null,
  val description: String? = null,
  val isActive: Boolean = false,
)

data class CprIdentifiersDto(
  @JsonSetter(nulls = Nulls.AS_EMPTY)
  val crns: List<String> = emptyList(),
  @JsonSetter(nulls = Nulls.AS_EMPTY)
  val prisonNumbers: List<String> = emptyList(),
  @JsonSetter(nulls = Nulls.AS_EMPTY)
  val defendantIds: List<String> = emptyList(),
  @JsonSetter(nulls = Nulls.AS_EMPTY)
  val cids: List<String> = emptyList(),
  @JsonSetter(nulls = Nulls.AS_EMPTY)
  val pncs: List<String> = emptyList(),
  @JsonSetter(nulls = Nulls.AS_EMPTY)
  val cros: List<String> = emptyList(),
  @JsonSetter(nulls = Nulls.AS_EMPTY)
  val nationalInsuranceNumbers: List<String> = emptyList(),
  @JsonSetter(nulls = Nulls.AS_EMPTY)
  val driverLicenseNumbers: List<String> = emptyList(),
  @JsonSetter(nulls = Nulls.AS_EMPTY)
  val arrestSummonsNumbers: List<String> = emptyList(),
)
