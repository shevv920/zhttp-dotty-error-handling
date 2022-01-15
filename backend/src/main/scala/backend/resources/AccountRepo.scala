package backend.resources

import backend.db
import interop.SlickToZio._
import zio.{Has, ZIO, ZLayer}
import zio.json._

trait AccountRepo {
  def byId(id: Int): ZIO[Any, Throwable, Option[backend.db.Account]]
}

object AccountRepo {
  def byId(id: Int): ZIO[Has[AccountRepo], Throwable, Option[backend.db.Account]] =
    ZIO.serviceWith(_.byId(id))
}

case class AccountRepoLive(databaseProvider: DatabaseProvider) extends AccountRepo {
  import backend.db.Tables._
  import backend.db.Tables.profile.api._
  private implicit val dp = databaseProvider
  val createIfNotExists = accounts.schema.createIfNotExists.toZIO(databaseProvider)

  override def byId(id: Int): ZIO[Any, Throwable, Option[backend.db.Account]] = {
    val query = accounts.filter(_.id === id)
    query.result.headOption.toZIO
  }
}

object AccountRepoLive {
  val layer = ZLayer.fromService(dp => AccountRepoLive(dp))
}
