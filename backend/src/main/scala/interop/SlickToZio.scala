package interop

import backend.Config.AppConfig
import backend.resources.DatabaseProvider.DatabaseProvider
import slick.dbio.DBIO
import zio.{ Has, ZIO }

object SlickToZio {

  def apply[T](action: DBIO[T]): ZIO[DatabaseProvider, Throwable, T] =
    for {
      dbTask <- ZIO.access[DatabaseProvider](_.get.db)
      db     <- dbTask
      res    <- ZIO.fromFuture(implicit ec => db.run(action))
    } yield res

  implicit class SlickToZIO[T](val action: DBIO[T]) extends AnyVal {
    def toZIO: ZIO[DatabaseProvider, Throwable, T] = apply(action)
  }
}
