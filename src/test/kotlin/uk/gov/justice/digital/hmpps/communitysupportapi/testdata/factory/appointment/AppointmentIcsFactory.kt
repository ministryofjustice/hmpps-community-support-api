package uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.appointment

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Appointment
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentDelivery
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentIcs
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ReferralUserFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.TestEntityFactory
import java.time.LocalDateTime
import java.util.UUID

class AppointmentIcsFactory : TestEntityFactory<AppointmentIcs>() {
  private var id: UUID = UUID.randomUUID()
  private var appointment: Appointment = AppointmentFactory().create()
  private var appointmentDelivery: AppointmentDelivery = AppointmentDeliveryFactory().create()
  private var createdAt: LocalDateTime = LocalDateTime.now()
  private var startDate: LocalDateTime = LocalDateTime.now().plusDays(1)
  private var createdBy: ReferralUser = ReferralUserFactory().create()
  private var sessionCommunication: List<String> = emptyList()

  fun withId(id: UUID) = apply { this.id = id }
  fun withAppointment(appointment: Appointment) = apply { this.appointment = appointment }
  fun withAppointmentDelivery(appointmentDelivery: AppointmentDelivery) = apply { this.appointmentDelivery = appointmentDelivery }
  fun withCreatedAt(createdAt: LocalDateTime) = apply { this.createdAt = createdAt }
  fun withStartDate(startDate: LocalDateTime) = apply { this.startDate = startDate }
  fun withCreatedBy(referralUser: ReferralUser) = apply { this.createdBy = referralUser }
  fun withSessionCommunication(sessionCommunication: List<String>) = apply { this.sessionCommunication = sessionCommunication }

  override fun create(): AppointmentIcs = AppointmentIcs(
    id = id,
    appointment = appointment,
    appointmentDelivery = appointmentDelivery,
    createdAt = createdAt,
    startDate = startDate,
    createdBy = createdBy,
    sessionCommunication = sessionCommunication,
  )
}
