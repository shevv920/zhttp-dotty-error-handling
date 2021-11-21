package backend

import backend.resources.DatabaseProvider
import zhttp.http._
import zhttp.service.server.ServerChannelFactory
import zhttp.service._
import zio.{ App, ExitCode, URIO, ZIO }

object Main extends App {
  private val appEnv = Config.live ++ (Config.live >>> DatabaseProvider.live) ++ Log.live
  private val env =
    ServerChannelFactory.auto ++
      ChannelFactory.auto ++
      EventLoopGroup.auto() ++
      appEnv

  private val server =
    Server.port(9000) ++              // Setup port
      Server.paranoidLeakDetection ++ // Paranoid leak detection (affects performance)
      Server.app(
        CORS(Logger <<< (Routes.public +++ Routes.publicM +++ (Auth <<< Routes.authed))),
      )

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    server.make
      .use(_ => ZIO.never)
      .provideCustomLayer(env)
      .exitCode
}
