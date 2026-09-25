package uk.gov.justice.digital.hmpps.communitysupportapi.integration

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlan
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanActivity
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanEvent
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanEventType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanQuestionAnswerType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanQuestionType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStep
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestion
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestionAnswerDetails
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestionAnswerHeader
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanTemplate
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanActivityRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanEventRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanStepQuestionAnswerDetailsRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanStepQuestionAnswerHeaderRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanStepQuestionRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanStepRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanTemplateRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ActionPlanActivityFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ActionPlanEventFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ActionPlanFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ActionPlanStepFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ActionPlanStepQuestionAnswerDetailsFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ActionPlanStepQuestionAnswerHeaderFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ActionPlanStepQuestionFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ActionPlanTemplateFactory
import java.time.OffsetDateTime
import java.util.UUID

@Component
class ActionPlanTestSupport(
  private val actionPlanTemplateRepository: ActionPlanTemplateRepository,
  private val actionPlanRepository: ActionPlanRepository,
  private val actionPlanEventRepository: ActionPlanEventRepository,
  private val actionPlanStepQuestionRepository: ActionPlanStepQuestionRepository,
  private val actionPlanStepRepository: ActionPlanStepRepository,
  private val actionPlanStepQuestionAnswerHeaderRepository: ActionPlanStepQuestionAnswerHeaderRepository,
  private val actionPlanStepQuestionAnswerDetailsRepository: ActionPlanStepQuestionAnswerDetailsRepository,
  private val actionPlanActivityRepository: ActionPlanActivityRepository,
) {
  fun createActionPlanTemplate(
    id: UUID = UUID.randomUUID(),
    activeGlobal: Boolean = false,
  ): ActionPlanTemplate = actionPlanTemplateRepository.save(
    ActionPlanTemplateFactory()
      .withId(id)
      .withActiveGlobal(activeGlobal)
      .create(),
  )

  fun createActionPlan(
    referralId: UUID,
    templateId: UUID,
    createdAt: OffsetDateTime = OffsetDateTime.now(),
    updatedAt: OffsetDateTime = OffsetDateTime.now(),
  ): ActionPlan = actionPlanRepository.save(
    ActionPlanFactory()
      .withReferralId(referralId)
      .withActionPlanTemplateId(templateId)
      .withCreatedAt(createdAt)
      .withUpdatedAt(updatedAt)
      .withCreatedEvent(createdAt = createdAt)
      .create(),
  )

  fun createSubmittedActionPlan(
    referralId: UUID,
    templateId: UUID,
    createdAt: OffsetDateTime = OffsetDateTime.now(),
    createdBy: String = "SYSTEM",
  ): ActionPlan = actionPlanRepository.save(
    ActionPlanFactory()
      .withReferralId(referralId)
      .withActionPlanTemplateId(templateId)
      .withCreatedAt(createdAt)
      .withUpdatedAt(createdAt)
      .withCreatedEvent(createdBy = createdBy, createdAt = createdAt)
      .withSubmittedEvent(createdBy = createdBy, createdAt = createdAt)
      .create(),
  )

  fun createActionPlanEvent(
    actionPlanId: UUID,
    eventType: ActionPlanEventType = ActionPlanEventType.CREATED,
    createdBy: String = "SYSTEM",
    createdAt: OffsetDateTime = OffsetDateTime.now(),
  ): ActionPlanEvent = actionPlanEventRepository.save(
    ActionPlanEventFactory()
      .withActionPlanId(actionPlanId)
      .withEventType(eventType)
      .withCreatedBy(createdBy)
      .withCreatedAt(createdAt)
      .create(),
  )

  fun createActionPlanStep(
    actionPlanTemplateId: UUID,
    orderNumber: Int = 1,
    name: String = "Step 1",
    stepType: ActionPlanStepType = ActionPlanStepType.NEED,
  ): ActionPlanStep = actionPlanStepRepository.save(
    ActionPlanStepFactory()
      .withActionPlanTemplateId(actionPlanTemplateId)
      .withOrderNumber(orderNumber)
      .withName(name)
      .withStepType(stepType)
      .create(),
  )

  fun createActionPlanStepQuestion(
    actionPlanStepId: UUID,
    orderNumber: Int = 1,
    label: String = "Step 1",
    answerType: ActionPlanQuestionAnswerType = ActionPlanQuestionAnswerType.RADIO,
    questionType: ActionPlanQuestionType = ActionPlanQuestionType.OUTCOME,
    maxNumberResponses: Int = 1,
    needId: UUID? = null,
  ): ActionPlanStepQuestion = actionPlanStepQuestionRepository.save(
    ActionPlanStepQuestionFactory()
      .withActionPlanStepId(actionPlanStepId)
      .withOrderNumber(orderNumber)
      .withTitle("Select an outcome for $label")
      .withAnswerType(answerType)
      .withQuestionType(questionType)
      .withMaxNumberResponses(maxNumberResponses)
      .withNeedId(needId)
      .create(),
  )

  fun createActionPlanStepQuestionAnswerHeader(
    actionPlanId: UUID,
    actionPlanStepQuestionId: UUID,
    orderNumber: Int = 1,
    createdAt: OffsetDateTime = OffsetDateTime.now(),
    createdBy: String = "System provider",
    deletedAt: OffsetDateTime? = null,
    deletedBy: String? = null,
  ): ActionPlanStepQuestionAnswerHeader = actionPlanStepQuestionAnswerHeaderRepository.save(
    ActionPlanStepQuestionAnswerHeaderFactory()
      .withActionPlanId(actionPlanId)
      .withActionPlanStepQuestionId(actionPlanStepQuestionId)
      .withOrderNumber(orderNumber)
      .withCreatedAt(createdAt)
      .withCreatedBy(createdBy)
      .withDeletedAt(deletedAt)
      .withDeletedBy(deletedBy)
      .create(),
  )

  fun createActionPlanStepQuestionAnswerDetails(
    actionPlanStepQuestionAnswerHeaderId: UUID,
    revisionNumber: Int = 1,
    content: String = "Answer to the question",
    freeTextValue: String? = null,
    createdAt: OffsetDateTime = OffsetDateTime.now(),
    createdBy: String = "SYSTEM",
  ): ActionPlanStepQuestionAnswerDetails = actionPlanStepQuestionAnswerDetailsRepository.save(
    ActionPlanStepQuestionAnswerDetailsFactory()
      .withActionPlanStepQuestionAnswerHeaderId(actionPlanStepQuestionAnswerHeaderId)
      .withRevisionNumber(revisionNumber)
      .withContent(content)
      .withFreeTextValue(freeTextValue)
      .withCreatedAt(createdAt)
      .withCreatedBy(createdBy)
      .create(),
  )

  fun createActionPlanActivity(
    actionPlanStepQuestionAnswerHeaderId: UUID,
    who: String = "Service provider",
    activityDetails: String = "Details about the activity",
    status: String = "ACTIVE",
  ): ActionPlanActivity = actionPlanActivityRepository.save(
    ActionPlanActivityFactory()
      .withActionPlanStepQuestionAnswerHeaderId(actionPlanStepQuestionAnswerHeaderId)
      .withWho(who)
      .withActivityDetails(activityDetails)
      .withStatus(status)
      .create(),
  )
}
