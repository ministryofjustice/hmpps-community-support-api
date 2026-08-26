package uk.gov.justice.digital.hmpps.communitysupportapi.dto

data class ActionPlanSessionDeliveryDetailsRequest(
  val answers: List<SessionDeliveryDetailsQuestionAnswers>,
)
