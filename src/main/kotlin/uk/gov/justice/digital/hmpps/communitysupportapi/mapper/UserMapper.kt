package uk.gov.justice.digital.hmpps.communitysupportapi.mapper

import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CaseWorkerDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.UserDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.User
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.UserType

fun User.toDto(): UserDto = UserDto(
  id = id,
  hmppsAuthId = hmppsAuthId,
  hmppsAuthUsername = hmppsAuthUsername,
  firstName = firstName,
  lastName = lastName,
  emailAddress = emailAddress,
)

fun User.toCaseWorkerDto(): CaseWorkerDto = CaseWorkerDto(
  userType = userType,
  userId = id,
  fullName = fullName,
  emailAddress = emailAddress,
)

fun UserDto.toEntity(): User = User(
  id = id,
  hmppsAuthId = hmppsAuthId,
  hmppsAuthUsername = hmppsAuthUsername,
  firstName = firstName.trim(),
  lastName = lastName.trim(),
  emailAddress = emailAddress.trim().lowercase(),
  userType = if (hmppsAuthId.isNullOrBlank()) UserType.EXTERNAL else UserType.INTERNAL,
)
