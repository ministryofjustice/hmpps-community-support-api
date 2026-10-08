package uk.gov.justice.digital.hmpps.communitysupportapi.util

import java.time.format.DateTimeFormatter
import java.util.Locale.ENGLISH

val FULL_MONTH_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", ENGLISH)
val APPOINTMENT_DATETIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm EEEE d MMMM yyyy", ENGLISH)
