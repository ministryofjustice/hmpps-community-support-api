package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentIcsFeedback
import java.time.LocalDateTime
import java.util.UUID

data class SessionDurationRequest(
  val hours: Int,
  val minutes: Int? = 0,
)

/**
 * Converts a [SessionDurationRequest] into a human-readable string such as
 * "1 hour and 45 minutes", "2 hours", "30 minutes".
 */
fun SessionDurationRequest.toDisplayString(): String {
  val hoursText = when (hours) {
    0 -> null
    1 -> "1 hour"
    else -> "$hours hours"
  }
  val mins = minutes ?: 0
  val minutesText = when (mins) {
    0 -> null
    1 -> "1 minute"
    else -> "$mins minutes"
  }
  return listOfNotNull(hoursText, minutesText).joinToString(" and ").ifBlank { "0 minutes" }
}

data class RecordSessionRequest(
  val didSessionHappen: Boolean,
  val howSessionTookPlace: SessionMethodRequest? = null,
)

data class SessionDetailsRequest(
  val wasPersonLate: Boolean? = null,
  val lateReason: String? = null,
  val duration: SessionDurationRequest? = null,
)

data class SessionFeedbackRequest(
  val whatHappened: String? = null,
  val behaviour: String? = null,
  val strengthsIdentified: String? = null,
)

data class IssuesAndConcernsRequest(
  val identified: String? = null,
  val notifyProbationPractitioner: Boolean? = null,
)

data class NextStepsRequest(
  val plannedForNextSession: String? = null,
  val actionsBeforeNextSession: String? = null,
)

data class CreateIcsFeedbackRequest(
  val record: RecordSessionRequest,
  val sessionDetails: SessionDetailsRequest? = null,
  val sessionFeedback: SessionFeedbackRequest? = null,
  val issuesAndConcerns: IssuesAndConcernsRequest? = null,
  val nextSteps: NextStepsRequest? = null,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "ICS appointment session feedback")
data class AppointmentIcsFeedbackResponse(

  val id: UUID,
  val appointmentIcsId: UUID,

  // Section 1 – Record session attendance
  val recordSessionDidSessionHappen: Boolean,
  val recordSessionHowSessionTookPlace: String?,
  val recordSessionNotInPersonReason: String?,
  val recordSessionPdu: String?,
  val recordSessionAddressLine1: String?,
  val recordSessionAddressLine2: String?,
  val recordSessionTownOrCity: String?,
  val recordSessionCounty: String?,
  val recordSessionPostcode: String?,

  // Section 2 – Session details
  val sessionDetailsWasPersonLate: Boolean?,
  val sessionDetailsLateReason: String?,
  val sessionDetailsDuration: String?,

  // Section 3 – Session feedback
  val sessionFeedbackWhatHappened: String?,
  val sessionFeedbackBehaviour: String?,
  val sessionFeedbackStrengthsIdentified: String?,

  // Section 4 – Issues or concerns
  val issuesOrConcernsIdentified: String?,
  val issuesOrConcernsNotifyProbationPractitioner: Boolean?,

  // Section 5 – Next steps
  val nextStepsPlannedForNextSession: String?,
  val nextStepsActionsBeforeNextSession: String?,

  // Audit
  val createdAt: LocalDateTime,
  val createdBy: UUID?,
) {
  companion object {
    fun from(feedback: AppointmentIcsFeedback) = AppointmentIcsFeedbackResponse(
      id = feedback.id,
      appointmentIcsId = feedback.appointmentIcs.id,
      recordSessionDidSessionHappen = feedback.recordSessionDidSessionHappen,
      recordSessionHowSessionTookPlace = feedback.recordSessionHowSessionTookPlace,
      recordSessionNotInPersonReason = feedback.recordSessionNotInPersonReason,
      recordSessionPdu = feedback.recordSessionPdu,
      recordSessionAddressLine1 = feedback.recordSessionAddressLine1,
      recordSessionAddressLine2 = feedback.recordSessionAddressLine2,
      recordSessionTownOrCity = feedback.recordSessionTownOrCity,
      recordSessionCounty = feedback.recordSessionCounty,
      recordSessionPostcode = feedback.recordSessionPostcode,
      sessionDetailsWasPersonLate = feedback.sessionDetailsWasPersonLate,
      sessionDetailsLateReason = feedback.sessionDetailsLateReason,
      sessionDetailsDuration = feedback.sessionDetailsDuration,
      sessionFeedbackWhatHappened = feedback.sessionFeedbackWhatHappened,
      sessionFeedbackBehaviour = feedback.sessionFeedbackBehaviour,
      sessionFeedbackStrengthsIdentified = feedback.sessionFeedbackStrengthsIdentified,
      issuesOrConcernsIdentified = feedback.issuesConcernsIdentified,
      issuesOrConcernsNotifyProbationPractitioner = feedback.issuesConcernsNotifyProbationPractitioner,
      nextStepsPlannedForNextSession = feedback.nextStepsPlannedForNextSession,
      nextStepsActionsBeforeNextSession = feedback.nextStepsActionsBeforeNextSession,
      createdAt = feedback.createdAt,
      createdBy = feedback.createdBy?.id,
    )
  }
}
