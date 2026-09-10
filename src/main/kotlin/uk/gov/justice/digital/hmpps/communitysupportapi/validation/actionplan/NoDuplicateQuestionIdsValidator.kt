package uk.gov.justice.digital.hmpps.communitysupportapi.validation.actionplan

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionDeliveryDetailsQuestionAnswers
import java.util.UUID
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [NoDuplicateQuestionIdsValidator::class])
annotation class NoDuplicateQuestionIds(
  val message: String = "Duplicate question IDs provided in question answer details",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = [],
)

class NoDuplicateQuestionIdsValidator : ConstraintValidator<NoDuplicateQuestionIds, List<SessionDeliveryDetailsQuestionAnswers>> {
  override fun isValid(value: List<SessionDeliveryDetailsQuestionAnswers>?, context: ConstraintValidatorContext): Boolean {
    if (value == null) {
      return true
    }

    val questionIds = HashSet<UUID>()
    questionIds.addAll(value.map { it.questionId })

    return value.size == questionIds.size
  }
}
