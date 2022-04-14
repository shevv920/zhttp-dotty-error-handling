package backend

import common.Protocol.Token
import io.github.nremond.{ toHex, PBKDF2 }
import pdi.jwt.{ Jwt, JwtAlgorithm, JwtClaim }

import java.time.Clock
import scala.util.Try
import zio.json._

object Security {
  private val secretKey     = "secret"
  private val salt          = "1234567890".getBytes
  implicit val clock: Clock = Clock.systemUTC
  def jwtEncode(username: String): String = {
    val json    = s"""{"username": "$username"}"""
    val claim   = JwtClaim { json }.issuedNow.expiresIn(300)
    val encoded = Jwt.encode(claim, secretKey, JwtAlgorithm.HS512)
    Token(encoded).toJson
  }

  def jwtDecode(token: String): Try[JwtClaim] =
    Jwt.decode(token, secretKey, Seq(JwtAlgorithm.HS512))

  implicit class HashString(value: String) {
    def toHexHash: String =
      toHex(PBKDF2(value.getBytes, salt))
  }
}
