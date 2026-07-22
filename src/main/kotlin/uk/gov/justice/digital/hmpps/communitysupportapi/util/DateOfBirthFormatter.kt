package uk.gov.justice.digital.hmpps.communitysupportapi.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DATE_OF_BIRTH_FORMAT = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)
private val ASSESSMENT_DATE_FORMAT = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)

fun LocalDate.toFormattedDateOfBirth(): String {
  val age = Period.between(this, LocalDate.now()).years
  return "${this.format(DATE_OF_BIRTH_FORMAT)} ($age years old)"
}

fun LocalDate.toFormattedDateOfBirthLong(): String {
  val age = Period.between(this, LocalDate.now()).years
  return "${this.format(ASSESSMENT_DATE_FORMAT)} ($age years old)"
}

fun String.parseDateOfBirth(): LocalDate {
  val datePart = this.substringBefore(" (")
  return LocalDate.parse(datePart, DATE_OF_BIRTH_FORMAT)
}

fun LocalDateTime.toFormattedAssessmentDate(): String = this.toLocalDate().format(ASSESSMENT_DATE_FORMAT)
