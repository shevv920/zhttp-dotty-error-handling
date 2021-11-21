package backend.resources

import backend.Config.AppConfig
import interop.SlickToZio._
import zio.ZIO
import zio.config.getConfig

object Token {
  import backend.db.Tables._
  import backend.db.Tables.profile.api._

  val createIfNotExists = tokens.schema.createIfNotExists.toZIO

  def getAccount(token: String) = {
    val query =
      for {
        _   <- tokens.filter(_.value === token)
        acc <- accounts
      } yield acc
    for {
      config <- getConfig[AppConfig]
      _      <- ZIO.when(config.env == "development")(createIfNotExists)
      res    <- query.result.headOption.toZIO
    } yield res
  }
}
