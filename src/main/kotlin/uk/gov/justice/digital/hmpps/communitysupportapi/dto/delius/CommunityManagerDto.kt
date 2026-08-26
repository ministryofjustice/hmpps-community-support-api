package uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius

data class CommunityManagerDto(
  val crn: String? = null,
  val communityManager: CommunityManagerDetailsDto? = null,
)

data class CommunityManagerDetailsDto(
  val jobRole: String? = null,
  val emailAddress: String? = null,
  val pdu: String? = null,
  val officeName: String? = null,
  val name: CommunityManagerNameDto? = null,
  val teamPhoneNumber: String? = null,
)

data class CommunityManagerNameDto(
  val forename: String? = null,
  val middleName: String? = null,
  val surname: String? = null,
)
