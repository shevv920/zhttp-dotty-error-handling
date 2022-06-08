package backend

import kuzminki.api.{ DbConfig, Kuzminki }
import zio.{ Schedule, ZIO }
import zio.config._
import zio.config.derivation.name
import zio.config.magnolia.Descriptor.descriptor

final case class DBConfig(
    @name("DATABASE_NAME") database: String,
    @name("DATABASE_USER") user: String,
    @name("DATABASE_HOST") host: String,
    @name("DATABASE_PASSWORD") password: String,
    @name("DATABASE_PORT") port: String,
)

object Database {
  def dbConfig(cfg: DBConfig) = {
    DbConfig
      .forDb(cfg.database)
      .withHost(cfg.host)
      .withUser(cfg.user)
      .withPassword(cfg.password)
      .withPort(cfg.port)
      .getConfig
  }

  private val dbConfig = descriptor[DBConfig]

  val live = ZConfig
    .fromSystemEnv(dbConfig)
    .orElse(
      ZConfig.fromMap(
        Map(
          "DATABASE_NAME"     -> "postgres",
          "DATABASE_USER"     -> "postgres",
          "DATABASE_PASSWORD" -> "dbpassword",
          "DATABASE_PORT"     -> "5432",
          "DATABASE_HOST"     -> "localhost",
        ),
        dbConfig,
      )
    )
    .tapError(e => ZIO.log(e.getMessage))
    .flatMap(c => Kuzminki.layer(dbConfig(c.get)))
    .tap(_ => ZIO.logInfo("Database layer created"))
    .tapError(error => ZIO.logError(error.getMessage))
    .retry(Schedule.fixed(zio.Duration.fromMillis(10000)))

}
