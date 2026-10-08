package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.util.UUID

enum class AppointmentType {
  ICS,
  CONTACT_SESSION,
  POST_RELEASE_SESSION,
  PRE_RELEASE_SESSION,
  HANDOVER_SESSION,
}

@Entity
@Table(name = "appointment")
class Appointment(
  @Id
  val id: UUID,

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "referral_id", nullable = false)
  val referral: Referral,

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  val type: AppointmentType,

  @OneToMany(mappedBy = "appointment", fetch = FetchType.LAZY)
  val appointmentHistory: MutableList<AppointmentHistory> = mutableListOf(),
)
