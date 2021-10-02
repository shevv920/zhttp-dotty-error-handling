package backend

import backend.resources.{ Account, DatabaseProvider }
import common.Common.commonValue
import zhttp.http._
import zhttp.service.{ ChannelFactory, EventLoopGroup, _ }
import zhttp.service.server.ServerChannelFactory
import zio.console.putStrLn
import zio.{ App, ExitCode, URIO, ZIO }

object Main extends App {
  val app = Http.collectM[Request] {
    case Method.GET -> Root / "health" =>
      putStrLn("health check ok") *> ZIO.succeed(Response.text("ok"))
    case Method.GET -> Root =>
      ZIO.succeed(Response.text(commonValue.toString))
    case req @ Method.POST -> Root / "login" =>
      for {
        res <- Account.findSomeName
      } yield Response.text(res.toString)

  }
  private val env =
    DatabaseProvider.live ++ ServerChannelFactory.auto ++ ChannelFactory.auto ++ EventLoopGroup
      .auto()
  private val server =
    Server.port(9000) ++              // Setup port
      Server.paranoidLeakDetection ++ // Paranoid leak detection (affects performance)
      Server.app(CORS(app))           // Setup the Http app

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    server.make
      .use(_ => ZIO.never)
      .provideCustomLayer(env)
      .exitCode
}
