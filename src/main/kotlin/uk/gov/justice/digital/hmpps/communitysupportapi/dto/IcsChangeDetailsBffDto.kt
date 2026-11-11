package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import java.time.LocalDate

data class IcsChangeDetailsBffDto(
  val firstName: String,
  val personIdentifier: String,
  val referralDate: LocalDate,
  val appointmentDate: LocalDate,
  val appointmentTime: AppointmentTimeResponse,
  val sessionMethod: SessionMethod,
  val sessionCommunications: List<String>,
)
