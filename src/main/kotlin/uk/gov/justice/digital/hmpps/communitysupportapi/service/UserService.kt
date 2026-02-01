package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.UserDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.User
import uk.gov.justice.digital.hmpps.communitysupportapi.mapper.toDto
import uk.gov.justice.digital.hmpps.communitysupportapi.mapper.toEntity
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.UserRepository
import java.time.OffsetDateTime
import java.util.UUID

@Service
class UserService(
  private val userRepository: UserRepository,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional
  fun getUser(emailAddress: String): UserDto? {
    log.info("Received user lookup request for email address: {}", emailAddress)

    val user = userRepository.findByEmailAddressIgnoreCase(emailAddress)

    if (user != null && recentlySynchronised(user)) {
      log.info("Using local cached user for {}", emailAddress)

      return user.toDto()
    }

    /**
     * somewhere to retrieve the user information (hmpps_auth?),
     * and then stores it locally
     *
     *     hmppsAuthService.getUserByEmailAddress(emailAddress)
     *
     */
    var loadedUser: UserDto?

    if (user == null) {
      log.info("Creating new local user from external service for email address {}", emailAddress)
      loadedUser = dummyExternalUser()
    } else {
      log.info("Updating local user from external service for email address {}", emailAddress)
      loadedUser = dummyExternalUser()
    }

    val savedUser: User = userRepository.save(loadedUser.toEntity())

    return savedUser.toDto()
  }

  fun recentlySynchronised(user: User): Boolean = user.lastSynchronisedAt.isAfter(OffsetDateTime.now().minusDays(1))

  /*
   * dummy external service, should be removed when actual service was in place
   */
  open fun dummyExternalUser(): UserDto = UserDto(
    id = UUID.randomUUID(),
    hmppsAuthId = null,
    hmppsAuthUsername = null,
    firstName = "Alex",
    lastName = "Smith",
    emailAddress = "emailAddress@email.com",
  )
}
