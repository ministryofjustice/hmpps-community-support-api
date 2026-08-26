package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestionAnswerDetails
import java.util.UUID

interface ActionPlanStepQuestionAnswerDetailsRepository : JpaRepository<ActionPlanStepQuestionAnswerDetails, UUID> {
  fun findAllByActionPlanStepQuestionAnswerHeaderIdIn(actionPlanStepQuestionAnswerHeaderIds: Collection<UUID>): List<ActionPlanStepQuestionAnswerDetails>

  @Query(
    value = """
      SELECT details.*
      FROM action_plan_step_question_answer_details details
      JOIN action_plan_step_question_answer_header header
        ON header.id = details.action_plan_step_question_answer_header_id
      WHERE header.action_plan_step_question_id = :questionId
        AND header.action_plan_id = :actionPlanId
        AND header.deleted_at IS NULL
      ORDER BY details.created_at DESC, details.id DESC
      """,
    nativeQuery = true,
  )
  fun getMostRecentAnswersForActionPlanQuestion(
    @Param("questionId") questionId: UUID,
    @Param("actionPlanId") actionPlanId: UUID,
  ): List<ActionPlanStepQuestionAnswerDetails>

  @Query(
    value = """
      SELECT DISTINCT ON (details.action_plan_step_question_answer_header_id) details.*
      FROM action_plan_step_question_answer_details details
      JOIN action_plan_step_question_answer_header header
          ON header.id = details.action_plan_step_question_answer_header_id
      WHERE header.action_plan_step_question_id IN (:questionIds)
        AND header.action_plan_id = :actionPlanId
        AND header.deleted_at IS NULL
      ORDER BY details.action_plan_step_question_answer_header_id, details.created_at DESC, details.id DESC
    """,
    nativeQuery = true,
  )
  fun getMostRecentAnswersForActionPlanQuestions(
    @Param("questionIds") questionIds: List<UUID>,
    @Param("actionPlanId") actionPlanId: UUID,
  ): List<ActionPlanStepQuestionAnswerDetails>
}
