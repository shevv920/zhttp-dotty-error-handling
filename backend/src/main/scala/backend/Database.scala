package backend

import kuzminki.api.{ DbConfig, Kuzminki }
import zio.{ Schedule, ZIO }
import zio.config._
import zio.config.magnolia.Descriptor.descriptor

final case class DBConfig(database: String, user: String, password: String)

object Database {
  def dbConfig(configDatabase: DBConfig) = {
    DbConfig
      .forDb(configDatabase.database)
      .withUser(configDatabase.user)
      .withPassword(configDatabase.password)
      .getConfig
  }

  private val dbConfig = descriptor[DBConfig]

  val live = ZConfig
    .fromSystemEnv(dbConfig)
    .orElse(ZConfig.fromMap(Map("database" -> "postgres", "user" -> "postgres", "password" -> "dbpassword"), dbConfig))
    .tapError(e => ZIO.log(e.getMessage))
    .flatMap(c => Kuzminki.layer(dbConfig(c.get)))
    .tap(_ => ZIO.logInfo("Database layer created"))
    .tapError(error => ZIO.logError(error.getMessage))
    .retry(Schedule.fixed(zio.Duration.fromMillis(10000)))

}
