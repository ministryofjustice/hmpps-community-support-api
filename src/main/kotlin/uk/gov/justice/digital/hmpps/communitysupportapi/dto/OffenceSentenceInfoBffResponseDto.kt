package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.OffenceSentenceDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person

data class OffenceSentenceInfoBffResponseDto(
  val firstName: String,
  val lastName: String,
  val offenceSentenceInfo: OffenceSentenceDto,
) {
  companion object {
    // TODO: this currently documents intended future behavior only; pending nDelius custody integration,
    //  this mapping remains a pass-through. Once nDelius confirms custody status is available here,
    //  populate only the relevant date field: expectedReleaseDate for custody cases, otherwise sentenceEndDate.
    fun from(person: Person, offenceSentenceInfo: OffenceSentenceDto): OffenceSentenceInfoBffResponseDto = OffenceSentenceInfoBffResponseDto(
      firstName = person.firstName,
      lastName = person.lastName,
      offenceSentenceInfo = offenceSentenceInfo,
    )
  }
}
