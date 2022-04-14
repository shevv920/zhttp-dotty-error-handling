package common

import zio.json._

import java.util.UUID

object Protocol {
  final case class Password(value: String) extends AnyVal
  implicit val passwordCodec = DeriveJsonCodec.gen[Password]

  final case class SigninRequest(username: String, password: Password)
  final case class SignupRequest(username: String, password: String)
  final case class CreateFruitRequest(name: String)
  final case class AccountPublic(id: UUID, username: String)
  object AccountPublic {
    implicit val codec = DeriveJsonCodec.gen[AccountPublic]
  }
  final case class Token(token: String)
  object Token {
    implicit val codec = DeriveJsonCodec.gen[Token]
  }
  object SignupRequest {
    implicit val codec = DeriveJsonCodec.gen[SignupRequest]
  }
  final case class LoginRequest(username: String, password: String)
  object LoginRequest {
    implicit val codec = DeriveJsonCodec.gen[LoginRequest]
  }

  final case class RequestError(message: String) extends Throwable
  object RequestError {
    implicit val codec = DeriveJsonCodec.gen[RequestError]
  }

  final case class RequestParseError(message: String) extends Throwable
  object RequestParseError {
    implicit val codec = DeriveJsonCodec.gen[RequestParseError]
  }
}
