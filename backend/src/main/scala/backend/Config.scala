package backend

import zio.config._, ConfigDescriptor._

object Config {
  final case class AppConfig(env: String, port: Int, dbConnectionString: String)

  private val configuration =
    (string("env").default("development")
      |@| int("port")
        .default(9000)
      |@| string("dbConnectionString").default(
        "jdbc:postgresql://postgres/postgres?user=postgres&password=dbpassword",
      ))(AppConfig.apply, AppConfig.unapply)

  val live = ZConfig.fromSystemEnv(configuration)

}
