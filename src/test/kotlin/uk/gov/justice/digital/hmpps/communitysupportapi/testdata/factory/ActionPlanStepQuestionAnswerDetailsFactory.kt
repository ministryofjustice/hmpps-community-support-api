package uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestionAnswerDetails
import java.time.OffsetDateTime
import java.util.UUID

class ActionPlanStepQuestionAnswerDetailsFactory : TestEntityFactory<ActionPlanStepQuestionAnswerDetails>() {

  private var id: UUID = UUID.randomUUID()
  private var actionPlanStepQuestionAnswerHeaderId: UUID = UUID.randomUUID()
  private var revisionNumber: Int = 1
  private var content: String = "Service provider"
  private var freeTextValue: String? = null
  private var createdAt: OffsetDateTime? = null
  private var createdBy: String = "SYSTEM"

  fun withId(id: UUID) = apply { this.id = id }
  fun withActionPlanStepQuestionAnswerHeaderId(actionPlanStepQuestionAnswerHeaderId: UUID) = apply { this.actionPlanStepQuestionAnswerHeaderId = actionPlanStepQuestionAnswerHeaderId }
  fun withRevisionNumber(revisionNumber: Int) = apply { this.revisionNumber = revisionNumber }
  fun withContent(value: String) = apply { this.content = value }
  fun withFreeTextValue(value: String?) = apply { this.freeTextValue = value }
  fun withCreatedAt(createdAt: OffsetDateTime?) = apply { this.createdAt = createdAt }
  fun withCreatedBy(createdBy: String) = apply { this.createdBy = createdBy }

  override fun create(): ActionPlanStepQuestionAnswerDetails = ActionPlanStepQuestionAnswerDetails(
    id = id,
    actionPlanStepQuestionAnswerHeaderId = actionPlanStepQuestionAnswerHeaderId,
    revisionNumber = revisionNumber,
    content = content,
    freeTextValue = freeTextValue,
    createdAt = createdAt,
    createdBy = createdBy,
  )
}
