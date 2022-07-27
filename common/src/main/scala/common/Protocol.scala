package common

import zio.json._

import java.util.UUID

object Protocol {
  final case class Fruit(id: UUID, name: String)
  object Fruit:
    given JsonCodec[Fruit] = zio.json.DeriveJsonCodec.gen[Fruit]

  final case class Password(value: String)
  object Password:
    given JsonEncoder[Password] = JsonEncoder[String].contramap[Password](_.value)
    given JsonDecoder[Password] = JsonDecoder[String].map(Password(_))

  final case class SigninRequest(username: String, password: Password)
  object SigninRequest:
    given JsonCodec[SigninRequest] = DeriveJsonCodec.gen[SigninRequest]

  final case class SignupRequest(username: String, password: Password)
  object SignupRequest:
    given JsonCodec[SignupRequest] = DeriveJsonCodec.gen[SignupRequest]

  final case class CreateFruitRequest(name: String)
  object CreateFruitRequest:
    given JsonCodec[CreateFruitRequest] = DeriveJsonCodec.gen[CreateFruitRequest]

  final case class AccountPublic(id: UUID, username: String)
  object AccountPublic:
    given JsonCodec[AccountPublic] = DeriveJsonCodec.gen[AccountPublic]

  final case class Token(token: String)
  object Token:
    given JsonCodec[Token] = DeriveJsonCodec.gen[Token]

  sealed trait RequestFail    extends Throwable
  sealed trait RequestSuccess extends Product with Serializable

  final case class RequestError(message: String) extends RequestFail
  object RequestError:
    given JsonCodec[RequestError] = DeriveJsonCodec.gen[RequestError]

  final case class RequestParseError(message: String) extends RequestFail
  object RequestParseError:
    given JsonCodec[RequestParseError] = DeriveJsonCodec.gen[RequestParseError]

  final case class QueryParamParseError(message: String) extends RequestFail
  object QueryParamParseError:
    given JsonEncoder[QueryParamParseError] = DeriveJsonEncoder.gen[QueryParamParseError]

}
