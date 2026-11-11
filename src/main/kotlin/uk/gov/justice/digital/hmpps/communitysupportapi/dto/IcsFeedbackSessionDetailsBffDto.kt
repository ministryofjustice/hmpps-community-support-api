package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import java.time.LocalDate

data class IcsFeedbackSessionDetailsBffDto(
  val referralFirstName: String,
  val date: LocalDate,
  val time: AppointmentTimeResponse,
)
