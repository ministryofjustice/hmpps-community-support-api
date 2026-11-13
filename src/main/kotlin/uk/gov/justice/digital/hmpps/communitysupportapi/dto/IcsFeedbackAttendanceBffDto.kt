package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentHistory
import java.time.LocalDate

data class IcsFeedbackAttendanceBffDto(
  val referralFirstName: String,
  val date: LocalDate,
  val time: AppointmentTimeResponse,
) {
  companion object {
    fun from(referralFirstName: String, appointmentHistory: AppointmentHistory): IcsFeedbackAttendanceBffDto = IcsFeedbackAttendanceBffDto(
      referralFirstName = referralFirstName,
      date = appointmentHistory.appointmentDateTime.toLocalDate(),
      time = appointmentHistory.appointmentDateTime.toLocalTime().toAppointmentTimeResponse(),
    )
  }
}
