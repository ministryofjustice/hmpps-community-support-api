package uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanQuestionType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestion
import java.util.UUID

class ActionPlanStepQuestionFactory : TestEntityFactory<ActionPlanStepQuestion>() {

  private var id: UUID = UUID.randomUUID()
  private var actionPlanStepId: UUID = UUID.randomUUID()
  private var orderNumber: Int = 1
  private var title: String = "What is the outcome?"
  private var answerType: String = "textarea"
  private var questionType: ActionPlanQuestionType = ActionPlanQuestionType.OUTCOME
  private var maxNumberResponses: Int = 10
  private var needId: UUID? = null

  fun withId(id: UUID) = apply { this.id = id }
  fun withActionPlanStepId(actionPlanStepId: UUID) = apply { this.actionPlanStepId = actionPlanStepId }
  fun withOrderNumber(orderNumber: Int) = apply { this.orderNumber = orderNumber }
  fun withTitle(title: String) = apply { this.title = title }
  fun withAnswerType(answerType: String) = apply { this.answerType = answerType }
  fun withQuestionType(questionType: ActionPlanQuestionType) = apply { this.questionType = questionType }
  fun withMaxNumberResponses(maxNumberResponses: Int) = apply { this.maxNumberResponses = maxNumberResponses }
  fun withNeedId(needId: UUID?) = apply { this.needId = needId }

  override fun create(): ActionPlanStepQuestion = ActionPlanStepQuestion(
    id = id,
    actionPlanStepId = actionPlanStepId,
    orderNumber = orderNumber,
    title = title,
    answerType = answerType,
    questionType = questionType,
    maxNumberResponses = maxNumberResponses,
    needId = needId,
  )
}
