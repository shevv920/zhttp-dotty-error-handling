package backend

import zio.ZIO
import zio.config._
import zio.config.derivation.name
import zio.config.magnolia.Descriptor.descriptor

final case class AppConfig(
    @name("APP_HOST") host: String,
    @name("APP_PORT") port: Int,
    @name("PWD_SK") pwdSecretKey: String,
    @name("PWD_SALT") pwdSalt: String,
)

object Config {
  private val configDescriptor = descriptor[AppConfig]

  val live = ZConfig
    .fromSystemEnv(configDescriptor)
    .orElse(
      ZConfig.fromMap(
        Map(
          "APP_HOST" -> "localhost",
          "APP_PORT" -> "9000",
          "PWD_SK"   -> "PASSWORD_SECRET_KEY",
          "PWD_SALT" -> "SALT_SALT_SALT",
        ),
        configDescriptor,
      )
    )
    .tapError(err => ZIO.logInfo(s"$err"))
}
