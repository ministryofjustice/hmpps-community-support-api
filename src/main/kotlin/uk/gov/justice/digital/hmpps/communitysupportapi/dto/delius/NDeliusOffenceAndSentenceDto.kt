package uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius

import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CodeDescriptionDto
import java.time.OffsetDateTime

data class NDeliusOffenceAndSentenceDto(
  val eventId: Long,
  val offence: CodeDescriptionDto,
  val offenceSubCategory: CodeDescriptionDto,
  val outcome: CodeDescriptionDto,
  val sentenceEndDate: OffsetDateTime? = null,
)
