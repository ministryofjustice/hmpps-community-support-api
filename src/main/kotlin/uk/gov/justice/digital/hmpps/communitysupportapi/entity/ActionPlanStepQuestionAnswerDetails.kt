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

/**
 * One recorded version of the content for an answer header.
 *
 * A new details record is added whenever an answer changes, preserving the
 * earlier responses while the header continues to identify the answer itself.
 */
@Entity
@Table(name = "action_plan_step_question_answer_details")
data class ActionPlanStepQuestionAnswerDetails(
  @Id
  @Column(name = "id")
  val id: UUID,

  @Column(name = "action_plan_step_question_answer_header_id", nullable = false)
  val actionPlanStepQuestionAnswerHeaderId: UUID,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "action_plan_step_question_answer_header_id", insertable = false, updatable = false)
  val actionPlanStepQuestionAnswerHeader: ActionPlanStepQuestionAnswerHeader? = null,

  @Column(name = "revision_number", nullable = false)
  val revisionNumber: Int,

  @Column(name = "content")
  val content: String? = null,

  @Column(name = "free_text_value")
  val freeTextValue: String? = null,

  @Column(name = "created_at", nullable = false)
  val createdAt: OffsetDateTime? = null,

  @Column(name = "created_by", nullable = false)
  val createdBy: String = "SYSTEM",
)
