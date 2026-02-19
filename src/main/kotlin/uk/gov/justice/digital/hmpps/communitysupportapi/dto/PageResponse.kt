package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import org.springframework.data.domain.Page

data class PageResponse<T : Any>(
  val content: List<T>,
  val page: Int,
  val size: Int,
  val totalElements: Long,
  val totalPages: Int,
)

fun <T : Any> Page<T>.toResponse() = PageResponse(
  content = content,
  page = number,
  size = size,
  totalElements = totalElements,
  totalPages = totalPages,
)
