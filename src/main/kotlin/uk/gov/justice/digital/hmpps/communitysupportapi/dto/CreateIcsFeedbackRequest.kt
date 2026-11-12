package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentHistory
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentIcsFeedback
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser

class CreateIcsFeedbackRequest(
  val record: RecordSessionRequest,
  val sessionDetails: SessionDetailsRequest? = null,
  val sessionFeedback: SessionFeedbackRequest? = null,
  val issuesAndConcerns: IssuesAndConcernsRequest? = null,
  val newlyIdentifiedAdditionalSupportNeeds: String? = null,
  val nextSteps: NextStepsRequest? = null,
) {
  fun toAppointmentIcsFeedback(ics: AppointmentHistory, submittedBy: ReferralUser): AppointmentIcsFeedback {
    val isOtherLocation = this.record.howSessionTookPlace?.type == SessionMethodType.IN_PERSON_OTHER_LOCATION

    return AppointmentIcsFeedback(
      appointmentIcs = ics,
      recordSessionDidSessionHappen = this.record.didSessionHappen,
      recordSessionHowSessionTookPlace = this.record.howSessionTookPlace?.type?.toSessionDisplayString(),
      recordSessionNotInPersonReason = this.record.howSessionTookPlace?.additionalDetails,
      recordSessionPdu = this.record.howSessionTookPlace?.pdu,
      recordSessionAddressLine1 = this.record.howSessionTookPlace?.addressLine1.takeIf { isOtherLocation },
      recordSessionAddressLine2 = this.record.howSessionTookPlace?.addressLine2.takeIf { isOtherLocation },
      recordSessionTownOrCity = this.record.howSessionTookPlace?.townOrCity.takeIf { isOtherLocation },
      recordSessionCounty = this.record.howSessionTookPlace?.county.takeIf { isOtherLocation },
      recordSessionPostcode = this.record.howSessionTookPlace?.postcode.takeIf { isOtherLocation },
      recordSessionDidPersonAttend = this.record.didPersonAttend,
      recordSessionNotHappenReason = this.record.sessionNotHappenReason?.reason?.name,
      recordSessionNotHappenReasonDetails = this.record.sessionNotHappenReason?.details,
      recordSessionNoAttendanceInformation = this.record.noAttendanceInformation,
      sessionDetailsWasPersonLate = this.sessionDetails?.wasPersonLate,
      sessionDetailsLateReason = this.sessionDetails?.lateReason,
      sessionDetailsDuration = this.sessionDetails?.duration?.toDisplayString(),
      sessionFeedbackWhatHappened = this.sessionFeedback?.whatHappened,
      sessionFeedbackBehaviour = this.sessionFeedback?.behaviour,
      sessionFeedbackStrengthsIdentified = this.sessionFeedback?.strengthsIdentified,
      issuesConcernsIdentified = this.issuesAndConcerns?.identified,
      issuesConcernsNotifyProbationPractitioner = this.issuesAndConcerns?.notifyProbationPractitioner,
      additionalSupportNeeds = this.newlyIdentifiedAdditionalSupportNeeds,
      nextStepsPlannedForNextSession = this.nextSteps?.plannedForNextSession,
      nextStepsActionsBeforeNextSession = this.nextSteps?.actionsBeforeNextSession,
      createdBy = submittedBy,
    )
  }
}
