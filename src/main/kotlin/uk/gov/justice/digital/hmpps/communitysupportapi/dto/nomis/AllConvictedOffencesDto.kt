package uk.gov.justice.digital.hmpps.communitysupportapi.dto.nomis

import java.time.LocalDate

data class AllConvictedOffencesDto(
  val statuteCode: String? = null,
  val offenceCode: String? = null,
  val offenceDescription: String? = null,
  val offenceDate: LocalDate? = null,
  val latestBooking: Boolean? = null,
  val sentenceStartDate: LocalDate? = null,
  val primarySentence: Boolean? = null,
)
