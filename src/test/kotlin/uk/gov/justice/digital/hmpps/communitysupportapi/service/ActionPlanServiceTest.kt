package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.ActionPlanTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.ReferralTestSupport
import java.time.OffsetDateTime
import java.util.UUID

// TODO: Move these to be actual integration tests (don't use moquito)
// Will will also need to create the data creation / tidy up helpers here
//
class ActionPlanServiceTest : IntegrationTestBase() {

  @Autowired
  private lateinit var actionPlanService: ActionPlanService

  @Autowired
  private lateinit var referralHelper: ReferralTestSupport

  @Autowired
  private lateinit var actionPlanHelper: ActionPlanTestSupport

  @Test
  fun `findOrCreateByReferralId should return existing action plan when present`() {
    val user = referralHelper.ensureReferralUser()
    val referral = referralHelper.createReferral(submittedBy = user)
    val globalTemplate = actionPlanHelper.createActionPlanTemplate(id = UUID.randomUUID())
    val existingActionPlan = actionPlanHelper.createActionPlan(
      referralId = referral.id,
      templateId = globalTemplate.id,
      createdAt = OffsetDateTime.now(),
      updatedAt = OffsetDateTime.now(),
    )

    val result = actionPlanService.findOrCreateByReferralId(referral.id)

    assertEquals(existingActionPlan, result)
  }
}
