package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentHistory
import java.util.UUID

data class IcsFeedbackCheckAnswersBffDto(
  val referralFirstName: String,
  val appointmentIcsId: UUID,
) {
  companion object {
    fun from(referralFirstName: String, appointmentHistory: AppointmentHistory): IcsFeedbackCheckAnswersBffDto = IcsFeedbackCheckAnswersBffDto(
      referralFirstName = referralFirstName,
      appointmentIcsId = appointmentHistory.id,
    )
  }
}
