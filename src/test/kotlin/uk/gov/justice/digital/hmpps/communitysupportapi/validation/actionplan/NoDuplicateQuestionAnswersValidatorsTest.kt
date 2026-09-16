package uk.gov.justice.digital.hmpps.communitysupportapi.validation.actionplan

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionDeliveryDetailsQuestionAnswer
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.SessionDeliveryDetailsQuestionAnswers
import java.util.UUID

class NoDuplicateQuestionAnswersValidatorsTest {

  private val noDuplicateQuestionIdsValidator = NoDuplicateQuestionIdsValidator()
  private val noDuplicateAnswerValuesValidator = NoDuplicateAnswerValuesValidator()

  @Test
  fun `question ids validator accepts unique ids`() {
    val requestAnswers = listOf(
      SessionDeliveryDetailsQuestionAnswers(
        questionId = UUID.randomUUID(),
        incomingAnswerDetails = listOf(SessionDeliveryDetailsQuestionAnswer(value = "BY_PHONE")),
      ),
      SessionDeliveryDetailsQuestionAnswers(
        questionId = UUID.randomUUID(),
        incomingAnswerDetails = listOf(SessionDeliveryDetailsQuestionAnswer(value = "BY_PHONE")),
      ),
    )

    assertThat(noDuplicateQuestionIdsValidator.isValid(requestAnswers, mock())).isTrue()
  }

  @Test
  fun `question ids validator rejects duplicate question ids`() {
    val duplicateQuestionId = UUID.randomUUID()
    val requestAnswers = listOf(
      SessionDeliveryDetailsQuestionAnswers(
        questionId = duplicateQuestionId,
        incomingAnswerDetails = listOf(SessionDeliveryDetailsQuestionAnswer(value = "BY_PHONE")),
      ),
      SessionDeliveryDetailsQuestionAnswers(
        questionId = duplicateQuestionId,
        incomingAnswerDetails = listOf(SessionDeliveryDetailsQuestionAnswer(value = "ONE_TO_ONE")),
      ),
    )

    assertThat(noDuplicateQuestionIdsValidator.isValid(requestAnswers, mock())).isFalse()
  }

  @Test
  fun `answer values validator accepts unique values`() {
    val answers = listOf(
      SessionDeliveryDetailsQuestionAnswer(value = "BY_PHONE"),
      SessionDeliveryDetailsQuestionAnswer(value = "BY_VIDEO"),
    )

    assertThat(noDuplicateAnswerValuesValidator.isValid(answers, mock())).isTrue()
  }

  @Test
  fun `answer values validator rejects duplicate values after trim`() {
    val answers = listOf(
      SessionDeliveryDetailsQuestionAnswer(value = " BY_PHONE "),
      SessionDeliveryDetailsQuestionAnswer(value = "BY_PHONE"),
    )

    assertThat(noDuplicateAnswerValuesValidator.isValid(answers, mock())).isFalse()
  }

  @Test
  fun `answer values validator accepts null list`() {
    assertThat(noDuplicateAnswerValuesValidator.isValid(null, mock())).isTrue()
  }

  @Test
  fun `question ids validator accepts null list`() {
    assertThat(noDuplicateQuestionIdsValidator.isValid(null, mock())).isTrue()
  }
}
