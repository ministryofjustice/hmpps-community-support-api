package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import java.time.LocalDate

data class IcsViewOrChangeBffDto(
  val referralFirstName: String,
  val referralLastName: String,
  val appointmentDate: LocalDate,
  val appointmentTime: AppointmentTimeResponse,
  val sessionMethod: SessionMethod,
  val sessionCommunications: List<String>,
  val changeAppointmentDetails: ChangeAppointmentDetails?,
) {
  companion object {
    fun from(appointment: AppointmentIcsResponse): IcsViewOrChangeBffDto = IcsViewOrChangeBffDto(
      referralFirstName = appointment.referralFirstName,
      referralLastName = appointment.referralLastName,
      appointmentDate = appointment.appointmentDate,
      appointmentTime = appointment.appointmentTime,
      sessionMethod = appointment.sessionMethod,
      sessionCommunications = appointment.sessionCommunications,
      changeAppointmentDetails = appointment.changeAppointmentDetails?.takeIf {
        it.changeRequestedBy != null || it.reasonForChange != null
      },
    )
  }
}
