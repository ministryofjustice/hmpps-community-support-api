package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "referral_user")
class ReferralUser(
  @Id
  val id: UUID = UUID.randomUUID(),

  @Column(name = "hmpps_auth_id", nullable = false)
  val hmppsAuthId: String,

  @Column(name = "hmpps_auth_username", nullable = false)
  val hmppsAuthUsername: String,

  @Column(name = "auth_source", nullable = false)
  val authSource: String,

  @Column(name = "last_synced_at")
  var lastSyncedAt: LocalDateTime? = null,
)
