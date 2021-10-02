package backend.resources

import backend.resources.DatabaseProvider.DatabaseProvider
import interop.SlickToZio
import zio.ZIO

object Account {
  import db.Tables._
  import slick.jdbc.PostgresProfile.api._
  val q = Accounts.filter(_.name === "somename").map(_.email)

  def findSomeName =
    for {
      db  <- ZIO.access[DatabaseProvider](_.get.db)
      res <- SlickToZio(q.result)(db).map(_.toList)
    } yield res
}
