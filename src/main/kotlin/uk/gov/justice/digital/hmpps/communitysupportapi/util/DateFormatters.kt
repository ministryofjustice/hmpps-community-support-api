package uk.gov.justice.digital.hmpps.communitysupportapi.util

import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.Locale.ENGLISH

val FULL_MONTH_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", ENGLISH)
val APPOINTMENT_DATETIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm EEEE d MMMM yyyy", ENGLISH)
val SHORT_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", ENGLISH)
val SHORT_DATE_TIME_FORMAT: DateTimeFormatter = DateTimeFormatterBuilder()
  .appendPattern("EEE d MMM yyyy h:mm")
  .appendText(ChronoField.AMPM_OF_DAY, mapOf(0L to "am", 1L to "pm"))
  .toFormatter(ENGLISH)
