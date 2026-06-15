package uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentIcs
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentIcsFeedback
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import java.util.UUID

class AppointmentIcsFeedbackFactory : TestEntityFactory<AppointmentIcsFeedback>() {

  private var id: UUID = UUID.randomUUID()
  private var appointmentIcs: AppointmentIcs? = null
  private var createdBy: ReferralUser = ReferralUserFactory().create()

  private var recordSessionDidSessionHappen: Boolean = true
  private var recordSessionHowSessionTookPlace: String? = "Phone"
  private var recordSessionDidPersonAttend: Boolean? = true
  private var recordSessionNotHappenReason: String? = null
  private var recordSessionNotHappenReasonDetails: String? = null
  private var recordSessionNoAttendanceInformation: String? = null

  private var sessionDetailsWasPersonLate: Boolean? = false
  private var sessionDetailsLateReason: String? = null
  private var sessionDetailsDuration: String? = "1 hour"

  private var sessionFeedbackWhatHappened: String? = "Discussed reintegration goals"
  private var sessionFeedbackBehaviour: String? = "Engaged and positive"
  private var sessionFeedbackStrengthsIdentified: String? = "Strong family support"

  private var issuesConcernsIdentified: String? = null
  private var issuesConcernsNotifyProbationPractitioner: Boolean? = false

  private var nextStepsPlannedForNextSession: String? = "Continue with action plan"
  private var nextStepsActionsBeforeNextSession: String? = "Complete CV template"

  fun withId(id: UUID) = apply { this.id = id }
  fun withAppointmentIcs(appointmentIcs: AppointmentIcs) = apply { this.appointmentIcs = appointmentIcs }
  fun withCreatedBy(createdBy: ReferralUser) = apply { this.createdBy = createdBy }

  fun withDidSessionHappen(didSessionHappen: Boolean) = apply {
    this.recordSessionDidSessionHappen = didSessionHappen
  }

  fun withSessionMethod(sessionMethod: String?) = apply {
    this.recordSessionHowSessionTookPlace = sessionMethod
  }

  fun withDidPersonAttend(didPersonAttend: Boolean?) = apply {
    this.recordSessionDidPersonAttend = didPersonAttend
  }

  fun withNotHappenReason(reason: String?) = apply {
    this.recordSessionNotHappenReason = reason
  }

  fun withNoAttendanceInformation(info: String?) = apply {
    this.recordSessionNoAttendanceInformation = info
  }

  fun withWasPersonLate(wasPersonLate: Boolean?) = apply {
    this.sessionDetailsWasPersonLate = wasPersonLate
  }

  fun withDuration(duration: String?) = apply {
    this.sessionDetailsDuration = duration
  }

  fun withWhatHappened(whatHappened: String?) = apply {
    this.sessionFeedbackWhatHappened = whatHappened
  }

  fun withBehaviour(behaviour: String?) = apply {
    this.sessionFeedbackBehaviour = behaviour
  }

  fun withStrengthsIdentified(strengths: String?) = apply {
    this.sessionFeedbackStrengthsIdentified = strengths
  }

  override fun create(): AppointmentIcsFeedback = AppointmentIcsFeedback(
    id = id,
    appointmentIcs = appointmentIcs ?: error("AppointmentIcs must be provided"),
    recordSessionDidSessionHappen = recordSessionDidSessionHappen,
    recordSessionHowSessionTookPlace = recordSessionHowSessionTookPlace,
    recordSessionDidPersonAttend = recordSessionDidPersonAttend,
    recordSessionNotHappenReason = recordSessionNotHappenReason,
    recordSessionNotHappenReasonDetails = recordSessionNotHappenReasonDetails,
    recordSessionNoAttendanceInformation = recordSessionNoAttendanceInformation,
    sessionDetailsWasPersonLate = sessionDetailsWasPersonLate,
    sessionDetailsLateReason = sessionDetailsLateReason,
    sessionDetailsDuration = sessionDetailsDuration,
    sessionFeedbackWhatHappened = sessionFeedbackWhatHappened,
    sessionFeedbackBehaviour = sessionFeedbackBehaviour,
    sessionFeedbackStrengthsIdentified = sessionFeedbackStrengthsIdentified,
    issuesConcernsIdentified = issuesConcernsIdentified,
    issuesConcernsNotifyProbationPractitioner = issuesConcernsNotifyProbationPractitioner,
    nextStepsPlannedForNextSession = nextStepsPlannedForNextSession,
    nextStepsActionsBeforeNextSession = nextStepsActionsBeforeNextSession,
    createdBy = createdBy,
  )
}
