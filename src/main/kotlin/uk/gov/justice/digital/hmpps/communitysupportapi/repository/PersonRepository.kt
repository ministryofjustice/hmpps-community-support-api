package uk.gov.justice.digital.hmpps.communitysupportapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.Person
import java.util.UUID

interface PersonRepository : JpaRepository<Person, UUID> {
  fun findByIdentifier(identifier: String): Person?
}
