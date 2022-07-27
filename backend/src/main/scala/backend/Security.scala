package backend

import common.Protocol.{ Password, Token }
import io.github.nremond.{ toHex, PBKDF2 }
import pdi.jwt.{ Jwt, JwtAlgorithm, JwtClaim }
import zio.ZIO

import java.time.Clock
import zio.json._

object Security {
  implicit val clock: Clock = Clock.systemUTC

  def jwtEncode(username: String): ZIO[AppConfig, Nothing, String] =
    for
      sk      <- ZIO.serviceWith[AppConfig](_.pwdSecretKey)
      json    <- ZIO.succeed(s"""{"username": "$username"}""")
      claim   <- ZIO.succeed(JwtClaim { json }.issuedNow.expiresIn(300))
      encoded <- ZIO.succeed(Jwt.encode(claim, sk, JwtAlgorithm.HS512))
    yield Token(encoded).toJson

  def jwtDecode(token: String): ZIO[AppConfig, Throwable, JwtClaim] =
    for
      sk  <- ZIO.serviceWith[AppConfig](_.pwdSecretKey)
      res <- ZIO.fromTry(Jwt.decode(token, sk, Seq(JwtAlgorithm.HS512)))
    yield res

  def hashPassword(pwd: Password): ZIO[AppConfig, Nothing, String] =
    for salt <- ZIO.serviceWith[AppConfig](_.pwdSalt)
    yield toHex(PBKDF2(pwd.value.getBytes, salt.getBytes))

  def toHexString(value: String): ZIO[AppConfig, Nothing, String] =
    for salt <- ZIO.serviceWith[AppConfig](_.pwdSalt)
    yield toHex(PBKDF2(value.getBytes, salt.getBytes))
}
