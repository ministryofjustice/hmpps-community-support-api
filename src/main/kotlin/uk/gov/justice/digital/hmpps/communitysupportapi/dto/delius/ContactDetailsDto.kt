package uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius

import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CodeDescriptionDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.PhoneNumberDto
import java.time.LocalDate

data class ContactDetailsDto(
  val phoneNumbers: List<PhoneNumberDto> = emptyList(),
  val emailAddresses: List<String> = emptyList(),
  val allowSMS: Boolean? = null,
  val addresses: List<AddressDto> = emptyList(),
)

data class AddressDto(
  val id: Long,
  val from: LocalDate? = null,
  val to: LocalDate? = null,
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
  val status: CodeDescriptionDto? = null,
  val type: CodeDescriptionDto? = null,
)
