package uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Need
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Outcome
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.OutcomeSetting
import java.util.UUID

class OutcomeFactory : TestEntityFactory<Outcome>() {
  private var id: UUID = UUID.randomUUID()
  private var needId: UUID = UUID.randomUUID()
  private var text: String = "Test outcome"
  private var orderNumber: Int = 1
  private var setting: OutcomeSetting = OutcomeSetting.ALL

  fun withId(id: UUID) = apply { this.id = id }
  fun withNeedId(needId: UUID) = apply { this.needId = needId }
  fun withNeed(need: Need) = apply { this.needId = need.id }
  fun withText(text: String) = apply { this.text = text }
  fun withOrderNumber(orderNumber: Int) = apply { this.orderNumber = orderNumber }
  fun withSetting(setting: OutcomeSetting) = apply { this.setting = setting }

  override fun create(): Outcome = Outcome(
    id = id,
    needId = needId,
    text = text,
    orderNumber = orderNumber,
    setting = setting,
  )
}
