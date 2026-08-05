package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlan
import java.time.OffsetDateTime
import java.util.UUID

class ActionPlanStatusDtoTest {

  private val actionPlan = ActionPlan(
    id = UUID.randomUUID(),
    referralId = UUID.randomUUID(),
    actionPlanTemplateId = UUID.randomUUID(),
    createdAt = OffsetDateTime.now(),
    updatedAt = OffsetDateTime.now(),
  )

  @Test
  fun `fromActionPlan maps submitted action plan status`() {
    val result = ActionPlanStatusDto.fromActionPlan(actionPlan, isSubmitted = true)

    result.actionPlanId shouldBe actionPlan.id
    result.status shouldBe ActionPlanStatus.submitted()
  }

  @Test
  fun `fromActionPlan maps not submitted action plan status`() {
    val result = ActionPlanStatusDto.fromActionPlan(actionPlan, isSubmitted = false)

    result.actionPlanId shouldBe actionPlan.id
    result.status shouldBe ActionPlanStatus.notSubmitted()
  }

  @Test
  fun `submitted status factory returns submitted true with teal tag`() {
    val result = ActionPlanStatus.submitted()

    result.submitted shouldBe true
    result.statusText shouldBe "Submitted"
    result.tag shouldBe "govuk-tag--teal"
  }

  @Test
  fun `notSubmitted status factory returns submitted false with blue tag`() {
    val result = ActionPlanStatus.notSubmitted()

    result.submitted shouldBe false
    result.statusText shouldBe "Not Submitted"
    result.tag shouldBe "govuk-tag--blue"
  }
}
