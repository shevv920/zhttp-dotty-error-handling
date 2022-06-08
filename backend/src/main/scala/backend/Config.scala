package backend

import zio.ZIO
import zio.config._
import zio.config.derivation.name
import zio.config.magnolia.Descriptor.descriptor

final case class AppConfig(
    @name("APP_HOST") host: String,
    @name("APP_PORT") port: Int,
)

object Config {

  private val configDescriptor = descriptor[AppConfig]

  val live = ZConfig
    .fromSystemEnv(configDescriptor)
    .orElse(ZConfig.fromMap(Map("APP_HOST" -> "localhost", "APP_PORT" -> "9000"), configDescriptor))
    .tapError(err => ZIO.logInfo(s"$err"))
}
