package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "referral_withdrawal_details")
class ReferralWithdrawalDetails(
  @Id
  val id: UUID,

  @Column(name = "referral_id", nullable = false)
  val referralId: UUID,

  @Column(name = "reason", nullable = false)
  var reason: String,

  @Column(name = "reason_details")
  var reasonDetails: String? = null,

  @Column(name = "created_at", nullable = false)
  val createdAt: OffsetDateTime,

  @Column(name = "created_by", nullable = false)
  val createdBy: UUID,
)
