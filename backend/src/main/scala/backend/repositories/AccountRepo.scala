package backend.repositories

import common.Protocol.{ AccountPublic, Password, SignupRequest }
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

  val live: ZLayer[DataSource, Throwable, AccountRepo] =
    ZLayer
      .fromFunction(AccountRepoLive.apply _)
      .tapError(_ => ZIO.logError("acc rep"))
}

final case class AccountRepoLive(dataSource: DataSource) extends AccountRepo {
  import QuillContext.*
  import io.getquill.*

  val env: ULayer[DataSource] = ZLayer.succeed(dataSource)

  inline def accounts: Quoted[EntityQuery[Account]] =
    quote {
      querySchema[Account]("accounts")
    }

  override def getById(id: UUID): ZIO[Any, SQLException, Option[AccountPublic]] =
    run(accounts.filter(acc => acc.id == lift(id)).map(acc => AccountPublic(acc.id, acc.username)))
      .map(_.headOption)
      .provide(env)

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

    run(q).map(_.headOption).provide(env)
  }

  def usernameExists(userName: String) = {
    inline def q =
      quote {
        accounts.filter(acc => acc.username == lift(userName)).map(_.username).nonEmpty
      }

    run(q).provide(env)
  }

  def insertSignup(signupRequest: SignupRequest) = {
    inline def q =
      quote {
        accounts
          .insert(_.username -> lift(signupRequest.username), _.password -> lift(signupRequest.password.value))
          .returning(acc => AccountPublic(acc.id, acc.username))
      }

    run(q).provide(env)
  }
}
