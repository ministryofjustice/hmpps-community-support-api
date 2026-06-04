import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CaseWorkerSummaryDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentDeliveryMethod
import java.time.LocalDateTime

data class SessionFeedbackDetailsDto(
  val currentCaseworkers: List<CaseWorkerSummaryDto>,
  val feedbackSubmittedBy: String,
  val startDateTime: LocalDateTime,
  val sessionMethod: AppointmentDeliveryMethod?,
  val sessionCommunications: List<String>?,
  val personFirstName: String,
)
