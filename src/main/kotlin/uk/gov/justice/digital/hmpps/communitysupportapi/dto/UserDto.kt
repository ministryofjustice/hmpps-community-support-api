package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.UserType
import uk.gov.justice.hmpps.kotlin.auth.AuthSource
import java.util.UUID

data class UserDto(
  val id: UUID,
  val hmppsAuthId: String,
  val hmppsAuthUsername: String,
  val authSource: String,
  val fullName: String,
) {
  val userType: UserType
    get() = if (authSource == AuthSource.AUTH.source) UserType.INTERNAL else UserType.EXTERNAL
}
