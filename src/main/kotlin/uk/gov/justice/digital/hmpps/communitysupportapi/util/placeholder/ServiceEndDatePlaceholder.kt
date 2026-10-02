package uk.gov.justice.digital.hmpps.communitysupportapi.util.placeholder

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.util.FULL_MONTH_DATE_FORMAT
import uk.gov.justice.digital.hmpps.communitysupportapi.util.Placeholders

class ServiceEndDatePlaceholder(
  private val referral: Referral,
) : Placeholders {
  override fun resolve(tokens: Set<String>): Map<String, String> = buildMap {
    val targetServiceCompletionDate = referral.targetServiceCompletionDate ?: return@buildMap
    val formatted = targetServiceCompletionDate.format(FULL_MONTH_DATE_FORMAT)
    SERVICE_END_DATE_TOKENS.forEach { token ->
      if (token in tokens) put(token, formatted)
    }
  }

  companion object {
    val SERVICE_END_DATE_TOKENS: Set<String> = setOf("service_end_date", "serviceEndDate")

    fun neededFor(tokens: Set<String>): Boolean = tokens.any { it in SERVICE_END_DATE_TOKENS }
  }
}
