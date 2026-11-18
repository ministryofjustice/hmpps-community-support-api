package uk.gov.justice.digital.hmpps.communitysupportapi.validation.appointments

import jakarta.validation.ValidationException
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.CreateIcsFeedbackRequest

object CreateIcsFeedbackValidator {
  fun validate(request: CreateIcsFeedbackRequest) {
    val record = request.record
    if (!record.didSessionHappen && record.didPersonAttend == false) {
      val errors = listOfNotNull(
        "howTheyTriedToContactThePerson is required when the person did not attend"
          .takeIf { record.howTheyTriedToContactThePerson.isNullOrBlank() },
        "reasonsTheyDidNotAttend is required when the person did not attend"
          .takeIf { record.reasonsTheyDidNotAttend.isNullOrBlank() },
      )
      if (errors.isNotEmpty()) {
        throw ValidationException(errors.joinToString(", "))
      }
    }
  }
}
