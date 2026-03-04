package uk.gov.justice.digital.hmpps.communitysupportapi.mapper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentDeliveryMethod
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentStatusHistoryType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentType
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.AppointmentDeliveryFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.AppointmentFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.AppointmentIcsFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ReferralFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ReferralUserFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.appointment.AppointmentStatusHistoryFactory
import java.time.LocalDateTime

class AppointmentMapperTest {

  @Test
  fun `Appointment toDto maps all fields correctly`() {
    val referral = ReferralFactory().create()
    val appointment = AppointmentFactory().withReferral(referral).withType(AppointmentType.ICS).create()

    val dto = appointment.toDto()

    assertEquals(appointment.id, dto.id)
    assertEquals(referral.id, dto.referralId)
    assertEquals(appointment.type, dto.type)
  }

  @Test
  fun `AppointmentDelivery toDto maps all fields correctly`() {
    val delivery = AppointmentDeliveryFactory()
      .withMethod(AppointmentDeliveryMethod.IN_PERSON_OTHER_LOCATION)
      .withMethodDetails("Zoom")
      .withAddressLine1("AddressLine 1")
      .withAddressLine2("AddressLine 2")
      .withTownOrCity("City")
      .withCounty("County")
      .withPostcode("Postcode")
      .create()

    val dto = delivery.toDto()

    assertEquals(delivery.id, dto.id)
    assertEquals(delivery.method, dto.method)
    assertEquals(delivery.methodDetails, dto.methodDetails)
    assertEquals(delivery.addressLine1, dto.addressLine1)
    assertEquals(delivery.addressLine2, dto.addressLine2)
    assertEquals(delivery.townOrCity, dto.townOrCity)
    assertEquals(delivery.county, dto.county)
    assertEquals(delivery.postcode, dto.postcode)
  }

  @Test
  fun `AppointmentIcs toDto maps all fields correctly`() {
    val referralUser = ReferralUserFactory().create()
    val referral = ReferralFactory().create()
    val appointment = AppointmentFactory()
      .withReferral(referral)
      .create()

    val delivery = AppointmentDeliveryFactory().create()

    val createdAt = LocalDateTime.of(2026, 3, 1, 12, 0)
    val appointmentDateTime = LocalDateTime.of(2026, 3, 2, 12, 0)

    val ics = AppointmentIcsFactory()
      .withAppointment(appointment)
      .withAppointmentDelivery(delivery)
      .withCreatedBy(referralUser)
      .withCreatedAt(createdAt)
      .withAppointmentDateTime(appointmentDateTime)
      .withSessionCommunication(listOf("EMAIL", "SMS"))
      .create()

    val dto = ics.toDto()

    assertEquals(appointment.id, dto.appointmentId)
    assertEquals(createdAt, dto.createdAt)
    assertEquals(appointmentDateTime, dto.appointmentDateTime)
    assertEquals(listOf("EMAIL", "SMS"), dto.sessionCommunication)

    assertNotNull(dto.appointmentDelivery)
    assertEquals(delivery.id, dto.appointmentDelivery.id)
  }

  @Test
  fun `AppointmentIcs toDto handles null delivery`() {
    val referralUser = ReferralUserFactory().create()
    val referral = ReferralFactory().create()
    val appointment = AppointmentFactory()
      .withReferral(referral)
      .create()

    val ics = AppointmentIcsFactory()
      .withAppointment(appointment)
      .withAppointmentDelivery(null)
      .withCreatedBy(referralUser)
      .create()

    val dto = ics.toDto()

    assertNull(dto.appointmentDelivery)
  }

  @Test
  fun `AppointmentStatusHistory toDto maps all fields correctly`() {
    val referral = ReferralFactory().create()
    val appointment = AppointmentFactory()
      .withReferral(referral)
      .create()

    val createdAt = LocalDateTime.of(2026, 3, 1, 12, 0)

    val statusHistory = AppointmentStatusHistoryFactory()
      .withAppointment(appointment)
      .withStatus(AppointmentStatusHistoryType.SCHEDULED)
      .withCreatedAt(createdAt)
      .create()

    val dto = statusHistory.toDto()

    assertEquals(AppointmentStatusHistoryType.SCHEDULED, dto.status)
    assertEquals(createdAt, dto.createdAt)
  }
}
