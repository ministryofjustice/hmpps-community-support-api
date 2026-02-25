package uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.appointment

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentDelivery
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.AppointmentDeliveryMethod
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.TestEntityFactory
import java.util.UUID

class AppointmentDeliveryFactory : TestEntityFactory<AppointmentDelivery>() {

  private var id: UUID = UUID.randomUUID()
  private var method: AppointmentDeliveryMethod = AppointmentDeliveryMethod.IN_PERSON_PROBATION_OFFICE
  private var methodDetails: String? = null

  /**
   * Address fields – only populated for IN_PERSON_OTHER_LOCATION.
   */
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
