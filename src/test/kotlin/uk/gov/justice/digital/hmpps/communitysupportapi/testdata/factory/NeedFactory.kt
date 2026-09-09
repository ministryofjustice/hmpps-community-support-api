package uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Need
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Outcome
import java.util.UUID

class NeedFactory : TestEntityFactory<Need>() {

  private var id: UUID = UUID.randomUUID()
  private var label: String = "Test Need"
  private var orderNumber: Int = 1
  private var outcomes: MutableList<Outcome> = mutableListOf()

  fun withId(id: UUID) = apply { this.id = id }
  fun withLabel(label: String) = apply { this.label = label }
  fun withOrderNumber(orderNumber: Int) = apply { this.orderNumber = orderNumber }
  fun withOutcomes(outcomes: List<Outcome>) = apply { this.outcomes = outcomes.toMutableList() }

  override fun create(): Need = Need(
    id = id,
    label = label,
    orderNumber = orderNumber,
  ).apply {
    this.outcomes.addAll(outcomes)
  }
}
