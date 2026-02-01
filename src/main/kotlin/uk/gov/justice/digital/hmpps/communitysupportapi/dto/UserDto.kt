package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.UserType
import java.util.UUID

data class UserDto(
  val id: UUID,
  val hmppsAuthId: String? = null,
  val hmppsAuthUsername: String? = null,
  val firstName: String,
  val lastName: String,
  val emailAddress: String,
) {
  val userType: UserType
    get() = if (hmppsAuthId.isNullOrBlank()) UserType.EXTERNAL else UserType.INTERNAL
}
