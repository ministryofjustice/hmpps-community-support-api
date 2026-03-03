package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import java.time.LocalDateTime

data class AppointmentIcsDto(
  val appointmentDelivery: AppointmentDeliveryDto?,
  val appointmentDateTime: LocalDateTime,
  val createdAt: LocalDateTime,
  val createdBy: UserDto,
  val sessionCommunication: List<String>,
)
