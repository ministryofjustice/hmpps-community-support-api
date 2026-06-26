import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CaseWorkerSummaryDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentDelivery
import java.time.LocalDateTime

data class SessionFeedbackAppointmentDetailsDto(
  val currentCaseworkers: List<CaseWorkerSummaryDto>,
  val feedbackSubmittedBy: CaseWorkerSummaryDto,
  val startDateTime: LocalDateTime,
  val appointmentDeliveryDetails: AppointmentDelivery?,
  val sessionCommunications: List<String>?,
  val personFirstName: String,
)
