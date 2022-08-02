package common

import zio.json.*

import java.util.UUID
import scala.deriving.Mirror

object Protocol {
  inline given [T: Mirror.Of]: JsonCodec[T] = DeriveJsonCodec.gen[T]
  final case class Fruit(id: UUID, name: String)
  final case class Password(value: String)
  final case class SigninRequest(username: String, password: Password)
  final case class SignupRequest(username: String, password: Password)
  final case class CreateFruitRequest(name: String)
  final case class AccountPublic(id: UUID, username: String)
  final case class Token(token: String)

  sealed trait RequestFail    extends Throwable
  sealed trait RequestSuccess extends Product with Serializable

  final case class RequestError(message: String)         extends RequestFail
  final case class RequestParseError(message: String)    extends RequestFail
  final case class QueryParamParseError(message: String) extends RequestFail
}
