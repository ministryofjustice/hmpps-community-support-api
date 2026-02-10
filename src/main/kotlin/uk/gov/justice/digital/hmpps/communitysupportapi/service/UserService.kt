package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.communitysupportapi.client.ManageUsersClient
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.UserDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import uk.gov.justice.digital.hmpps.communitysupportapi.mapper.toDto
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ReferralUserRepository
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

    val user = referralUserRepository.findByHmppsAuthUsernameIgnoreCase(emailAddress)

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
                hmppsAuthUsername = userDetails.username.trim().lowercase(),
                authSource = userDetails.authSource,
                fullName = userDetails.name,
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

  fun recentlySynchronised(user: ReferralUser): Boolean = user.lastSyncedAt?.isAfter(LocalDateTime.now().minusDays(1)) ?: false
}
