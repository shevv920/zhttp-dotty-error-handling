package backend.resources

import backend.Security._
import common.Protocol._
import kuzminki.api._
import kuzminki.sorting.Sorting
import zhttp.http._
import zio.ZIO
import zio.json._

import java.util.UUID

final case class Account(id: UUID, username: String, password: String)

class Accounts extends Model("accounts") {
  val id         = column[UUID]("id")
  val username   = column[String]("username")
  val password   = column[String]("password")
  val item       = read[Account](id, username, password)
  val itemPublic = read[AccountPublic](id, username)
  val itemSignup = write[SignupRequest](username, password)

  def all    = (id, username, password)
  def public = (id, username)
}

object Accounts extends Resource[Accounts] {
  override val defaultOrder: Accounts => Seq[Sorting] = _.username.asc
  override val items: Accounts                        = Model.get[Accounts]

  val public = Http.collectZIO[Request] {
    case req @ Method.POST -> !! / "signin" =>
      for {
        body         <- req.bodyAsString
        loginRequest <- ZIO.fromEither(body.fromJson[LoginRequest]).mapError(RequestParseError.apply)
        accOpt <-
          sql
            .select(items)
            .colsType(_.itemPublic)
            .where(acc => Seq(acc.username === loginRequest.username, acc.password === loginRequest.password.toHexHash))
            .runHeadOpt
        acc <- ZIO.fromOption(accOpt).mapError(_ => RequestParseError("not found"))
      } yield Response.json(jwtEncode(acc.username))
    case req @ Method.POST -> !! / "signup" =>
      for {
        body          <- req.bodyAsString
        signupRequest <- ZIO.fromEither(body.fromJson[SignupRequest]).mapError(RequestParseError.apply)
        res <- sql
                 .insert(items)
                 .cols2(acc => (acc.username, acc.password))
                 .returningType(_.itemPublic)
                 .runHead((signupRequest.username, signupRequest.password.toHexHash))
      } yield Response.json(res.toJson)
  }
}
