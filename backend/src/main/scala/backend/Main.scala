package backend

import backend.Security.jwtDecode
import backend.repositories.{ AccountRepo, FruitRepoLive }
import backend.routes.Routes
import common.Protocol.{ RequestError, RequestParseError }
import io.getquill.context.ZioJdbc.DataSourceLayer
import zhttp.http.Middleware.interceptZIOPatch
import zhttp.http.*
import zhttp.http.middleware.HttpMiddleware
import zhttp.service.*
import zio.{ Clock, ExitCode, URIO, ZIO, ZIOAppDefault }

import java.net.InetAddress

object Main extends ZIOAppDefault {
  private val middlewares = myDebug ++ Middleware.cors()

  def myDebug: HttpMiddleware[Any, Nothing] =
    interceptZIOPatch(req => Clock.nanoTime.map(start => (req.method, req.url, start))) {
      case (response, (method, url, start)) =>
        for
          end <- Clock.nanoTime
          _ <- ZIO
                 .log(s"${response.status.code} $method ${url.encode} ${(end - start) / 1000000}ms")
        yield Patch.empty
    }

  def authMiddleware: HttpMiddleware[AppConfig, Throwable] =
    Middleware.bearerAuthZIO { token =>
      jwtDecode(token) *> ZIO.succeed(true)
    }

  private val authedApp = Routes.authed @@ authMiddleware
  private val pubApp    = Routes.public
  private val app       = (pubApp ++ authedApp) @@ middlewares

  val dataSourceLayer = DataSourceLayer.fromPrefix("myDatabaseConfig")

  override def run: URIO[Any, ExitCode] = {
    val server =
      for
        config <- ZIO.service[AppConfig]
        _      <- ZIO.logInfo(s"Starting server on ${config.host}:${config.port}")
        server <- Server
                    .start(InetAddress.getByName(config.host), config.port, app)
      yield server

    server
      .provide(Config.live, dataSourceLayer, FruitRepoLive.live, AccountRepo.live)
      .exitCode
  }
}
