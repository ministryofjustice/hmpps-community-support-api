package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentHistory
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser

class CreateIcsFeedbackRequestTest {

  @Test
  fun `maps feedback request fields to an appointment feedback entity`() {
    val appointmentIcs = mock<AppointmentHistory>()
    val submittedBy = mock<ReferralUser>()
    val request = CreateIcsFeedbackRequest(
      record = RecordSessionRequest(
        didSessionHappen = true,
        howSessionTookPlace = SessionMethodRequest(
          type = SessionMethodType.IN_PERSON_OTHER_LOCATION,
          additionalDetails = "Venue closed",
          pdu = "PDU South",
          addressLine1 = "1 High Street",
          addressLine2 = "Suite 2",
          townOrCity = "Leeds",
          county = "West Yorkshire",
          postcode = "LS1 1AA",
        ),
        didPersonAttend = true,
        noAttendanceInformation = "Attendance confirmed",
      ),
      sessionDetails = SessionDetailsRequest(
        wasPersonLate = true,
        lateReason = "Traffic",
        duration = SessionDurationRequest(hours = 1, minutes = 30),
      ),
      sessionFeedback = SessionFeedbackRequest(
        whatHappened = "Discussed progress",
        behaviour = "Engaged",
        strengthsIdentified = "Motivated",
      ),
      issuesAndConcerns = IssuesAndConcernsRequest(
        identified = "Housing",
        notifyProbationPractitioner = true,
      ),
      newlyIdentifiedAdditionalSupportNeeds = "Interpreter",
      nextSteps = NextStepsRequest(
        plannedForNextSession = "Review housing",
        actionsBeforeNextSession = "Contact landlord",
      ),
    )

    val feedback = request.toAppointmentIcsFeedback(appointmentIcs, submittedBy)

    assertThat(feedback.appointmentIcs).isSameAs(appointmentIcs)
    assertThat(feedback.recordSessionDidSessionHappen).isTrue()
    assertThat(feedback.recordSessionHowSessionTookPlace).isEqualTo("In person (other location)")
    assertThat(feedback.recordSessionNotInPersonReason).isEqualTo("Venue closed")
    assertThat(feedback.recordSessionPdu).isEqualTo("PDU South")
    assertThat(feedback.recordSessionAddressLine1).isEqualTo("1 High Street")
    assertThat(feedback.recordSessionAddressLine2).isEqualTo("Suite 2")
    assertThat(feedback.recordSessionTownOrCity).isEqualTo("Leeds")
    assertThat(feedback.recordSessionCounty).isEqualTo("West Yorkshire")
    assertThat(feedback.recordSessionPostcode).isEqualTo("LS1 1AA")
    assertThat(feedback.recordSessionDidPersonAttend).isTrue()
    assertThat(feedback.recordSessionNoAttendanceInformation).isEqualTo("Attendance confirmed")
    assertThat(feedback.sessionDetailsWasPersonLate).isTrue()
    assertThat(feedback.sessionDetailsLateReason).isEqualTo("Traffic")
    assertThat(feedback.sessionDetailsDuration).isEqualTo("1 hour and 30 minutes")
    assertThat(feedback.sessionFeedbackWhatHappened).isEqualTo("Discussed progress")
    assertThat(feedback.sessionFeedbackBehaviour).isEqualTo("Engaged")
    assertThat(feedback.sessionFeedbackStrengthsIdentified).isEqualTo("Motivated")
    assertThat(feedback.issuesConcernsIdentified).isEqualTo("Housing")
    assertThat(feedback.issuesConcernsNotifyProbationPractitioner).isTrue()
    assertThat(feedback.additionalSupportNeeds).isEqualTo("Interpreter")
    assertThat(feedback.nextStepsPlannedForNextSession).isEqualTo("Review housing")
    assertThat(feedback.nextStepsActionsBeforeNextSession).isEqualTo("Contact landlord")
    assertThat(feedback.createdBy).isSameAs(submittedBy)
  }
}
