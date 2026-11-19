package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.AssertTrue
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
  @get:JsonIgnore
  @get:Schema(hidden = true)
  @get:AssertTrue(message = "howTheyTriedToContactThePerson is required when the person did not attend")
  val isContactMethodValid: Boolean
    get() = record.didPersonAttend != false || !record.howTheyTriedToContactThePerson.isNullOrBlank()

  @get:JsonIgnore
  @get:Schema(hidden = true)
  @get:AssertTrue(message = "reasonsTheyDidNotAttend is required when the person did not attend")
  val isReasonsValid: Boolean
    get() = record.didPersonAttend != false || !record.reasonsTheyDidNotAttend.isNullOrBlank()

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
      recordSessionHowTheyTriedToContactThePerson = this.record.howTheyTriedToContactThePerson,
      recordSessionReasonsTheyDidNotAttend = this.record.reasonsTheyDidNotAttend,
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
