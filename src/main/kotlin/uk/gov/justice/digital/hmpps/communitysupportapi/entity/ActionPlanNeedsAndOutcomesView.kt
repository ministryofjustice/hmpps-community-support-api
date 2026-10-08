package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "action_plan_needs_and_outcomes_view")
class ActionPlanNeedsAndOutcomesView(
  @Id
  @Column(name = "referral_id")
  val referralId: UUID,

  @Column(name = "action_plan_id")
  val actionPlanId: UUID,

  @Column(name = "action_plan_step_question_answer_header_id")
  val actionPlanStepQuestionAnswerHeaderId: UUID,

  @Column(name = "action_plan_step_question_answer_details_id")
  val actionPlanStepQuestionAnswerDetailsId: UUID,

  @Column(name = "need_id")
  val needId: UUID,

  @Column(name = "need_name")
  val needName: String,

  @Column(name = "outcome_answer_text")
  val outcomeAnswerText: String,

  @Column(name = "display_order")
  val displayOrder: Int,
)
