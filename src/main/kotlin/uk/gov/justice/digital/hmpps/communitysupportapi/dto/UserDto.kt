package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.UserType
import java.util.UUID

data class UserDto(
  val id: UUID,
  val hmppsAuthId: String,
  val hmppsAuthUsername: String,
  val authSource: String,
  val fullName: String,
  val emailAddress: String,
) {
  val userType: UserType
    get() = if (authSource == "auth") UserType.INTERNAL else UserType.EXTERNAL
}
