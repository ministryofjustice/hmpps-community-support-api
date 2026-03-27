package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import java.util.UUID

data class ReferralProgressDto(
  val referralId: UUID,
  val fullName: String,
  val appointments: List<ReferralAppointmentHistoryDto>,
)
