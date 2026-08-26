package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.util.UUID

enum class ActionPlanQuestionType {
  OUTCOME,
  GENERAL,
}

enum class ActionPlanQuestionAnswerType {
  TEXTAREA,
  RADIO,
  CHECKBOX,
}

/**
 * An ActionPlanStepQuestion represents a specific question posed to a user
 * when creating an ActionPlan.  We use the `question_type` field to note
 * if this question is about a specific kind of knowledge, e.g. an "outcome"
 * (against a Need) is a specific idea.
 *
 * This entity allows us to model the questions that will be presented to a user
 * when completing an ActionPlan (their wording, the `input` type presenting, the
 * ordering) at the API level, rather than more ad-hoc at the UI level.  This
 * allows us to create a less opinionated UI, pushing decisions into the API
 * layer.
 *
 * @see ActionPlan
 * @see ActionPlanQuestionType
 * @see ActionPlanStepQuestionAnswerHeader
 */
@Entity
@Table(name = "action_plan_step_question")
data class ActionPlanStepQuestion(
  @Id
  @Column(name = "id")
  val id: UUID,

  @Column(name = "action_plan_step_id", nullable = false)
  val actionPlanStepId: UUID,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "action_plan_step_id", insertable = false, updatable = false)
  val actionPlanStep: ActionPlanStep? = null,

  @Column(name = "order_number", nullable = false)
  val orderNumber: Int,

  @Column(name = "title", nullable = false)
  val title: String,

  @Column(name = "answer_type", nullable = false)
  @Enumerated(EnumType.STRING)
  val answerType: ActionPlanQuestionAnswerType,

  @Column(name = "question_type", nullable = false)
  @Enumerated(EnumType.STRING)
  val questionType: ActionPlanQuestionType,

  @Column(name = "max_number_responses", nullable = false)
  val maxNumberResponses: Int,

  @Column(name = "need_id")
  val needId: UUID? = null,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "need_id", insertable = false, updatable = false)
  val need: Need? = null,

  @OneToMany(fetch = FetchType.LAZY)
  @JoinColumn(name = "action_plan_step_question_id", insertable = false, updatable = false)
  val choices: MutableList<ActionPlanStepQuestionChoice> = mutableListOf(),
)
