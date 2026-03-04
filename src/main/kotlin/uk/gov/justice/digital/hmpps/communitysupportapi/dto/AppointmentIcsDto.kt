package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import java.time.LocalDateTime
import java.util.UUID

data class AppointmentIcsDto(
  val id: UUID,
  val appointmentId: UUID,
  val appointmentDelivery: AppointmentDeliveryDto?,
  val appointmentDateTime: LocalDateTime,
  val createdAt: LocalDateTime,
  val createdBy: UserDto,
  val sessionCommunication: List<String>,
)
