package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.util.UUID

data class ActionPlanActionRequest(
  @field:NotNull(message = "Need ID must not be null")
  val needId: UUID,

  @field:NotNull(message = "Outcome ID must not be null")
  val outcomeId: UUID,

  @field:NotEmpty(message = "Activities list must not be empty")
  @field:Valid
  val activities: List<ActionPlanActivityRequest>,
)

data class ActionPlanActivityRequest(
  @field:NotBlank(message = "Activity 'who' field must not be blank")
  val who: String,

  @field:NotBlank(message = "Activity details must not be blank")
  val activityDetails: String,

  @field:NotBlank(message = "Activity status must not be blank")
  val status: String,
)
