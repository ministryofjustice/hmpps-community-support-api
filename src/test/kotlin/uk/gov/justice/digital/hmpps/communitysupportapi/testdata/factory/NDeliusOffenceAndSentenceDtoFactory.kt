package uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory

import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CodeDescriptionDto
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.delius.NDeliusOffenceAndSentenceDto
import java.time.OffsetDateTime

class NDeliusOffenceAndSentenceDtoFactory : TestEntityFactory<NDeliusOffenceAndSentenceDto>() {
  private var eventId: Long = 0
  private var offence: CodeDescriptionDto = CodeDescriptionDto("default-offence-code", "default offence description")
  private var offenceSubCategory: CodeDescriptionDto = CodeDescriptionDto("default-offence-sub-category-code", "default offence sub category description")
  private var outcome: CodeDescriptionDto = CodeDescriptionDto("default-outcome-code", "default outcome description")
  private var sentenceEndDate: String? = "2000-01-01"

  fun withEventId(eventId: Long) = apply { this.eventId = eventId }
  fun withOffence(offence: CodeDescriptionDto) = apply { this.offence = offence }
  fun withOffenceSubCategory(offenceSubCategory: CodeDescriptionDto) = apply { this.offenceSubCategory = offenceSubCategory }
  fun withOutcome(outcome: CodeDescriptionDto) = apply { this.outcome = outcome }
  fun withSentenceEndDate(sentenceEndDate: String) = apply { this.sentenceEndDate = sentenceEndDate }
  fun withoutSentenceEndDate() = apply { this.sentenceEndDate = null }

  override fun create(): NDeliusOffenceAndSentenceDto = NDeliusOffenceAndSentenceDto(
    eventId = eventId,
    offence = offence,
    offenceSubCategory = offenceSubCategory,
    outcome = outcome,
    sentenceEndDate = sentenceEndDate?.let { OffsetDateTime.parse(it) },
  )

  fun createAsJson(): String = """
      {
        "eventId": $eventId,
        "offence": {
          "code": "${offence.code}",
          "description": "${offence.description}"
        },
        "offenceSubCategory": {
          "code": "${offenceSubCategory.code}",
          "description": "${offenceSubCategory.description}"
        },
        "outcome": {
          "code": "${outcome.code}",
          "description": "${outcome.description}"
        },
        "sentenceEndDate": "$sentenceEndDate"
      }
  """.trimIndent()
}
