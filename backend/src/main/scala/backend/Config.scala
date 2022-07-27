package backend

import zio.ZIO
import zio.config.*
import zio.config.derivation.name
import zio.ZLayer
import zio.config.magnolia.descriptor

final case class AppConfig(
    @name("APP_HOST") host: String,
    @name("APP_PORT") port: Int,
    @name("PWD_SK") pwdSecretKey: String,
    @name("PWD_SALT") pwdSalt: String,
)

object Config {
  private val configDescriptor = descriptor[AppConfig]

  val live: ZLayer[Any, ReadError[String], AppConfig] = ZConfig
    .fromSystemEnv(configDescriptor)
    .orElse(
      ZConfig.fromMap(
        Map(
          "host"         -> "localhost",
          "port"         -> "9000",
          "pwdSecretKey" -> "PASSWORD_SECRET_KEY",
          "pwdSalt"      -> "SALT_SALT_SALT",
        ),
        configDescriptor,
      )
    )
    .tapError(err => ZIO.logError(s"Config load error: $err"))
}
