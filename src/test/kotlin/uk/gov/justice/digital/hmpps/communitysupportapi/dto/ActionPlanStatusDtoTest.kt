package uk.gov.justice.digital.hmpps.communitysupportapi.dto

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanEventType
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ActionPlanFactory

class ActionPlanStatusDtoTest {
  @Nested
  @DisplayName("status")
  inner class Status {
    @Test
    fun `should have the Submitted status if it has a Submitted event`() {
      val actionPlan = ActionPlanFactory()
        .withSubmittedEvent()
        .create()

      val result = ActionPlanStatusDto.fromActionPlan(actionPlan)

      assert(actionPlan.events.map { it.eventType }.contains(ActionPlanEventType.SUBMITTED))
      result.actionPlanId shouldBe actionPlan.id
      result.status shouldBe ActionPlanStatus.submitted()
    }

    @Test
    fun `should have the notSubmitted status if there is no Submitted event`() {
      val actionPlan = ActionPlanFactory().create()

      val result = ActionPlanStatusDto.fromActionPlan(actionPlan)

      actionPlan.events.size shouldBe 0
      result.actionPlanId shouldBe actionPlan.id
      result.status shouldBe ActionPlanStatus.notSubmitted()
    }
  }

  @Nested
  @DisplayName("Displaying Statuses")
  inner class DisplayStatuses {
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
}
