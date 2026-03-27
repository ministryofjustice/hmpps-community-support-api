package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentStatusHistoryType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentType
import java.time.LocalDateTime
import java.util.UUID

data class ReferralAppointmentHistoryDto(
  val appointmentId: UUID,
  val type: AppointmentType,
  val dateTime: LocalDateTime,
  val status: AppointmentStatusHistoryType,
)
