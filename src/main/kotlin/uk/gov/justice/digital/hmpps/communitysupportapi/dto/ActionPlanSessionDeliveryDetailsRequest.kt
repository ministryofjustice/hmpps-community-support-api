package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import jakarta.validation.Valid
import uk.gov.justice.digital.hmpps.communitysupportapi.validation.actionplan.NoDuplicateQuestionIds

data class ActionPlanSessionDeliveryDetailsRequest(
  @field:Valid
  @field:NoDuplicateQuestionIds
  val answers: List<SessionDeliveryDetailsQuestionAnswers>,
)
