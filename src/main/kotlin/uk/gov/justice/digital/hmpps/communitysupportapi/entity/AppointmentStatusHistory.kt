package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.io.Serializable
import java.time.LocalDateTime
import java.util.UUID

data class AppointmentStatusHistoryId(
  val appointment: UUID? = null,
  val createdAt: LocalDateTime? = null,
) : Serializable

@Entity
@Table(name = "appointment_status_history")
@IdClass(AppointmentStatusHistoryId::class)
class AppointmentStatusHistory(
  @Id
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "appointment_id", nullable = false)
  val appointment: Appointment,

  @Id
  @Column(name = "created_at", nullable = false)
  val createdAt: LocalDateTime = LocalDateTime.now(),

  @Column(name = "status", nullable = false)
  val status: String,
)
