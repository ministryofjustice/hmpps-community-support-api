package uk.gov.justice.digital.hmpps.communitysupportapi.util

import java.time.format.DateTimeFormatter
import java.util.Locale

val FULL_MONTH_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)
