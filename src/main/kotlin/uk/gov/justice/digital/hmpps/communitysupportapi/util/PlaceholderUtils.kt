package uk.gov.justice.digital.hmpps.communitysupportapi.util

import org.slf4j.LoggerFactory

/**
 * This utility object provides methods for extracting and rendering placeholders in templates.
 * This is a convention taken from the UI repository where we might use e.g.:
 * "What support does {{ firstName }} need to help them live independently?"
 */
object PlaceholderUtils {
  private val logger = LoggerFactory.getLogger(PlaceholderUtils::class.java)

  /**
   * Matches placeholders in the format {{ token }} where token can contain alphanumeric characters,
   * underscores, hyphens, and dots. The regex is case-sensitive and allows for optional whitespace
   * around the token.
   */
  private val placeholderRegex = Regex("""\{\{\s*([A-Za-z0-9_.-]+)\s*}}""")

  /**
   * Extracts all unique placeholder tokens from a collection of template strings.
   * @return A set of unique tokens found in the provided templates, e.g. "firstName", "lastName", "fullName".
   */
  fun extractTokens(templates: Iterable<String?>): Set<String> = templates.asSequence()
    .filterNotNull()
    .flatMap { extractTokens(it) }
    .toSet()

  /**
   * Replaces the placeholder (e.g. {{ firstName }}) in the template with the corresponding value from the provided Placeholders.
   * @param template The template string containing placeholders in the format {{ token }}.
   * @param placeholders Vararg of Placeholders instances that provide the values for the tokens.
   * @return The rendered string with placeholders replaced by their values, or the original template if no values are found.
   */
  fun replacePlaceholdersInTemplate(template: String?, vararg placeholders: Placeholders): String? {
    if (template.isNullOrBlank()) {
      return template
    }

    val tokens = extractTokens(template)

    if (tokens.isEmpty()) {
      logger.debug("No tokens found in template: {}", template)
      return template
    }

    val values = buildPlaceholderValues(tokens, *placeholders)

    if (values.isEmpty()) {
      logger.warn("No placeholder values found for tokens (but template and tokens both present): {}", tokens)
      return template
    }

    return placeholderRegex.replace(template) { match ->
      values[match.groupValues[1]] ?: match.value
    }
  }

  private fun extractTokens(template: String): Set<String> = placeholderRegex.findAll(template)
    .map { it.groupValues[1] }
    .toSet()

  private fun buildPlaceholderValues(
    tokens: Set<String>,
    vararg placeholders: Placeholders,
  ): Map<String, String> = placeholders.fold(emptyMap()) { result, source ->
    result + source.resolve(tokens)
  }
}
