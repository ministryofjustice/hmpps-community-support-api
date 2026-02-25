package uk.gov.justice.digital.hmpps.communitysupportapi.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.FileNotFoundException

data class Record(
  val id: Int,
  val name: String,
  val address: String,
)

class CsvFileHelperTest {
  companion object {
    private val MAPPER: (org.apache.commons.csv.CSVRecord) -> Record = { row ->
      Record(
        id = row.get("id")?.toIntOrNull() ?: 0,
        name = row.get("name")?.trim() ?: "",
        address = row.get("address")?.trim() ?: "",
      )
    }
  }

  @Test
  fun `should read valid CSV from classpath and map correctly`() {
    val records = CsvFileHelper.readFromClasspath(
      resourcePath = "test-data/test-records.csv",
      mapper = MAPPER,
    )

    assertEquals(5, records.size)
    assertEquals(1, records[0].id)
    assertEquals("name_1", records[0].name)
    assertEquals("address_1", records[0].address)
    assertEquals(4, records[3].id)
    assertEquals("name_4", records[3].name)
    assertEquals("address_4", records[3].address)
  }

  @Test
  fun `should fail when resource does not exist`() {
    assertThrows<FileNotFoundException> {
      CsvFileHelper.readFromClasspath(
        resourcePath = "non-existing.csv",
        mapper = MAPPER,
      )
    }
  }
}
