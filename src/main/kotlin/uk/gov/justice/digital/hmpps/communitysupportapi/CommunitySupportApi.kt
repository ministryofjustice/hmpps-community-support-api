package uk.gov.justice.digital.hmpps.communitysupportapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CommunitySupportApi

fun main(args: Array<String>) {
  runApplication<CommunitySupportApi>(*args)
}
