package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentStatusHistoryType
import java.time.LocalDateTime

data class AppointmentStatusHistoryDto(
  val status: AppointmentStatusHistoryType,
  val createdAt: LocalDateTime,
)
