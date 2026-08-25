package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "action_plan_activity")
data class ActionPlanActivity(
  @Id
  @Column(name = "id")
  val id: UUID,

  @Column(name = "action_plan_step_question_id", nullable = false)
  val actionPlanStepQuestionId: UUID,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "action_plan_step_question_id", insertable = false, updatable = false)
  val actionPlanStepQuestion: ActionPlanStepQuestion? = null,

  @Column(name = "who", nullable = false)
  val who: String,

  @Column(name = "activity_details", nullable = false)
  val activityDetails: String,

  @Column(name = "status", nullable = false)
  val status: String,
)
