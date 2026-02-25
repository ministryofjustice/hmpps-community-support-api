package uk.gov.justice.digital.hmpps.communitysupportapi.entity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

enum class AppointmentDeliveryMethod {
  PHONE_CALL,
  VIDEO_CALL,
  IN_PERSON_PROBATION_OFFICE,
  IN_PERSON_OTHER_LOCATION,
}

@Entity
@Table(name = "appointment_delivery")
class AppointmentDelivery(
  @Id
  val id: UUID = UUID.randomUUID(),
  @Enumerated(EnumType.STRING)
  @Column(name = "method", nullable = false)
  val method: AppointmentDeliveryMethod,

  @Column(name = "method_details")
  val methodDetails: String? = null,
  /**
   * Address fields – only populated for IN_PERSON_OTHER_LOCATION.
   */
  @Column(name = "address_line1")
  val addressLine1: String? = null,
  @Column(name = "address_line2")
  val addressLine2: String? = null,
  @Column(name = "town_or_city")
  val townOrCity: String? = null,
  @Column(name = "county")
  val county: String? = null,
  @Column(name = "postcode")
  val postcode: String? = null,
)
