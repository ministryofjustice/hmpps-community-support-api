package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.io.Serializable
import java.util.UUID

@Embeddable
class ActionPlanNeedsAndOutcomesViewId(
  val referralId: UUID,
  val actionPlanId: UUID,
  val actionPlanStepQuestionAnswerHeaderId: UUID,
  val actionPlanStepQuestionAnswerDetailsId: UUID,
) : Serializable

@Entity
@Table(name = "action_plan_needs_and_outcomes_view")
class ActionPlanNeedsAndOutcomesView(
  @EmbeddedId
  val id: ActionPlanNeedsAndOutcomesViewId,

  @Column(name = "need_id")
  val needId: UUID,

  @Column(name = "need_name")
  val needName: String,

  @Column(name = "outcome_answer_text")
  val outcomeAnswerText: String,

  @Column(name = "display_order")
  val displayOrder: Int,
)
