package uk.gov.justice.digital.hmpps.communitysupportapi.dto.nomis

data class ContactDetails(
  val phoneNumbers: List<PhoneNumber> = emptyList(),
  val emailAddresses: List<String> = emptyList(),
  val allowSMS: Boolean = true,
  val addresses: List<Address> = emptyList()
)

data class PhoneNumber(
  val number: String,
  val type: String
)

data class Address(
  val id: Int = 0,
  val from: String? = null,
  val to: String? = null,
  val noFixedAbode: Boolean = false,
  val notes: String? = null,
  val addressNumber: String? = null,
  val buildingName: String? = null,
  val streetName: String? = null,
  val district: String? = null,
  val town: String? = null,
  val county: String? = null,
  val postcode: String? = null,
  val telephoneNumber: String? = null,
  val status: CodeDescription? = null,
  val type: CodeDescription? = null
)
