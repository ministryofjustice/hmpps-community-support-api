package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "referral_provider_assignment")
class ReferralProviderAssignment(
  @Id
  val id: UUID = UUID.randomUUID(),

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "referral_id", nullable = false)
  val referral: Referral,

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "community_service_provider_id", nullable = false)
  val communityServiceProvider: CommunityServiceProvider,

  @Column(name = "created_at", nullable = false)
  val createdAt: LocalDateTime = LocalDateTime.now(),

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by")
  val createdBy: ReferralUser? = null,
)
