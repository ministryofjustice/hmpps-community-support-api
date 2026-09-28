package uk.gov.justice.digital.hmpps.communitysupportapi.util.placeholder

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.util.Placeholders
import java.time.format.DateTimeFormatter
import java.util.Locale

class ServiceEndDatePlaceholder(
  private val referral: Referral,
) : Placeholders {
  override fun resolve(tokens: Set<String>): Map<String, String> = buildMap {
    val targetServiceCompletionDate = referral.targetServiceCompletionDate ?: return@buildMap
    val formatted = targetServiceCompletionDate.format(DATE_FORMATTER)
    SERVICE_END_DATE_TOKENS.forEach { token ->
      if (token in tokens) put(token, formatted)
    }
  }

  companion object {
    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)
    val SERVICE_END_DATE_TOKENS: Set<String> = setOf("service_end_date", "serviceEndDate")

    fun neededFor(tokens: Set<String>): Boolean = tokens.any { it in SERVICE_END_DATE_TOKENS }
  }
}
