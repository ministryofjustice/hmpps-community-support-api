package uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory

import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ActionPlanTemplate
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
class ActionPlanTemplateFactory : TestEntityFactory<ActionPlanTemplate>() {

  private var id: UUID = UUID.randomUUID()
  private var activeGlobal: Boolean = false

  fun withId(id: UUID) = apply { this.id = id }
  fun withActiveGlobal(activeGlobal: Boolean) = apply { this.activeGlobal = activeGlobal }

  override fun create(): ActionPlanTemplate = ActionPlanTemplate(
    id = id,
    activeGlobal = activeGlobal,
  )

  companion object {
    /**
     * Creates a global action plan template.
     */
    fun aGlobalTemplate(): ActionPlanTemplate = ActionPlanTemplateFactory()
      .withActiveGlobal(true)
      .create()

    /**
     * Creates a non-global action plan template.
     */
    fun aTemplate(): ActionPlanTemplate = ActionPlanTemplateFactory()
      .withActiveGlobal(false)
      .create()
  }
}
