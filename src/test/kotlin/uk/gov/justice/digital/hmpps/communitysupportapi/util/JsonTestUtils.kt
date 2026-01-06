package uk.gov.justice.digital.hmpps.communitysupportapi.util


import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

val testObjectMapper = jacksonObjectMapper()
  .registerKotlinModule()

fun Any.toJson(): String = testObjectMapper.writeValueAsString(this)
