package uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory

import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CreateIcsFeedbackRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.IssuesAndConcernsRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.NextStepsRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.RecordSessionRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionDetailsRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionDurationRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionFeedbackRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionMethodRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionMethodType
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionNotHappenReasonRequest
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Appointment
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentDelivery
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentDeliveryMethod
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentIcs
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentStatusHistory
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentStatusHistoryType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import java.time.LocalDateTime
import java.util.UUID

class AppointmentFactory : TestEntityFactory<Appointment>() {

  private var id: UUID = UUID.randomUUID()
  private var referral: Referral? = null
  private var type: AppointmentType = AppointmentType.ICS

  fun withId(id: UUID) = apply { this.id = id }
  fun withReferral(referral: Referral) = apply { this.referral = referral }
  fun withType(type: AppointmentType) = apply { this.type = type }

  override fun create(): Appointment = Appointment(
    id = id,
    referral = referral ?: error("Referral must be provided"),
    type = type,
  )
}

class AppointmentDeliveryFactory : TestEntityFactory<AppointmentDelivery>() {

  private var id: UUID = UUID.randomUUID()
  private var method: AppointmentDeliveryMethod = AppointmentDeliveryMethod.PHONE_CALL
  private var methodDetails: String? = null
  private var addressLine1: String? = null
  private var addressLine2: String? = null
  private var townOrCity: String? = null
  private var county: String? = null
  private var postcode: String? = null

  fun withId(id: UUID) = apply { this.id = id }
  fun withMethod(method: AppointmentDeliveryMethod) = apply { this.method = method }
  fun withMethodDetails(methodDetails: String?) = apply { this.methodDetails = methodDetails }
  fun withAddressLine1(addressLine1: String?) = apply { this.addressLine1 = addressLine1 }
  fun withAddressLine2(addressLine2: String?) = apply { this.addressLine2 = addressLine2 }
  fun withTownOrCity(townOrCity: String?) = apply { this.townOrCity = townOrCity }
  fun withCounty(county: String?) = apply { this.county = county }
  fun withPostcode(postcode: String?) = apply { this.postcode = postcode }

  override fun create(): AppointmentDelivery = AppointmentDelivery(
    id = id,
    method = method,
    methodDetails = methodDetails,
    addressLine1 = addressLine1,
    addressLine2 = addressLine2,
    townOrCity = townOrCity,
    county = county,
    postcode = postcode,
  )
}

class AppointmentIcsFactory : TestEntityFactory<AppointmentIcs>() {

  private var id: UUID = UUID.randomUUID()
  private var appointment: Appointment? = null
  private var appointmentDelivery: AppointmentDelivery? = null
  private var createdAt: LocalDateTime = LocalDateTime.now()
  private var appointmentDateTime: LocalDateTime = LocalDateTime.now().plusDays(7)
  private var createdBy: ReferralUser = ReferralUserFactory().create()
  private var sessionCommunication: List<String> = emptyList()

  fun withId(id: UUID) = apply { this.id = id }
  fun withAppointment(appointment: Appointment) = apply { this.appointment = appointment }
  fun withAppointmentDelivery(appointmentDelivery: AppointmentDelivery?) = apply { this.appointmentDelivery = appointmentDelivery }
  fun withCreatedAt(createdAt: LocalDateTime) = apply { this.createdAt = createdAt }
  fun withAppointmentDateTime(appointmentDateTime: LocalDateTime) = apply { this.appointmentDateTime = appointmentDateTime }
  fun withCreatedBy(createdBy: ReferralUser) = apply { this.createdBy = createdBy }
  fun withSessionCommunication(sessionCommunication: List<String>) = apply { this.sessionCommunication = sessionCommunication }

  override fun create(): AppointmentIcs = AppointmentIcs(
    id = id,
    appointment = appointment ?: error("Appointment must be provided"),
    appointmentDelivery = appointmentDelivery,
    appointmentDateTime = appointmentDateTime,
    createdAt = createdAt,
    createdBy = createdBy,
    sessionCommunication = sessionCommunication,
  )
}

class AppointmentStatusHistoryFactory : TestEntityFactory<AppointmentStatusHistory>() {
  private var id: UUID = UUID.randomUUID()
  private var appointment: Appointment? = null
  private var createdAt: LocalDateTime = LocalDateTime.now()
  private var status: AppointmentStatusHistoryType? = null

  fun withId(id: UUID) = apply { this.id = id }
  fun withAppointment(appointment: Appointment) = apply { this.appointment = appointment }
  fun withCreatedAt(createdAt: LocalDateTime) = apply { this.createdAt = createdAt }
  fun withStatus(status: AppointmentStatusHistoryType) = apply { this.status = status }

  override fun create(): AppointmentStatusHistory = AppointmentStatusHistory(
    appointment = appointment ?: error("Appointment must be provided"),
    status = status ?: error("Appointment Status must be provided"),
    createdAt = createdAt,
  )
}

class CreateIcsFeedbackRequestFactory : TestEntityFactory<CreateIcsFeedbackRequest>() {

  // Section 1 – Record session attendance
  private var didSessionHappen: Boolean = true
  private var howSessionTookPlace: SessionMethodRequest? = SessionMethodRequest(type = SessionMethodType.PHONE)
  private var didPersonAttend: Boolean? = null
  private var sessionNotHappenReason: SessionNotHappenReasonRequest? = null
  private var noAttendanceInformation: String? = null

  // Section 2 – Session details
  private var wasPersonLate: Boolean? = false
  private var lateReason: String? = null
  private var duration: SessionDurationRequest? = SessionDurationRequest(hours = 1, minutes = 0)

  // Section 3 – Session feedback
  private var whatHappened: String? = "Discussed reintegration goals"
  private var behaviour: String? = "Engaged and positive"
  private var strengthsIdentified: String? = "Strong family support"

  // Section 4 – Issues or concerns
  private var issuesConcernsIdentified: String? = null
  private var notifyProbationPractitioner: Boolean? = false

  // Section 5 – Next steps
  private var plannedForNextSession: String? = "Continue with action plan"
  private var actionsBeforeNextSession: String? = "Complete CV template"

  fun withDidSessionHappen(value: Boolean) = apply { didSessionHappen = value }
  fun withHowSessionTookPlace(value: SessionMethodRequest?) = apply { howSessionTookPlace = value }
  fun withDidPersonAttend(value: Boolean?) = apply { didPersonAttend = value }
  fun withSessionNotHappenReason(value: SessionNotHappenReasonRequest?) = apply { sessionNotHappenReason = value }
  fun withNoAttendanceInformation(value: String?) = apply { noAttendanceInformation = value }
  fun withWasPersonLate(value: Boolean?) = apply { wasPersonLate = value }
  fun withLateReason(value: String?) = apply { lateReason = value }
  fun withDuration(value: SessionDurationRequest?) = apply { duration = value }
  fun withWhatHappened(value: String?) = apply { whatHappened = value }
  fun withBehaviour(value: String?) = apply { behaviour = value }
  fun withStrengthsIdentified(value: String?) = apply { strengthsIdentified = value }
  fun withIssuesConcernsIdentified(value: String?) = apply { issuesConcernsIdentified = value }
  fun withNotifyProbationPractitioner(value: Boolean?) = apply { notifyProbationPractitioner = value }
  fun withPlannedForNextSession(value: String?) = apply { plannedForNextSession = value }
  fun withActionsBeforeNextSession(value: String?) = apply { actionsBeforeNextSession = value }

  override fun create(): CreateIcsFeedbackRequest = CreateIcsFeedbackRequest(
    record = RecordSessionRequest(
      didSessionHappen = didSessionHappen,
      howSessionTookPlace = howSessionTookPlace,
      didPersonAttend = didPersonAttend,
      sessionNotHappenReason = sessionNotHappenReason,
      noAttendanceInformation = noAttendanceInformation,
    ),
    sessionDetails = SessionDetailsRequest(
      wasPersonLate = wasPersonLate,
      lateReason = lateReason,
      duration = duration,
    ),
    sessionFeedback = SessionFeedbackRequest(
      whatHappened = whatHappened,
      behaviour = behaviour,
      strengthsIdentified = strengthsIdentified,
    ),
    issuesAndConcerns = IssuesAndConcernsRequest(
      identified = issuesConcernsIdentified,
      notifyProbationPractitioner = notifyProbationPractitioner,
    ),
    nextSteps = NextStepsRequest(
      plannedForNextSession = plannedForNextSession,
      actionsBeforeNextSession = actionsBeforeNextSession,
    ),
  )
}
