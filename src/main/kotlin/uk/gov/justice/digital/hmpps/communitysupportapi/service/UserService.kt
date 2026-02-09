package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.communitysupportapi.client.ManageUsersClient
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.UserDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import uk.gov.justice.digital.hmpps.communitysupportapi.mapper.toDto
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralUserRepository
import uk.gov.justice.hmpps.kotlin.auth.AuthSource
import java.time.LocalDateTime
import java.util.UUID

@Service
class UserService(
  private val referralUserRepository: ReferralUserRepository,
  private val manageUsersClient: ManageUsersClient,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional
  fun getUser(emailAddress: String): UserDto? {
    log.info("Received user lookup request for email address: {}", emailAddress)

    val user = referralUserRepository.findByEmailAddressIgnoreCase(emailAddress)

    if (user != null) {
      log.info("Using local cached user for {}", emailAddress)
      return user.toDto()
    } else {
      var loadedUser: ReferralUser?
      try {
        val userDetails = manageUsersClient.getUserDetails(emailAddress)
        if (userDetails != null) {
          loadedUser = referralUserRepository.findByHmppsAuthId((userDetails.userId))
            ?: referralUserRepository.save(
              ReferralUser(
                id = UUID.randomUUID(),
                hmppsAuthId = userDetails.userId,
                hmppsAuthUsername = userDetails.username,
                authSource = userDetails.authSource,
                fullName = userDetails.name,
                emailAddress = userDetails.username.trim().lowercase(),
              ),
            )
          return loadedUser.toDto()
        }
      } catch (e: Exception) {
        log.error("Error getting user details for email address: {}", emailAddress)
      }
      return null
    }
  }

  /*
   * dummy test service, should be removed when actual service was in place
   */
  open fun getTestUser(username: String): UserDto? = UserDto(
    id = UUID.randomUUID(),
    hmppsAuthId = "hmppsAuthId",
    hmppsAuthUsername = username,
    authSource = AuthSource.AUTH.source,
    fullName = "Test User",
    emailAddress = username,
  )

  fun recentlySynchronised(user: ReferralUser): Boolean = user.lastSyncedAt?.isAfter(LocalDateTime.now().minusDays(1)) ?: false
}
