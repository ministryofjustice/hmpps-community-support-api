package uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory

/**
 * Abstract base class for test entity factories.
 * Provides a consistent pattern for creating test entities with sensible defaults.
 *
 * @param T The entity type this factory creates
 */
abstract class TestEntityFactory<T> {

  /**
   * Creates an entity with default values.
   * Override individual properties using the specific factory's builder methods.
   */
  abstract fun create(): T

  /**
   * Creates multiple entities with default values.
   * @param count The number of entities to create
   * @param customizer Optional function to customize each entity based on its index
   */
  fun createMany(count: Int, customizer: ((Int) -> T)? = null): List<T> = (0 until count).map { index ->
    customizer?.invoke(index) ?: create()
  }
}
