package uk.gov.justice.digital.hmpps.communitysupportapi.validation

import jakarta.validation.ValidationException
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.communitysupportapi.model.PersonIdentifier

@Component
class PersonIdentifierValidator {

  private val crnRegex = Regex("^[A-Za-z]\\d{6}$")
  private val prisonerNumberRegex = Regex("^[A-Z]\\d{4}[A-Z]{2}$")

  fun validate(value: String?): PersonIdentifier {
    val identifier = value?.trim()

    if (identifier.isNullOrEmpty()) {
      throw ValidationException("Person Identifier must be provided")
    }

    return when {
      crnRegex.matches(identifier) ->
        PersonIdentifier.Crn(identifier)

      prisonerNumberRegex.matches(identifier) ->
        PersonIdentifier.PrisonerNumber(identifier)

      else ->
        throw ValidationException("Invalid Person Identifier format")
    }
  }
}
