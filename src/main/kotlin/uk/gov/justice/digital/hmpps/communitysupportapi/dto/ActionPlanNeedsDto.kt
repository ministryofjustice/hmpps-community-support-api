package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import java.util.UUID

data class ActionPlanNeedsResponse(
  val needs: List<NeedDto>,
)

data class NeedDto(
  val id: UUID,
  val label: String,
  val questions: List<QuestionDto>,
)

data class QuestionDto(
  val id: UUID,
  val label: String,
  val answerType: String,
)
