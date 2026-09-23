package uk.gov.justice.digital.hmpps.communitysupportapi.datafetcher

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.ActionPlanTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.ReferralTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanTemplateRepository
import java.time.OffsetDateTime

class ActionPlanDataFetcherIntegrationTest : IntegrationTestBase() {
  @Autowired
  private lateinit var referralHelper: ReferralTestSupport

  @Autowired
  private lateinit var actionPlanHelper: ActionPlanTestSupport

  @Autowired
  private lateinit var actionPlanDataFetcher: ActionPlanDataFetcher

  @Autowired
  private lateinit var actionPlanRepository: uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanRepository

  @Autowired
  private lateinit var actionPlanTemplateRepository: ActionPlanTemplateRepository

  private val globalTemplate by lazy { actionPlanTemplateRepository.getGlobalActionPlanTemplate() }
  private val user by lazy { referralHelper.ensureReferralUser() }

  @Nested
  @DisplayName("Get Action Plan Data For Referral Tests")
  inner class GetActionPlanDataForReferralTests {
    @Test
    fun `should not create an additional ActionPlan when one already exists`() {
      // Given
      val referral = referralHelper.createReferral(submittedBy = user)
      val existingActionPlan = actionPlanHelper.createActionPlan(
        referralId = referral.id,
        templateId = globalTemplate!!.id,
        createdAt = OffsetDateTime.now(),
        updatedAt = OffsetDateTime.now(),
      )

      // When
      val result = actionPlanDataFetcher.getActionPlanDataForReferral(referral.referenceNumber!!)

      // Then
      val allActionPlansForReferral = actionPlanRepository.findAllByReferralId(referral.id)
      assertEquals(existingActionPlan.id, result.actionPlan.id)
      assertEquals(allActionPlansForReferral.size, 1)
    }

    @Test
    fun `should create a new ActionPlan when one does not already exist`() {
      // Given
      val referral = referralHelper.createReferral(submittedBy = user)

      // When
      val result = actionPlanDataFetcher.getActionPlanDataForReferral(referral.referenceNumber!!)

      // Then
      val allActionPlansForReferral = actionPlanRepository.findAllByReferralId(referral.id)
      assertEquals(result.actionPlan.id, result.actionPlan.id)
      assertEquals(allActionPlansForReferral.size, 1)
    }
  }
}
