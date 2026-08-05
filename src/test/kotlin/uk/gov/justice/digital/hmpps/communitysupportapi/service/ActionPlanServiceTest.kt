package uk.gov.justice.digital.hmpps.communitysupportapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlan
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanEvent
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanEventType
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanTemplate
import uk.gov.justice.digital.hmpps.communitysupportapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanEventRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.repository.ActionPlanTemplateRepository
import java.time.OffsetDateTime
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class ActionPlanServiceTest {

  @Mock
  private lateinit var actionPlanRepository: ActionPlanRepository

  @Mock
  private lateinit var actionPlanEventRepository: ActionPlanEventRepository

  @Mock
  private lateinit var actionPlanTemplateRepository: ActionPlanTemplateRepository

  @InjectMocks
  private lateinit var actionPlanService: ActionPlanService

  @Test
  fun `findOrCreateByReferralId should return existing action plan when present`() {
    val referralId = UUID.randomUUID()
    val existingActionPlan = ActionPlan(
      id = UUID.randomUUID(),
      referralId = referralId,
      actionPlanTemplateId = UUID.randomUUID(),
      createdAt = OffsetDateTime.now(),
      updatedAt = OffsetDateTime.now(),
    )
    whenever(actionPlanRepository.findByReferralId(referralId)).thenReturn(existingActionPlan)

    val result = actionPlanService.findOrCreateByReferralId(referralId)

    assertEquals(existingActionPlan, result)
    verify(actionPlanRepository).findByReferralId(referralId)
    verifyNoInteractions(actionPlanTemplateRepository, actionPlanEventRepository)
  }

  @Test
  fun `findOrCreateByReferralId should create action plan and created event when missing`() {
    val referralId = UUID.randomUUID()
    val template = ActionPlanTemplate(id = UUID.randomUUID())

    whenever(actionPlanRepository.findByReferralId(referralId)).thenReturn(null)
    whenever(actionPlanTemplateRepository.findFirstByOrderByIdAsc()).thenReturn(template)
    whenever(actionPlanRepository.save(any<ActionPlan>())).thenAnswer { it.arguments[0] as ActionPlan }
    whenever(actionPlanEventRepository.save(any<ActionPlanEvent>())).thenAnswer { it.arguments[0] as ActionPlanEvent }

    val result = actionPlanService.findOrCreateByReferralId(referralId)

    assertEquals(referralId, result.referralId)
    assertEquals(template.id, result.actionPlanTemplateId)

    val actionPlanEventCaptor = ArgumentCaptor.forClass(ActionPlanEvent::class.java)
    verify(actionPlanEventRepository).save(actionPlanEventCaptor.capture())
    assertEquals(result.id, actionPlanEventCaptor.value.actionPlanId)
    assertEquals(ActionPlanEventType.CREATED, actionPlanEventCaptor.value.eventType)
  }

  @Test
  fun `findOrCreateByReferralId should throw NotFoundException when no action plan template exists`() {
    val referralId = UUID.randomUUID()
    whenever(actionPlanRepository.findByReferralId(referralId)).thenReturn(null)
    whenever(actionPlanTemplateRepository.findFirstByOrderByIdAsc()).thenReturn(null)

    assertThrows<NotFoundException> {
      actionPlanService.findOrCreateByReferralId(referralId)
    }

    verify(actionPlanRepository).findByReferralId(referralId)
    verify(actionPlanTemplateRepository).findFirstByOrderByIdAsc()
    verifyNoInteractions(actionPlanEventRepository)
  }
}
