package uk.gov.justice.digital.hmpps.communitysupportapi.mapper

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.communitysupportapi.dto.UserDto
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.ReferralUser
import uk.gov.justice.digital.hmpps.communitysupportapi.entity.UserType
import uk.gov.justice.hmpps.kotlin.auth.AuthSource
import java.util.UUID

class UserMapperTest {

  @Test
  fun `toEntity should map UserDto to an Internal User entity`() {
    val id = UUID.randomUUID()

    val userDto = UserDto(
      id = id,
      hmppsAuthId = "hmppsAuthId",
      hmppsAuthUsername = "hmppsAuthUsername",
      authSource = AuthSource.AUTH.source,
      fullName = "John Smith",
      emailAddress = "johnsmith@email.com",

    )

    val userEntity = userDto.toEntity()

    userEntity.id shouldBe id
    userEntity.hmppsAuthId shouldBe "hmppsAuthId"
    userEntity.hmppsAuthUsername shouldBe "hmppsAuthUsername"
    userEntity.authSource shouldBe AuthSource.AUTH.source
    userEntity.fullName shouldBe "John Smith"
    userEntity.emailAddress shouldBe "johnsmith@email.com"
  }

  @Test
  fun `toEntity should map UserDto to an External User entity`() {
    val id = UUID.randomUUID()

    val userDto = UserDto(
      id = id,
      hmppsAuthId = "hmppsAuthId",
      hmppsAuthUsername = "hmppsAuthUsername",
      authSource = AuthSource.AUTH.source,
      fullName = "John Smith",
      emailAddress = "johnsmith@email.com",
    )

    val userEntity = userDto.toEntity()

    userEntity.id shouldBe id
    userEntity.hmppsAuthId shouldBe "hmppsAuthId"
    userEntity.hmppsAuthUsername shouldBe "hmppsAuthUsername"
    userEntity.fullName shouldBe "John Smith"
    userEntity.emailAddress shouldBe "johnsmith@email.com"
  }

  @Test
  fun `toDto should map User to UserDto (Internal User)`() {
    val id = UUID.randomUUID()

    val user = ReferralUser(
      id = id,
      hmppsAuthId = "hmppsAuthId",
      hmppsAuthUsername = "hmppsAuthUsername",
      authSource = AuthSource.AUTH.source,
      fullName = "John Smith",
      emailAddress = "johnsmith@email.com",
    )

    val userDto = user.toDto()

    userDto.id shouldBe id
    userDto.hmppsAuthId shouldBe "hmppsAuthId"
    userDto.hmppsAuthUsername shouldBe "hmppsAuthUsername"
    userDto.fullName shouldBe "John Smith"
    userDto.emailAddress shouldBe "johnsmith@email.com"
    userDto.userType shouldBe UserType.INTERNAL
  }

  @Test
  fun `toDto should map User to UserDto (External User)`() {
    val id = UUID.randomUUID()

    val user = ReferralUser(
      id = id,
      hmppsAuthId = "hmppsAuthId",
      hmppsAuthUsername = "hmppsAuthUsername",
      authSource = AuthSource.AUTH.source,
      fullName = "John Smith",
      emailAddress = "johnsmith@email.com",
    )

    val userDto = user.toDto()

    userDto.id shouldBe id
    userDto.hmppsAuthId shouldBe "hmppsAuthId"
    userDto.hmppsAuthUsername shouldBe "hmppsAuthUsername"
    userDto.fullName shouldBe "John Smith"
    userDto.emailAddress shouldBe "johnsmith@email.com"
    userDto.userType shouldBe UserType.INTERNAL
  }
}
