package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentHistory
import java.time.format.DateTimeFormatter

data class IcsFeedbackSessionDto(
  val fullName: String,
  val appointmentDetails: AppointmentDetailsDto? = null,
  val otherAppointmentMethods: List<String>? = null,
) {
  companion object {
    fun from(fullName: String, appointmentHistory: AppointmentHistory): IcsFeedbackSessionDto = IcsFeedbackSessionDto(
      fullName = fullName,
      appointmentDetails = AppointmentDetailsDto(
        method = appointmentHistory.appointmentDelivery?.method,
        date = appointmentHistory.appointmentDateTime.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
        time = appointmentHistory.appointmentDateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
      ),
      otherAppointmentMethods = appointmentHistory.sessionCommunication,
    )
  }
}
