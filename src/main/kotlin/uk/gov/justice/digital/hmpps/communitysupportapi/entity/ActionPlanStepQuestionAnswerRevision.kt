package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "action_plan_step_question_answer_revision")
data class ActionPlanStepQuestionAnswerRevision(
  @Id
  @Column(name = "id")
  val id: UUID,

  @Column(name = "action_plan_step_question_answer_id", nullable = false)
  val actionPlanStepQuestionAnswerId: UUID,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "action_plan_step_question_answer_id", insertable = false, updatable = false)
  val actionPlanStepQuestionAnswer: ActionPlanStepQuestionAnswer? = null,

  @Column(name = "revision_number", nullable = false)
  val revisionNumber: Int,

  @Column(name = "content")
  val content: String? = null,

  @Column(name = "created_at", nullable = false)
  val createdAt: OffsetDateTime? = null,

  @Column(name = "created_by", nullable = false)
  val createdBy: String = "SYSTEM",
)
