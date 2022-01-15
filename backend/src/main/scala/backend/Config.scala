package backend

import zio.config._, ConfigDescriptor._

object Config {
  final case class AppConfig(
      env: String,
      port: Int,
      dbConnectionString: String,
      twitchClientId: String,
  )

  private val configuration =
    (string("env").default("development")
      |@| int("port")
        .default(9000)
      |@| string("dbConnectionString").default(
        "jdbc:postgresql://postgres/postgres?user=postgres&password=dbpassword",
      )
      |@| string("twitchClientId")
        .default("8j5tv717b8hwfyfvmt0c88he6d1hhe"))(AppConfig.apply, AppConfig.unapply)

  val live = ZConfig.fromSystemEnv(configuration)

}
