package uk.gov.justice.digital.hmpps.communitysupportapi.validation.actionplan

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionDeliveryDetailsQuestionAnswer
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [NoDuplicateAnswerValuesValidator::class])
annotation class NoDuplicateAnswerValues(
  val message: String = "Duplicate response values provided for the same question",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = [],
)

class NoDuplicateAnswerValuesValidator : ConstraintValidator<NoDuplicateAnswerValues, List<SessionDeliveryDetailsQuestionAnswer>> {
  override fun isValid(value: List<SessionDeliveryDetailsQuestionAnswer>?, context: ConstraintValidatorContext): Boolean {
    if (value == null) {
      return true
    }

    val duplicates = value
      .map { it.value.trim() }
      .groupingBy { it }
      .eachCount()
      .filterValues { it > 1 }
      .keys

    return duplicates.isEmpty()
  }
}
