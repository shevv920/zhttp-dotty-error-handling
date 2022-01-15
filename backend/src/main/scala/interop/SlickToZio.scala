package interop

import backend.resources.DatabaseProvider
import slick.dbio.DBIO
import zio.ZIO

object SlickToZio {

  def apply[T](action: DBIO[T])(databaseProvider: DatabaseProvider): ZIO[Any, Throwable, T] =
    for {
      db <- databaseProvider.db
      res    <- ZIO.fromFuture(implicit ec => db.run(action))
    } yield res

  implicit class SlickToZIO[T](val action: DBIO[T]) extends AnyVal {
    def toZIO(implicit databaseProvider: DatabaseProvider): ZIO[Any, Throwable, T] = apply(action)(databaseProvider)
  }
}
