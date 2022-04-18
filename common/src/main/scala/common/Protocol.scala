package common

import zio.json._

import java.util.UUID

object Protocol {
  final case class Password(value: String) extends AnyVal

  final case class SigninRequest(username: String, password: String)
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
  object SigninRequest {
    implicit val codec = DeriveJsonCodec.gen[SigninRequest]
  }
  object SignupRequest {
    implicit val codec = DeriveJsonCodec.gen[SignupRequest]
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
