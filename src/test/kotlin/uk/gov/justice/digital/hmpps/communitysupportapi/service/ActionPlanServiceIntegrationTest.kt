package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.ActionPlanTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.ReferralTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanEventRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanTemplateRepository
import java.time.OffsetDateTime

class ActionPlanServiceIntegrationTest :
  IntegrationTestBase(),
  AfterAllCallback {

  @Autowired
  private lateinit var actionPlanService: ActionPlanService

  @Autowired
  private lateinit var referralHelper: ReferralTestSupport

  @Autowired
  private lateinit var actionPlanHelper: ActionPlanTestSupport

  @Autowired
  private lateinit var actionPlanTemplateRepository: ActionPlanTemplateRepository

  @Autowired
  private lateinit var actionPlanEventRepository: ActionPlanEventRepository

  @Autowired
  private lateinit var actionPlanRepository: ActionPlanRepository

  fun afterAll() {
  }

  override fun afterAll(context: ExtensionContext) {
    testDataCleaner.cleanAllTables()
  }

  @Nested
  @DisplayName("findOrCreateByReferralId")
  inner class FindOrCreateByReferralId {
    val user = referralHelper.ensureReferralUser()
    val globalTemplate = actionPlanTemplateRepository.getGlobalActionPlanTemplate() ?: throw NotFoundException("Cannot find Global ActionPlan")

    @Test
    fun `should not create an additional ActionPlan when one already exists`() {
      // Given
      val referral = referralHelper.createReferral(submittedBy = user)
      val existingActionPlan = actionPlanHelper.createActionPlan(
        referralId = referral.id,
        templateId = globalTemplate.id,
        createdAt = OffsetDateTime.now(),
        updatedAt = OffsetDateTime.now(),
      )

      // When
      val result = actionPlanService.findOrCreateByReferralId(referral.id)

      // Then
      val allActionPlansForReferral = actionPlanRepository.findAllByReferralId(referral.id)
      assertEquals(existingActionPlan.id, result.id)
      assertEquals(allActionPlansForReferral.size, 1)
    }

    @Test
    fun `should create an ActionPlan when one does not exists`() {
      // Given
      val referral = referralHelper.createReferral(submittedBy = user)

      // When
      assertEquals(actionPlanRepository.findAllByReferralId(referral.id).size, 0)
      val result = actionPlanService.findOrCreateByReferralId(referral.id)

      // Then
      val allActionPlans = actionPlanRepository.findAllByReferralId(referral.id)
      assertEquals(result.referralId, referral.id)
      assertEquals(allActionPlans.size, 1)
    }
  }
}
