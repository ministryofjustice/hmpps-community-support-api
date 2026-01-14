package uk.gov.justice.digital.hmpps.communitysupportapi.authorization

import org.springframework.security.access.AccessDeniedException
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component

@Component
class UserMapper {
  fun fromToken(authentication: JwtAuthenticationToken): String = authentication.token.getClaimAsString("user_name")
    ?: throw AccessDeniedException("no user name present in token")
}
