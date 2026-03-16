package uk.gov.justice.digital.hmpps.communitysupportapi.validation

import jakarta.validation.ValidationException
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.communitysupportapi.model.CaseIdentifier
import java.util.UUID

@Component
class CaseIdentifierValidator {
  private val caseIdentifierRegex = Regex("^[A-Z]{2}\\d{4}[A-Z]{2}$")
  private val uuidCaseIdentifierValidator = Regex("^[A-Za-z0-9]{8}-[A-Za-z0-9]{4}-[A-Za-z0-9]{4}-[A-Za-z0-9]{4}-[A-Za-z0-9]{12}$")

  fun validate(value: String?): CaseIdentifier {
    val identifier = value?.trim()

    if (identifier.isNullOrEmpty()) throw ValidationException("Case identifier must be provided")

    return when {
      caseIdentifierRegex.matches(identifier) -> CaseIdentifier.CaseId(identifier)

      uuidCaseIdentifierValidator.matches(identifier) ->
        CaseIdentifier.ReferralId(UUID.fromString(identifier))

      else ->
        throw ValidationException("Invalid Case Identifier format")
    }
  }
}
