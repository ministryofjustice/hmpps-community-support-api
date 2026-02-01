package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.UserType
import java.util.UUID

data class CaseWorkerDto(
  val userType: UserType,
  var userId: UUID? = null,
  val fullName: String? = null,
  val emailAddress: String,
)
