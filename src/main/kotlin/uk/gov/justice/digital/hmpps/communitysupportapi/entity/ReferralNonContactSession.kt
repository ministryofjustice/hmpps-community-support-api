package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "referral_non_contact_session")
class ReferralNonContactSession(
  @Id
  @Column(name = "id")
  var id: UUID,

  @Column(name = "referral_id")
  val referralId: UUID,

  @Column(name = "created_at", nullable = false)
  val createdAt: OffsetDateTime = OffsetDateTime.now(),

  @Column(name = "created_by", nullable = false)
  val createdBy: String = "SYSTEM",

  @Column(name = "deleted_at")
  val deletedAt: OffsetDateTime? = null,

  @Column(name = "start_datetime", nullable = false)
  val startDateTime: OffsetDateTime,

  @Column(name = "duration_hours", nullable = false)
  val durationHours: Int,

  @Column(name = "duration_minutes", nullable = false)
  val durationMinutes: Int,

  @Column(name = "title", nullable = false)
  val title: String,

  @Column(name = "details", nullable = false)
  val details: String,
) {
  @OneToMany(fetch = FetchType.LAZY)
  @JoinColumn(name = "referral_non_contact_session_id")
  val referralPathways: MutableList<ReferralNonContactSessionReferenceDataReferralPathway> = mutableListOf()
}
