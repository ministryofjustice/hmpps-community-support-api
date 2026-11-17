package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import java.time.LocalDate

data class IcsChangeDetailsBffDto(
  val firstName: String,
  val personIdentifier: String,
  val referralDate: LocalDate,
  val appointmentDate: LocalDate,
  val appointmentTime: AppointmentTimeResponse,
  val sessionMethod: SessionMethod,
  val sessionCommunications: List<String>,
) {
  companion object {
    fun from(referral: Referral, appointment: AppointmentIcsResponse): IcsChangeDetailsBffDto = IcsChangeDetailsBffDto(
      firstName = appointment.referralFirstName,
      personIdentifier = referral.personIdentifier,
      referralDate = referral.createdAt.toLocalDate(),
      appointmentDate = appointment.appointmentDate,
      appointmentTime = appointment.appointmentTime,
      sessionMethod = appointment.sessionMethod,
      sessionCommunications = appointment.sessionCommunications,
    )
  }
}
