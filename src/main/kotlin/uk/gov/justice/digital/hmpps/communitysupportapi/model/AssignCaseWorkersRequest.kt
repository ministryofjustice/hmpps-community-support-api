package uk.gov.justice.digital.hmpps.communitysupportapi.model

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size

data class AssignCaseWorkersRequest(
  @field:Size(max = 5, message = "Maximum 5 case workers allowed")
  val emails: List<
    @Email(
      message = "Invalid email format",
    )
    String,
    >,
)
