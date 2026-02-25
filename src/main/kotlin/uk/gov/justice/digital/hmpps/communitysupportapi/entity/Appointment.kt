package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "appointment")
class Appointment(
  @Id
  val id: UUID,

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "referral_id", nullable = false)
  val referral: Referral,

  @Column(name = "type", nullable = false)
  val type: String,
)
