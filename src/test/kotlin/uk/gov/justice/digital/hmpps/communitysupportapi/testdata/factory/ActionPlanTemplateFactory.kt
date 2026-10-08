package uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferenceDataActionPlanTemplate
import java.util.UUID

/**
 * Factory for creating ActionPlanTemplate test entities with sensible defaults.
 * Use the builder pattern to customize individual properties.
 *
 * Example usage:
 * ```
 * // Create with defaults
 * val template = ActionPlanTemplateFactory().create()
 *
 * // Create with custom values
 * val template = ActionPlanTemplateFactory()
 *     .withActiveGlobal(true)
 *     .create()
 *
 * // Create a global template
 * val template = ActionPlanTemplateFactory.aGlobalTemplate()
 * ```
 */
class ActionPlanTemplateFactory : TestEntityFactory<ReferenceDataActionPlanTemplate>() {

  private var id: UUID = UUID.randomUUID()
  private var activeGlobal: Boolean = false

  fun withId(id: UUID) = apply { this.id = id }
  fun withActiveGlobal(activeGlobal: Boolean) = apply { this.activeGlobal = activeGlobal }

  override fun create(): ReferenceDataActionPlanTemplate = ReferenceDataActionPlanTemplate(
    id = id,
    activeGlobal = activeGlobal,
  )

  companion object {
    /**
     * Creates a global action plan template.
     */
    fun aGlobalTemplate(): ReferenceDataActionPlanTemplate = ActionPlanTemplateFactory()
      .withActiveGlobal(true)
      .create()

    /**
     * Creates a non-global action plan template.
     */
    fun aTemplate(): ReferenceDataActionPlanTemplate = ActionPlanTemplateFactory()
      .withActiveGlobal(false)
      .create()
  }
}
