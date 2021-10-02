package backend.resources

import backend.resources.DatabaseProvider.DatabaseProvider
import interop.SlickToZio
import zio.ZIO

object Account {
  import db.Tables._
  import slick.jdbc.PostgresProfile.api._

  def findByEmail(email: String) =
    for {
      db  <- ZIO.access[DatabaseProvider](_.get.db)
      q    = Accounts.filter(_.email === email).map(r => r.name)
      res <- SlickToZio(q.result)(db).map(_.toList)
    } yield res.headOption
}
