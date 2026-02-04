package uk.gov.justice.digital.hmpps.communitysupportapi.authorization

import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.communitysupportapi.client.ManageUsersClient
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralUserRepository
import uk.gov.justice.hmpps.kotlin.auth.AuthSource
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import java.util.UUID

@Component
class UserMapper(
  private val referralUserRepository: ReferralUserRepository,
  private val manageUsersClient: ManageUsersClient,
) {
  fun fromToken(
    authenticationHolder: HmppsAuthenticationHolder,
  ): ReferralUser {
    val errors = mutableListOf<String>()

    val userName = authenticationHolder.authentication.userName
      ?: run {
        errors.add("no 'user_name' claim in token")
        null
      }

    val userId = userName?.let { manageUsersClient.getUserDetails(it)?.userId }
      ?: run {
        if (userName != null) {
          errors.add("no 'user_id' claim in token")
        }
        null
      }

    val authSource = authenticationHolder.authentication.authSource
    if (authSource == AuthSource.NONE) {
      errors.add("no 'auth_source' claim in token")
    }

    if (errors.isNotEmpty()) {
      throwAccessDenied(errors)
    }

    return referralUserRepository.findByHmppsAuthId(requireNotNull(userId))
      ?: referralUserRepository.save(
        ReferralUser(
          id = UUID.randomUUID(),
          hmppsAuthId = userId,
          authSource = authSource.name,
          hmppsAuthUsername = requireNotNull(userName),
        ),
      )
  }

  private fun throwAccessDenied(errors: List<String>): Nothing = throw AccessDeniedException("could not map auth token to user: $errors")
}
