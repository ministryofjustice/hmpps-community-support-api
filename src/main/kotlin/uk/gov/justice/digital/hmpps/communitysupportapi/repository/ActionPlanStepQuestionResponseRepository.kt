package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepQuestionResponse
import java.util.UUID

interface ActionPlanStepQuestionResponseRepository : JpaRepository<ActionPlanStepQuestionResponse, UUID>
