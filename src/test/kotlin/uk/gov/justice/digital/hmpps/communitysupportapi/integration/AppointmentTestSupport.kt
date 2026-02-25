package uk.gov.justice.digital.hmpps.communitysupportapi.integration

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Appointment
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentDelivery
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentDeliveryMethod
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentIcs
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentStatusHistory
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentStatusHistoryType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentDeliveryRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentIcsRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.AppointmentStatusHistoryRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.appointment.AppointmentDeliveryFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.appointment.AppointmentFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.appointment.AppointmentIcsFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.appointment.AppointmentStatusHistoryFactory
import java.time.LocalDateTime

@Component
class AppointmentTestSupport(
  private val appointmentRepository: AppointmentRepository,
  private val appointmentIcsRepository: AppointmentIcsRepository,
  private val appointmentDeliveryRepository: AppointmentDeliveryRepository,
  private val appointmentStatusHistoryRepository: AppointmentStatusHistoryRepository,
) {
  fun createAppointment(referral: Referral, type: AppointmentType = AppointmentType.ICS): Appointment = appointmentRepository.save(
    AppointmentFactory()
      .withReferral(referral)
      .withType(type)
      .create(),
  )

  fun createAppointmentDelivery(
    method: AppointmentDeliveryMethod = AppointmentDeliveryMethod.VIDEO_CALL,
    methodDetails: String? = null,
    addressLineOne: String? = null,
    addressLineTwo: String? = null,
    county: String? = null,
    townOrCity: String? = null,
    postCode: String? = null,
  ): AppointmentDelivery = appointmentDeliveryRepository.save(
    AppointmentDeliveryFactory()
      .withMethod(method)
      .withMethodDetails(methodDetails)
      .withAddressLine1(addressLineOne)
      .withAddressLine2(addressLineTwo)
      .withCounty(county)
      .withTownOrCity(townOrCity)
      .withPostcode(postCode)
      .create(),
  )

  fun createAppointmentIcs(
    appointment: Appointment,
    delivery: AppointmentDelivery,
    user: ReferralUser,
    startDate: LocalDateTime = LocalDateTime.now(),
    createdAt: LocalDateTime = LocalDateTime.now().minusDays(1),
    communications: List<String>,
  ): AppointmentIcs = appointmentIcsRepository.save(
    AppointmentIcsFactory()
      .withAppointment(appointment)
      .withAppointmentDelivery(delivery)
      .withCreatedBy(user)
      .withStartDate(startDate)
      .withCreatedAt(createdAt)
      .withSessionCommunication(communications)
      .create(),
  )

  fun createAppointmentStatusHistory(
    appointment: Appointment,
    status: AppointmentStatusHistoryType,
    createdAt: LocalDateTime = LocalDateTime.now(),
  ): AppointmentStatusHistory = appointmentStatusHistoryRepository.save(
    AppointmentStatusHistoryFactory()
      .withAppointment(appointment)
      .withStatus(status)
      .withCreatedAt(createdAt)
      .create(),
  )
}
