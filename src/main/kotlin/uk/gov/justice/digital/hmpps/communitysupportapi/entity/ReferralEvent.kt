package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "referral_event")
class ReferralEvent(
  @Id
  val id: UUID,

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "referral_id", nullable = false)
  var referral: Referral,

  @Column(name = "event_type")
  val eventType: String? = null,

  @Column(name = "created_at", nullable = false)
  @CreatedDate
  val createdAt: LocalDateTime,

  @Column(name = "actor_type", nullable = false)
  val actorType: String,

  @Column(name = "actor_id")
  val actorId: String,
)
