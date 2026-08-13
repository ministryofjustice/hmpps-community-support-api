package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import java.time.OffsetDateTime

data class ServiceEndDatePageDto(
  @Schema(description = "The target service completion date for the referral")
  @get:JsonProperty("target_service_completion_date")
  val targetServiceCompletionDate: OffsetDateTime?,
  @Schema(description = "The reason the target service completion date was set")
  @get:JsonProperty("target_service_completion_reason")
  val targetServiceCompletionReason: String?,
) {
  companion object {
    fun from(referral: Referral): ServiceEndDatePageDto = ServiceEndDatePageDto(
      targetServiceCompletionDate = referral.targetServiceCompletionDate,
      targetServiceCompletionReason = referral.targetServiceCompletionDateReason,
    )
  }
}
