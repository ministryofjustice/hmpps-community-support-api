package uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Appointment
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentDelivery
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentDeliveryMethod
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentIcs
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import java.time.LocalDateTime
import java.util.UUID

class AppointmentFactory : TestEntityFactory<Appointment>() {

  private var id: UUID = UUID.randomUUID()
  private var referral: Referral? = null
  private var type: String = "ICS"

  fun withId(id: UUID) = apply { this.id = id }
  fun withReferral(referral: Referral) = apply { this.referral = referral }
  fun withType(type: String) = apply { this.type = type }

  override fun create(): Appointment = Appointment(
    id = id,
    referral = referral ?: error("Referral must be provided"),
    type = type,
  )
}

class AppointmentDeliveryFactory : TestEntityFactory<AppointmentDelivery>() {

  private var id: UUID = UUID.randomUUID()
  private var method: AppointmentDeliveryMethod = AppointmentDeliveryMethod.PHONE_CALL
  private var methodDetails: String? = null
  private var addressLine1: String? = null
  private var addressLine2: String? = null
  private var townOrCity: String? = null
  private var county: String? = null
  private var postcode: String? = null

  fun withId(id: UUID) = apply { this.id = id }
  fun withMethod(method: AppointmentDeliveryMethod) = apply { this.method = method }
  fun withMethodDetails(methodDetails: String?) = apply { this.methodDetails = methodDetails }
  fun withAddressLine1(addressLine1: String?) = apply { this.addressLine1 = addressLine1 }
  fun withAddressLine2(addressLine2: String?) = apply { this.addressLine2 = addressLine2 }
  fun withTownOrCity(townOrCity: String?) = apply { this.townOrCity = townOrCity }
  fun withCounty(county: String?) = apply { this.county = county }
  fun withPostcode(postcode: String?) = apply { this.postcode = postcode }

  override fun create(): AppointmentDelivery = AppointmentDelivery(
    id = id,
    method = method,
    methodDetails = methodDetails,
    addressLine1 = addressLine1,
    addressLine2 = addressLine2,
    townOrCity = townOrCity,
    county = county,
    postcode = postcode,
  )
}

class AppointmentIcsFactory : TestEntityFactory<AppointmentIcs>() {

  private var id: UUID = UUID.randomUUID()
  private var appointment: Appointment? = null
  private var appointmentDelivery: AppointmentDelivery? = null
  private var createdAt: LocalDateTime = LocalDateTime.now()
  private var startDate: LocalDateTime = LocalDateTime.now().plusDays(7)
  private var createdBy: ReferralUser? = null
  private var sessionCommunication: List<String> = emptyList()

  fun withId(id: UUID) = apply { this.id = id }
  fun withAppointment(appointment: Appointment) = apply { this.appointment = appointment }
  fun withAppointmentDelivery(appointmentDelivery: AppointmentDelivery?) = apply { this.appointmentDelivery = appointmentDelivery }
  fun withCreatedAt(createdAt: LocalDateTime) = apply { this.createdAt = createdAt }
  fun withStartDate(startDate: LocalDateTime) = apply { this.startDate = startDate }
  fun withCreatedBy(createdBy: ReferralUser?) = apply { this.createdBy = createdBy }
  fun withSessionCommunication(sessionCommunication: List<String>) = apply { this.sessionCommunication = sessionCommunication }

  override fun create(): AppointmentIcs = AppointmentIcs(
    id = id,
    appointment = appointment ?: error("Appointment must be provided"),
    appointmentDelivery = appointmentDelivery,
    createdAt = createdAt,
    appointmentDateTime = startDate,
    createdBy = createdBy,
    sessionCommunication = sessionCommunication,
  )
}
