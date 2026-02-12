package uk.gov.justice.digital.hmpps.communitysupportapi.model

import uk.gov.justice.digital.hmpps.communitysupportapi.dto.AssignmentFailureDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CaseWorkerDto

data class AssignCaseWorkersResult(
  val success: Boolean,
  val message: String,
  val succeededList: List<CaseWorkerDto>? = emptyList(),
  val failureList: List<AssignmentFailureDto>? = emptyList(),
)
