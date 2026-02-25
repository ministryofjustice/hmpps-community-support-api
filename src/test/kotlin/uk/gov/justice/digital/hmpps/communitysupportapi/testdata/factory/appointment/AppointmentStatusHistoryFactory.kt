package uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.appointment

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Appointment
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentStatusHistory
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentStatusHistoryType
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.TestEntityFactory
import java.time.LocalDateTime
import java.util.UUID

class AppointmentStatusHistoryFactory : TestEntityFactory<AppointmentStatusHistory>() {
  private var id: UUID = UUID.randomUUID()
  private var appointment: Appointment = AppointmentFactory().create()
  private var createdAt: LocalDateTime = LocalDateTime.now()
  private var status: AppointmentStatusHistoryType = AppointmentStatusHistoryType.SCHEDULED

  fun withId(id: UUID) = apply { this.id = id }
  fun withAppointment(appointment: Appointment) = apply { this.appointment = appointment }
  fun withCreatedAt(createdAt: LocalDateTime) = apply { this.createdAt = createdAt }
  fun withStatus(status: AppointmentStatusHistoryType) = apply { this.status = status }

  override fun create(): AppointmentStatusHistory = AppointmentStatusHistory(appointment = appointment, createdAt = createdAt, status = status)
}
