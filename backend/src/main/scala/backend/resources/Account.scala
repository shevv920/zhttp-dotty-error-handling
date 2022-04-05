package backend.resources

import kuzminki.api._
import kuzminki.sorting.Sorting
import zio.ZIO
import zio.json._
import zhttp.http._
import pdi.jwt.{ Jwt, JwtAlgorithm, JwtClaim }

import java.time.Clock
import java.util.UUID
import scala.util.Try

final case class Account(id: UUID, username: String, passwordHash: String)
object Account {
  implicit val codec = DeriveJsonCodec.gen[Account]
}

final case class AccountPublic(id: UUID, username: String)
object AccountPublic {
  implicit val codec = DeriveJsonCodec.gen[AccountPublic]
}

final case class LoginRequest(username: String, password: String)
object LoginRequest {
  implicit val codec = DeriveJsonCodec.gen[LoginRequest]
}

final case class Token(token: String)
object Token {
  implicit val codec = DeriveJsonCodec.gen[Token]
}

class Accounts extends Model("accounts") {
  val id           = column[UUID]("id")
  val username     = column[String]("username")
  val passwordHash = column[String]("passwordHash")
  val item         = read[Account](id, username, passwordHash)
  val itemPublic   = read[AccountPublic](id, username)

  def all    = (id, username, passwordHash)
  def public = (id, username)
}

object Accounts extends Resource[Accounts] {
  import backend.Implicits._
  import backend.resources.Resource._

  override val defaultOrder: Accounts => Seq[Sorting] = _.username.asc
  override val items: Accounts                        = Model.get[Accounts]

  val secretKey             = "secret"
  implicit val clock: Clock = Clock.systemUTC
  def jwtEncode(username: String): String = {
    val json    = s"""{"username": "$username"}"""
    val claim   = JwtClaim { json }.issuedNow.expiresIn(300)
    val encoded = Jwt.encode(claim, secretKey, JwtAlgorithm.HS512)
    Token(encoded).toJson
  }

  def jwtDecode(token: String): Try[JwtClaim] = {
    Jwt.decode(token, secretKey, Seq(JwtAlgorithm.HS512))
  }

  private def get(limit: => Limit = Limit(10), page: => Page = Page(1)) = {
    sql
      .select(items)
      .colsType(_.itemPublic)
      .all
      .orderBy(defaultOrder)
      .offset((page - 1) * limit)
      .limit(limit)
      .run
  }

  val priv: Http[Kuzminki, Throwable, Request, Response] = Http.collectZIO { case req @ Method.GET -> !! =>
    (for {
      limit <- req.qs.int("limit", defaultLimit)
      page  <- req.qs.int("page", defaultPage)
      l     <- Limit.make(limit).mapError(QueryParamParseError(_)).toZIO
      p     <- Page.make(page).mapError(QueryParamParseError(_)).toZIO
      res   <- get(l, p)
    } yield Response.json(res.toJson)).catchSome {
      case QueryParamParseError(msg) =>
        ZIO.attempt(Response.text(msg).setStatus(Status.BadRequest))
      case e =>
        ZIO.attempt(Response.text(e.getMessage).setStatus(Status.BadRequest))
    }
  }

  val public = Http.collectZIO[Request] { case req @ Method.POST -> !! / "login" =>
    for {
      body <- req.bodyAsString
      _    <- ZIO.fromEither(body.fromJson[LoginRequest]).mapError(a => new Throwable(a))
    } yield Response.json(jwtEncode(body))
  }
}
