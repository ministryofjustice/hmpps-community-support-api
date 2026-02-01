package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.OffsetDateTime

@Entity
@Table(
  name = "referral_user_assignment",
  uniqueConstraints = [
    UniqueConstraint(name = "uk_referral_user", columnNames = ["referral_id", "user_id"]),
  ],
)
class ReferralUserAssignment(
  @EmbeddedId
  val id: ReferralUserAssignmentId,

//  @Column(name = "referral_id", nullable = false, insertable = false, updatable = false)
//  val referral: UUID,
//
//  @Column(name = "user_id", nullable = false, insertable = false, updatable = false)
//  val user: UUID,

  @Column(name = "created_by", nullable = false)
  val createdBy: String,

  @Column(name = "created_at", nullable = false)
  val createdAt: OffsetDateTime,

  @Column(name = "deleted_by")
  val deletedBy: String? = null,

  @Column(name = "deleted_at")
  val deletedAt: OffsetDateTime? = null,
)
