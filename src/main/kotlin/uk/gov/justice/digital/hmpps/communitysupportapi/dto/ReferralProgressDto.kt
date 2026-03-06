package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentStatusHistoryType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentType
import java.time.LocalDateTime
import java.util.UUID

data class ReferralProgressDto(
  val referralId: UUID,
  val appointmentId: UUID,
  val appointmentType: AppointmentType,
  val appointmentDateTime: LocalDateTime,
  val status: AppointmentStatusHistoryType,
)
