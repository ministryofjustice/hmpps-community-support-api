package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral

data class ServiceDaysPageDto(
  @Schema(description = "The number of service days allocated to the referral")
  @param:JsonProperty("service_days")
  @get:JsonProperty("service_days")
  val serviceDays: Int?,
) {
  companion object {
    fun from(referral: Referral): ServiceDaysPageDto = ServiceDaysPageDto(
      serviceDays = referral.serviceDays,
    )
  }
}
