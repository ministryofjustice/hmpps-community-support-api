package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "appointment_ics")
class AppointmentIcs(
  @Id
  val id: UUID = UUID.randomUUID(),

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "appointment_id", nullable = false)
  val appointment: Appointment,

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "appointment_delivery_id")
  val appointmentDelivery: AppointmentDelivery? = null,

  @Column(name = "created_at", nullable = false)
  val createdAt: LocalDateTime = LocalDateTime.now(),

  @Column(name = "start_date", nullable = false)
  val startDate: LocalDateTime,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by")
  val createdBy: ReferralUser? = null,

  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(name = "session_communication", columnDefinition = "text[]")
  val sessionCommunication: List<String> = emptyList(),
)
