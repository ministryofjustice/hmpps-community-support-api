package uk.gov.justice.digital.hmpps.communitysupportapi.controller

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod.GET
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ActionPlanNeedsResponse
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.ActionPlanSummaryDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanStepType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Referral
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.ActionPlanTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.communitysupportapi.integration.ReferralTestSupport
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanStepQuestionRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanStepRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.NeedRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ActionPlanStepFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory.ActionPlanStepQuestionFactory
import uk.gov.justice.digital.hmpps.communitysupportapi.util.ReferralReferenceTestUtil.randomReferralReference

class ActionPlanControllerIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var referralHelper: ReferralTestSupport

  @Autowired
  private lateinit var needRepository: NeedRepository

  @Autowired
  private lateinit var actionPlanHelper: ActionPlanTestSupport

  @Autowired
  private lateinit var actionPlanStepRepository: ActionPlanStepRepository

  @Autowired
  private lateinit var actionPlanStepQuestionRepository: ActionPlanStepQuestionRepository

  @Nested
  @DisplayName("GET /bff/referral/{referralReference}/action-plan")
  inner class GetActionPlanSummaryEndpoint {
    @Test
    fun `should return OK with action plan summary for a valid referral reference`() {
      val user = referralHelper.ensureReferralUser()
      val person = referralHelper.createPerson(firstName = "Adam", lastName = "Smith")
      val referral = referralHelper.createReferral(person = person, referenceNumber = randomReferralReference(), submittedBy = user)
      val expectedNeeds = needRepository.findAllByOrderByOrderNumberAsc()

      webTestClient.get()
        .uri("/bff/referral/${referral.referenceNumber}/action-plan")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBody<ActionPlanSummaryDto>()
        .consumeWith { response ->
          val body = response.responseBody!!
          body.personDetails.fullName shouldBe "Adam Smith"
          body.needs.size shouldBe expectedNeeds.size
          body.needs.map { it.label } shouldBe expectedNeeds.map { it.label }
          body.needs.map { it.id } shouldBe expectedNeeds.map { it.id }
        }
    }
  }

  @Nested
  @DisplayName("GET /bff/referral/{referralReference}/action-plan/needs")
  inner class GetActionPlanNeedsEndpoint {
    @Test
    fun `should return unauthorized if no token`() {
      assertUnauthorized(GET, "/bff/referral/AB1234CD/action-plan/needs")
    }

    @Test
    fun `should return forbidden if no role`() {
      assertForbiddenNoRole(GET, "/bff/referral/AB1234CD/action-plan/needs")
    }

    @Test
    fun `should return forbidden if wrong role`() {
      assertForbiddenWrongRole(GET, "/bff/referral/AB1234CD/action-plan/needs")
    }

    @Test
    fun `should return action plan needs grouped by need and ordered by question order`() {
      val referral = createReferral("Jo", "Bloggs")
      val actionPlanTemplate = actionPlanHelper.createActionPlanTemplate()
      actionPlanHelper.createActionPlan(referralId = referral.id, templateId = actionPlanTemplate.id)

      val orderedNeeds = needRepository.findAllByOrderByOrderNumberAsc().take(2)
      val firstNeed = orderedNeeds[0]
      val secondNeed = orderedNeeds[1]

      val needStep = actionPlanStepRepository.save(
        ActionPlanStepFactory()
          .withActionPlanTemplateId(actionPlanTemplate.id)
          .withOrderNumber(1)
          .withName("Needs")
          .withStepType(ActionPlanStepType.NEED)
          .create(),
      )

      actionPlanStepQuestionRepository.save(
        ActionPlanStepQuestionFactory()
          .withActionPlanStepId(needStep.id)
          .withOrderNumber(1)
          .withTitle("Question for second need")
          .withAnswerType("textarea")
          .withNeedId(secondNeed.id)
          .create(),
      )
      actionPlanStepQuestionRepository.save(
        ActionPlanStepQuestionFactory()
          .withActionPlanStepId(needStep.id)
          .withOrderNumber(2)
          .withTitle("First question for first need")
          .withAnswerType("textarea")
          .withNeedId(firstNeed.id)
          .create(),
      )
      actionPlanStepQuestionRepository.save(
        ActionPlanStepQuestionFactory()
          .withActionPlanStepId(needStep.id)
          .withOrderNumber(3)
          .withTitle("Second question for first need")
          .withAnswerType("textarea")
          .withNeedId(firstNeed.id)
          .create(),
      )
      actionPlanStepQuestionRepository.save(
        ActionPlanStepQuestionFactory()
          .withActionPlanStepId(needStep.id)
          .withOrderNumber(4)
          .withTitle("Question without need")
          .withNeedId(null)
          .create(),
      )

      webTestClient.get()
        .uri("/bff/referral/${referral.referenceNumber}/action-plan/needs")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus().isOk
        .expectBody<ActionPlanNeedsResponse>()
        .consumeWith { response ->
          val body = response.responseBody!!

          body.needs.map { it.id } shouldBe listOf(firstNeed.id, secondNeed.id)
          body.needs[0].label shouldBe "Accommodation"
          body.needs[0].questions.map { it.label } shouldBe listOf("First question for first need", "Second question for first need")
          body.needs[0].questions.map { it.answerType } shouldBe listOf("textarea", "textarea")

          body.needs[1].label shouldBe secondNeed.label
          body.needs[1].questions.map { it.label } shouldBe listOf("Question for second need")
          body.needs[1].questions.map { it.answerType } shouldBe listOf("textarea")
        }
    }

    @Test
    fun `should return not found for unknown referral reference`() {
      assertNotFound(GET, "/bff/referral/ZZ9999ZZ/action-plan/needs")
    }

    private fun createReferral(firstName: String, lastName: String): Referral {
      val user = referralHelper.ensureReferralUser()
      val person = referralHelper.createPerson(firstName = firstName, lastName = lastName)
      return referralHelper.createReferral(person = person, referenceNumber = randomReferralReference(), submittedBy = user)
    }
  }
}
