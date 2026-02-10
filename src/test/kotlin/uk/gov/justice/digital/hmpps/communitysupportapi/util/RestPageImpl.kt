package uk.gov.justice.digital.hmpps.communitysupportapi.util

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

// Test-only helper for deserialising Spring Page responses

class RestPageImpl<T : Any>
@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
constructor(
  @JsonProperty("content") content: List<T>,
  @JsonProperty("number") number: Int,
  @JsonProperty("size") size: Int,
  @JsonProperty("totalElements") totalElements: Long,
) : PageImpl<T>(
  content.toList(), // ensure it is immutable list
  PageRequest.of(number, size),
  totalElements,
)
