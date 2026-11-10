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
@Table(name = "referral_notes")
class ReferralNote(
  @Id
  @Column(name = "id")
  val id: UUID,

  @Column(name = "referral_id")
  val referralId: UUID,

  @Column(name = "created_at", nullable = false)
  val createdAt: OffsetDateTime? = null,

  @Column(name = "created_by", nullable = false)
  val createdBy: String = "SYSTEM",

  @Column(name = "deleted_at")
  val deletedAt: OffsetDateTime? = null,

  @Column(name = "title", nullable = false)
  val title: String,

  @Column(name = "details", nullable = false)
  val details: String,

  @Column(name = "should_notify", nullable = false)
  val shouldNotify: Boolean = false,
) {
  @OneToMany(fetch = FetchType.LAZY)
  @JoinColumn(name = "referral_notes_id")
  val referralPathways: MutableList<ReferralNotesReferenceDataReferralPathway> = mutableListOf()
}
