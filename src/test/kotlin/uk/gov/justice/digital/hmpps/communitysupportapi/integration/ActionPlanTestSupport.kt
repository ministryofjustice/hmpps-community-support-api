package uk.gov.justice.digital.hmpps.communitysupportapi.integration

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlan
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanEvent
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanEventType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanTemplate
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanEventRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanTemplateRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ActionPlanEventFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ActionPlanFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ActionPlanTemplateFactory
import java.time.OffsetDateTime
import java.util.UUID

@Component
class ActionPlanTestSupport(
  private val actionPlanTemplateRepository: ActionPlanTemplateRepository,
  private val actionPlanRepository: ActionPlanRepository,
  private val actionPlanEventRepository: ActionPlanEventRepository,
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
}
