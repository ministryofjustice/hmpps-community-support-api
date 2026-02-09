package uk.gov.justice.digital.hmpps.communitysupportapi.testdata.factory

import com.sun.tools.javac.tree.TreeInfo.fullName
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import java.time.LocalDateTime
import java.util.UUID

class ReferralUserFactory : TestEntityFactory<ReferralUser>() {

  private var id: UUID = UUID.randomUUID()
  private var hmppsAuthId: String = UUID.randomUUID().toString()
  private var hmppsAuthUsername: String = "test-user"
  private var authSource: String = "auth"
  private var fullName: String = "Test User"
  private var emailAddress: String = "testuser@email.com"
  private var lastSyncedAt: LocalDateTime? = null

  fun withId(id: UUID) = apply { this.id = id }
  fun withHmppsAuthId(hmppsAuthId: String) = apply { this.hmppsAuthId = hmppsAuthId }
  fun withHmppsAuthUsername(hmppsAuthUsername: String) = apply { this.hmppsAuthUsername = hmppsAuthUsername }
  fun withAuthSource(authSource: String) = apply { this.authSource = authSource }
  fun withFullName(fullName: String) = apply { this.fullName = fullName }
  fun withLastSyncedAt(lastSyncedAt: LocalDateTime?) = apply { this.lastSyncedAt = lastSyncedAt }

  override fun create(): ReferralUser = ReferralUser(
    id = id,
    hmppsAuthId = hmppsAuthId,
    hmppsAuthUsername = hmppsAuthUsername,
    authSource = authSource,
    fullName = fullName,
    emailAddress = emailAddress,
    lastSyncedAt = lastSyncedAt,
  )

  companion object {
    fun aDefaultUser(): ReferralUser = ReferralUserFactory().create()

    fun anAuthUser(username: String = "test-user"): ReferralUser = ReferralUserFactory()
      .withHmppsAuthUsername(username)
      .withAuthSource("auth")
      .create()

    fun aDeliusUser(username: String = "delius-user"): ReferralUser = ReferralUserFactory()
      .withHmppsAuthUsername(username)
      .withAuthSource("delius")
      .create()
  }
}
