package uk.gov.justice.digital.hmpps.communitysupportapi.dto

data class ReferralProgressDto(
  val appointment: AppointmentDto,
  val appointmentIcs: AppointmentIcsDto,
  val appointmentStatusHistory: List<AppointmentStatusHistoryDto>,
)
