package backend.resources

import backend.db.Account
import interop.SlickToZio._
import zio._


trait TokenRepo {
  def getAccountByTokenValue(value: String): ZIO[Any, Throwable, Option[Account]]
}

object TokenRepo {
  def getAccountByTokenValue(value: String): ZIO[Has[TokenRepo], Throwable, Option[Account]] =
    ZIO.serviceWith[TokenRepo](_.getAccountByTokenValue(value))
}

case class TokenRepoLive(databaseProvider: DatabaseProvider) extends TokenRepo {
  import backend.db.Tables._
  import backend.db.Tables.profile.api._

  val createIfNotExists = tokens.schema.createIfNotExists.toZIO(databaseProvider)

  override def getAccountByTokenValue(value: String): ZIO[Any, Throwable, Option[Account]] = {
    val query =
      for {
        _   <- tokens.filter(_.value === value)
        acc <- accounts
      } yield acc
    query.result.headOption.toZIO(databaseProvider)
  }
}

object TokenRepoLive {
  val layer: ZLayer[Has[DatabaseProvider], Nothing, Has[TokenRepo]] =
    ZLayer.fromService((dp: DatabaseProvider) => TokenRepoLive(dp))
}
