package uk.gov.justice.digital.hmpps.communitysupportapi.dto

data class IcsFeedbackSessionDto(
  val fullName: String,
  val appointmentDetails: AppointmentDetailsDto? = null,
  val otherAppointmentMethods: List<String>? = null,
)
