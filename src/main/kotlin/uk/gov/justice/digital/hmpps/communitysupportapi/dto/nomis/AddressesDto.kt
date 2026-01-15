package uk.gov.justice.digital.hmpps.communitysupportapi.dto.nomis

import uk.gov.justice.digital.hmpps.communitysupportapi.dto.PhoneNumberDto
import java.time.LocalDate

data class AddressesDto(
  val fullAddress: String? = null,
  val postalCode: String? = null,
  val startDate: LocalDate? = null,
  val primaryAddress: Boolean = false,
  val noFixedAddress: Boolean = false,
  val phoneNumber: PhoneNumberDto? = null,
)
