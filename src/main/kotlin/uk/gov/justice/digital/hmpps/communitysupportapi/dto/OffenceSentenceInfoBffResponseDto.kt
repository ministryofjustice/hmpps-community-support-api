package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.OffenceSentenceDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person
import uk.gov.justice.digital.hmpps.communitysupportapi.util.toFormattedDateOfBirthLong

data class OffenceSentenceInfoBffResponseDto(
  val firstName: String,
  val lastName: String,
  val crn: String,
  val dateOfBirth: String,
  val offenceSentenceInfo: OffenceSentenceDto,
) {
  companion object {
    fun from(person: Person, crn: String, offenceSentenceInfo: OffenceSentenceDto): OffenceSentenceInfoBffResponseDto = OffenceSentenceInfoBffResponseDto(
      firstName = person.firstName,
      lastName = person.lastName,
      crn = crn,
      dateOfBirth = person.dateOfBirth.toFormattedDateOfBirthLong(),
      offenceSentenceInfo = offenceSentenceInfo,
    )
  }
}
