package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import java.util.UUID

data class IcsFeedbackCheckAnswersBffDto(
  val referralFirstName: String,
  val appointmentIcsId: UUID,
)
