package uk.gov.justice.digital.hmpps.communitysupportapi.validation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [NullOrNotBlankValidator::class])
annotation class NullOrNotBlank(
  val message: String = "Must be null or must not be blank",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = [],
)

class NullOrNotBlankValidator : ConstraintValidator<NullOrNotBlank, String?> {
  override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean = value == null || value.isNotBlank()
}
