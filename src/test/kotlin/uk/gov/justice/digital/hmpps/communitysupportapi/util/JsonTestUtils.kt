package uk.gov.justice.digital.hmpps.communitysupportapi.util

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

val testObjectMapper = jacksonObjectMapper()
  .registerModule(JavaTimeModule())
  .registerKotlinModule()

fun Any.toJson(): String = testObjectMapper.writeValueAsString(this)
