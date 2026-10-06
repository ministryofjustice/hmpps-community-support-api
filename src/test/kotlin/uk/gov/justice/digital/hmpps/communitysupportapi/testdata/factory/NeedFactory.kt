package uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferenceDataNeed
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferenceDataOutcome
import java.util.UUID

class NeedFactory : TestEntityFactory<ReferenceDataNeed>() {

  private var id: UUID = UUID.randomUUID()
  private var label: String = "Test Need"
  private var orderNumber: Int = 1
  private var referenceDataOutcomes: MutableList<ReferenceDataOutcome> = mutableListOf()

  fun withId(id: UUID) = apply { this.id = id }
  fun withLabel(label: String) = apply { this.label = label }
  fun withOrderNumber(orderNumber: Int) = apply { this.orderNumber = orderNumber }
  fun withOutcomes(referenceDataOutcomes: List<ReferenceDataOutcome>) = apply { this.referenceDataOutcomes = referenceDataOutcomes.toMutableList() }

  override fun create(): ReferenceDataNeed = ReferenceDataNeed(
    id = id,
    label = label,
    orderNumber = orderNumber,
  ).apply {
    this.referenceDataOutcomes.addAll(referenceDataOutcomes)
  }
}
