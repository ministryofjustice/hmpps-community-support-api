package uk.gov.justice.digital.hmpps.communitysupportapi.mapper

import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CaseWorkerDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.UserDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.UserType
import uk.gov.justice.hmpps.kotlin.auth.AuthSource

fun ReferralUser.toDto(): UserDto = UserDto(
  id = id,
  hmppsAuthId = hmppsAuthId,
  hmppsAuthUsername = hmppsAuthUsername,
  authSource = authSource,
  fullName = fullName,
  emailAddress = emailAddress,
)

fun ReferralUser.toCaseWorkerDto(): CaseWorkerDto = CaseWorkerDto(
  userType = if (authSource == AuthSource.AUTH.source) UserType.INTERNAL else UserType.EXTERNAL,
  userId = id,
  fullName = fullName,
  emailAddress = emailAddress,
)

fun UserDto.toEntity(): ReferralUser = ReferralUser(
  id = id,
  hmppsAuthId = hmppsAuthId,
  hmppsAuthUsername = hmppsAuthUsername,
  authSource = if (userType == UserType.INTERNAL) AuthSource.AUTH.source else AuthSource.NONE.source,
  fullName = fullName.trim(),
  emailAddress = emailAddress.trim().lowercase(),
)
