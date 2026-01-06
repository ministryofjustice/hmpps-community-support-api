package uk.gov.justice.digital.hmpps.communitysupportapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.PersonDto
import uk.gov.justice.digital.hmpps.communitysupportapi.mapper.toDto
import uk.gov.justice.digital.hmpps.communitysupportapi.service.PersonService

@RestController
@RequestMapping("/person")
@PreAuthorize("hasAnyRole('ROLE_IPB_FRONTEND_RW')")
class PersonController(
  private val personService: PersonService,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Operation(summary = "Find a person by an identifier i.e Prison Number or CRN")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Person found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = PersonDto::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Person not found",
        content = [Content(mediaType = "application/json")],
      ),
    ],
  )

  @GetMapping("/{personIdentifier}")
  fun getPersonDetails(
    @PathVariable personIdentifier: String,
  ): ResponseEntity<PersonDto> {
    log.info("Attempt to find person using identifier {}", personIdentifier)

    val person = personService.getPerson(personIdentifier)
    return ResponseEntity.ok(person.toDto())
  }
}
