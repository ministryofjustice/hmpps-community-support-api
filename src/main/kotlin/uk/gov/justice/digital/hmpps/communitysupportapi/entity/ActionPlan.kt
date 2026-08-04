package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "action_plan")
data class ActionPlan(
  @Id
  @Column(name = "id")
  val id: UUID,

  @Column(name = "referral_id", nullable = false)
  val referralId: UUID,

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "referral_id", insertable = false, updatable = false, unique = true)
  val referral: Referral? = null,

  @Column(name = "created_at", nullable = false)
  val createdAt: OffsetDateTime? = null,

  @Column(name = "updated_at")
  val updatedAt: OffsetDateTime? = null,
)
