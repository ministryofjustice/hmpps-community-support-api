package uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestion
import uk.gov.justice.digital.hmpps.communitysupportapi.model.ActionPlanQuestionAnswers
import uk.gov.justice.digital.hmpps.communitysupportapi.model.ActionPlanQuestionAnswers.CurrentAnswer
import java.util.UUID

class ActionPlanQuestionAnswersFactory {
  private val actionPlanId: UUID = UUID.randomUUID()
  private var currentAnswers: List<CurrentAnswer> = emptyList()

  fun withCurrentAnswers(currentAnswers: List<CurrentAnswer>): ActionPlanQuestionAnswersFactory = apply { this.currentAnswers = currentAnswers }

  fun create(question: ActionPlanStepQuestion): ActionPlanQuestionAnswers = ActionPlanQuestionAnswers(
    actionPlanId = actionPlanId,
    question = question,
    currentAnswers = currentAnswers,
  )
}
