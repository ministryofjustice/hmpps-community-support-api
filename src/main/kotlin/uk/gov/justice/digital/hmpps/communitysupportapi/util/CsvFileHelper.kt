package uk.gov.justice.digital.hmpps.communitysupportapi.util

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

object CsvFileHelper {
  fun <T : Any> readFromClasspath(
    resourcePath: String,
    mapper: (CSVRecord) -> T,
    format: CSVFormat = defaultFormat(),
  ): List<T> {
    val resource: Resource = ClassPathResource(resourcePath)
    return read(resource.inputStream, mapper, format)
  }

  fun <T : Any> read(
    inputStream: InputStream,
    mapper: (CSVRecord) -> T,
    format: CSVFormat = defaultFormat(),
  ): List<T> {
    val records = mutableListOf<T>()

    BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8)).use { reader ->
      CSVParser.parse(reader, format).use { parser ->
        for (record in parser) {
          try {
            records.add(mapper(record))
          } catch (e: Exception) {
            throw CsvParsingException(
              "Failed to map CSV row #${parser.currentLineNumber}: ${record.toMap()}",
              e,
            )
          }
        }
      }
    }
    return records
  }

  inline fun <reified T : Any> readToDataClassFromClasspath(
    resourcePath: String,
    noinline rowToObject: (CSVRecord) -> T = { record ->
      throw NotImplementedError("Provide your own mapper for ${T::class.simpleName}")
    },
  ): List<T> = readFromClasspath(resourcePath, rowToObject)

  fun defaultFormat(): CSVFormat = CSVFormat.RFC4180
    .builder()
    .setHeader()
    .setSkipHeaderRecord(true)
    .setTrim(true)
    .setIgnoreSurroundingSpaces(true)
    .setIgnoreEmptyLines(true)
    .setNullString("")
    .get()

  class CsvParsingException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
}
