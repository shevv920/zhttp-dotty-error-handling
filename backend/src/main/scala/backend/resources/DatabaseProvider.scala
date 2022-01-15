package backend.resources

import backend.Config.AppConfig
import slick.basic.BasicBackend
import slick.jdbc.PostgresProfile
import zio.{ Has, ZIO, ZLayer }

trait DatabaseProvider {
  def db: ZIO[Any, Throwable, BasicBackend#DatabaseDef]
}

object DatabaseProvider {
  def db: ZIO[Has[DatabaseProvider], Throwable, BasicBackend#DatabaseDef] = ZIO.serviceWith[DatabaseProvider](_.db)
}

case class DatabaseProviderLive(appConfig: AppConfig) extends DatabaseProvider {
  override val db: ZIO[Any, Throwable, BasicBackend#DatabaseDef] =
    ZIO.effect(PostgresProfile.api.Database.forURL(appConfig.dbConnectionString))
}

object DatabaseProviderLive {
  val layer: ZLayer[Has[AppConfig], Nothing, Has[DatabaseProvider]] =
    ZLayer.fromService(appConfig => DatabaseProviderLive(appConfig))
}

