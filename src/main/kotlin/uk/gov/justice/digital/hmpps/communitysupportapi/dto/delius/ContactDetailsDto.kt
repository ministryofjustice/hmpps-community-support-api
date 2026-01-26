package uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CodeDescriptionDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.PhoneNumberDto
import java.time.LocalDate

data class ContactDetailsDto(
  @JsonSetter(nulls = Nulls.AS_EMPTY)
  val phoneNumbers: List<PhoneNumberDto> = emptyList(),
  @JsonSetter(nulls = Nulls.AS_EMPTY)
  val emailAddresses: List<String> = emptyList(),
  val allowSMS: Boolean? = null,
  @JsonSetter(nulls = Nulls.AS_EMPTY)
  val addresses: List<AddressDto> = emptyList(),
)

data class AddressDto(
  val id: Long,
  val from: LocalDate? = null,
  val to: LocalDate? = null,
  val noFixedAbode: Boolean? = null,
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
