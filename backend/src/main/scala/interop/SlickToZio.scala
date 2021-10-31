package interop

import backend.resources.DatabaseProvider.DatabaseProvider
import slick.dbio.DBIO
import zio.ZIO

object SlickToZio {

  def apply[T](action: DBIO[T]): ZIO[DatabaseProvider, Throwable, T] =
    for {
      dbTask <- ZIO.access[DatabaseProvider](_.get.db)
      db     <- dbTask
      res    <- ZIO.fromFuture(implicit ec => db.run(action))
    } yield res
}
