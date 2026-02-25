package uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.appointment

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Appointment
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ReferralFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.TestEntityFactory
import java.util.UUID

class AppointmentFactory : TestEntityFactory<Appointment>() {
  private var id: UUID = UUID.randomUUID()
  private var referral: Referral = ReferralFactory().create()
  private var type: AppointmentType = AppointmentType.ICS

  fun withId(id: UUID) = apply { this.id = id }
  fun withReferral(referral: Referral) = apply { this.referral = referral }
  fun withType(type: AppointmentType) = apply { this.type = type }

  override fun create(): Appointment = Appointment(id = id, referral = referral, type = type)
}
