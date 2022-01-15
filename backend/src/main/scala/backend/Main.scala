package backend


import backend.resources.{DatabaseProviderLive, TokenRepoLive}
import zhttp.http.Middleware
import zhttp.service._
import zio.{App, ExitCode, URIO}

object Main extends App {
  val middlewares = Middleware.debug ++ Middleware.cors()

  val app = (Routes.public ++ Routes.publicM @@ middlewares) ++ Routes.authed @@ (middlewares ++ Auth.tokenAuth)

  val env = zio.system.System.live >>> (Config.live >+> DatabaseProviderLive.layer >+> TokenRepoLive.layer)
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    Server.start(9000, app).provideCustomLayer(env).exitCode
}
