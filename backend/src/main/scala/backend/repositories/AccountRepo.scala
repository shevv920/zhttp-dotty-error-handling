package backend.repositories

import common.Protocol.{ AccountPublic, Password, SignupRequest }
import io.getquill.jdbczio.Quill
import zio.{ ZIO, ZLayer }

import java.sql.SQLException
import java.util.UUID
import javax.sql.DataSource
import io.getquill.{ EntityQuery, Quoted }
import zio.ULayer

final case class Account(id: UUID, username: String, password: String)

trait AccountRepo extends Resource {
  def getById(id: UUID): ZIO[Any, SQLException, Option[AccountPublic]]
  def getByUsernameAndPassword(userName: String, password: String): ZIO[Any, SQLException, Option[AccountPublic]]
  def usernameExists(userName: String): ZIO[Any, SQLException, Boolean]
  def insertSignup(signupRequest: SignupRequest): ZIO[Any, SQLException, AccountPublic]
}

object AccountRepo {
  def getByUsernameAndPassword(
      userName: String,
      password: String,
  ): ZIO[AccountRepo with AccountRepo, SQLException, Option[AccountPublic]] =
    ZIO.serviceWithZIO[AccountRepo](_.getByUsernameAndPassword(userName, password))

  def getById(id: UUID): ZIO[AccountRepo, SQLException, Option[AccountPublic]] =
    ZIO.serviceWithZIO[AccountRepo](_.getById(id))

  def usernameExists(userName: String): ZIO[AccountRepo, SQLException, Boolean] =
    ZIO.serviceWithZIO[AccountRepo](_.usernameExists(userName))

  def insertSignup(signupRequest: SignupRequest): ZIO[AccountRepo, SQLException, AccountPublic] =
    ZIO.serviceWithZIO[AccountRepo](_.insertSignup(signupRequest))

  val live =
    ZLayer
      .fromFunction(AccountRepoLive.apply _)
}

final case class AccountRepoLive(quill: Quill.Postgres[io.getquill.SnakeCase]) extends AccountRepo {
  import io.getquill._
  import quill.*

  inline def accounts: Quoted[EntityQuery[Account]] =
    quote {
      querySchema[Account]("accounts")
    }

  override def getById(id: UUID): ZIO[Any, SQLException, Option[AccountPublic]] =
    run(accounts.filter(acc => acc.id == lift(id)).map(acc => AccountPublic(acc.id, acc.username)))
      .map(_.headOption)

  override def getByUsernameAndPassword(
      userName: String,
      password: String,
  ): ZIO[Any, SQLException, Option[AccountPublic]] = {
    inline def q =
      quote {
        accounts
          .filter(acc => acc.username == lift(userName) && acc.password == lift(password))
          .map(acc => AccountPublic(acc.id, acc.username))
      }

    run(q).map(_.headOption)
  }

  def usernameExists(userName: String) = {
    inline def q =
      quote {
        accounts.filter(acc => acc.username == lift(userName)).map(_.username).nonEmpty
      }

    run(q)
  }

  def insertSignup(signupRequest: SignupRequest) = {
    inline def q =
      quote {
        accounts
          .insert(_.username -> lift(signupRequest.username), _.password -> lift(signupRequest.password.value))
          .returning(acc => AccountPublic(acc.id, acc.username))
      }

    run(q)
  }
}
