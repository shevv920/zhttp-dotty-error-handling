package backend.resources

import backend.Config.AppConfig
import slick.basic.BasicBackend
import slick.jdbc.PostgresProfile
import zio.{ Has, ZIO, ZLayer }

object DatabaseProvider {
  type DatabaseProvider = Has[DatabaseProvider.Service]

  trait Service {
    def db: ZIO[Any, Throwable, BasicBackend#DatabaseDef]
  }

  val live: ZLayer[Has[AppConfig], Nothing, DatabaseProvider] =
    ZLayer.fromService((appConfig: AppConfig) =>
      new Service {
        override val db: ZIO[Any, Throwable, BasicBackend#DatabaseDef] =
          ZIO.effect(PostgresProfile.api.Database.forURL(appConfig.dbConnectionString))
      },
    )
}
