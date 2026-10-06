package uk.gov.justice.digital.hmpps.communitysupportapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

typealias AuthGroupID = String

@Entity
@Table(name = "reference_data_service_provider")
class ReferenceDataServiceProvider(
  @Id
  val id: UUID,

  @Column(name = "auth_group_id", nullable = false, unique = true)
  val authGroupId: AuthGroupID,

  @Column(nullable = false, unique = true)
  val name: String,
)
