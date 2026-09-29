package uk.gov.justice.digital.hmpps.communitysupportapi.util

object PlaceholderUtils {
  private val placeholderRegex = Regex("""\{\{\s*([A-Za-z0-9_.-]+)\s*}}""")

  fun extractTokens(template: String): Set<String> = placeholderRegex.findAll(template)
    .map { it.groupValues[1] }
    .toSet()

  fun extractTokens(templates: Iterable<String?>): Set<String> = templates.asSequence()
    .filterNotNull()
    .flatMap { extractTokens(it) }
    .toSet()

  fun render(template: String?, vararg placeholders: Placeholders): String? {
    if (template.isNullOrBlank() || placeholders.isEmpty()) return template

    val tokens = extractTokens(template)
    if (tokens.isEmpty()) return template

    val values = buildPlaceholderValues(tokens, *placeholders)
    if (values.isEmpty()) return template

    return replace(template, values)
  }

  fun buildPlaceholderValues(
    tokens: Set<String>,
    vararg placeholders: Placeholders,
  ): Map<String, String> {
    if (tokens.isEmpty() || placeholders.isEmpty()) return emptyMap()
    return placeholders.fold(emptyMap()) { result, source ->
      result + source.resolve(tokens)
    }
  }

  private fun replace(template: String, values: Map<String, String>): String = placeholderRegex.replace(template) { match ->
    values[match.groupValues[1]] ?: match.value
  }
}
