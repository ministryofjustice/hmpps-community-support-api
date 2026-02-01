package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

enum class UserType {
  UNDEFINED,
  INTERNAL,
  EXTERNAL,
}

@Entity
@Table(name = "app_user")
class User(
  @Id
  val id: UUID,

  @Column(name = "hmpps_auth_id")
  val hmppsAuthId: String? = null,

  @Column(name = "hmpps_auth_username")
  val hmppsAuthUsername: String? = null,

  @Column(name = "first_name", nullable = false)
  val firstName: String,

  @Column(name = "last_name", nullable = false)
  val lastName: String,

  @Column(name = "email_address", nullable = false)
  val emailAddress: String,

  @Enumerated(EnumType.STRING)
  @Column(name = "user_type", nullable = false)
  val userType: UserType = UserType.UNDEFINED,

  @Column(name = "last_synchronised_at", nullable = false)
  val lastSynchronisedAt: OffsetDateTime = OffsetDateTime.now(),
) {
  val fullName: String
    get() = listOfNotNull(firstName.trim(), lastName.trim())
      .filter { it.isNotBlank() }
      .joinToString(" ")
}
