package uk.gov.justice.digital.hmpps.communitysupportapi.dto

data class WithdrawalReasonsGroupedBffResponseDto(
  val withdrawalReasons: Map<String, List<String>>,
)
