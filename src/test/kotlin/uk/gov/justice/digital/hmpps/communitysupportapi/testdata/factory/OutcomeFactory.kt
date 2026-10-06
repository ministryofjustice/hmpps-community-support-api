package uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.OutcomeSetting
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferenceDataNeed
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferenceDataOutcome
import java.util.UUID

class OutcomeFactory : TestEntityFactory<ReferenceDataOutcome>() {
  private var id: UUID = UUID.randomUUID()
  private var needId: UUID = UUID.randomUUID()
  private var text: String = "Test outcome"
  private var orderNumber: Int = 1
  private var setting: OutcomeSetting = OutcomeSetting.ALL

  fun withId(id: UUID) = apply { this.id = id }
  fun withNeedId(needId: UUID) = apply { this.needId = needId }
  fun withNeed(referenceDataNeed: ReferenceDataNeed) = apply { this.needId = referenceDataNeed.id }
  fun withText(text: String) = apply { this.text = text }
  fun withOrderNumber(orderNumber: Int) = apply { this.orderNumber = orderNumber }
  fun withSetting(setting: OutcomeSetting) = apply { this.setting = setting }

  override fun create(): ReferenceDataOutcome = ReferenceDataOutcome(
    id = id,
    needId = needId,
    text = text,
    orderNumber = orderNumber,
    setting = setting,
  )
}
