package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.User
import java.util.UUID

interface UserRepository : JpaRepository<User, UUID> {
  fun findByEmailAddressIgnoreCase(emailAddress: String): User?
}
