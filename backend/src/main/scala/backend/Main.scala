package backend

import backend.Security.jwtDecode
import zhttp.http.Middleware.interceptZIOPatch
import zhttp.http._
import zhttp.http.middleware.HttpMiddleware
import zhttp.service._
import zio.{ Clock, ExitCode, URIO, ZIO, ZIOAppDefault }

import java.net.InetAddress

object Main extends ZIOAppDefault {
  private val middlewares = myDebug ++ Middleware.cors()

  def myDebug: HttpMiddleware[Any, Nothing] =
    interceptZIOPatch(req => Clock.nanoTime.map(start => (req.method, req.url, start))) {
      case (response, (method, url, start)) =>
        for {
          end <- Clock.nanoTime
          _ <- ZIO
                 .log(s"${response.status.code} $method ${url.encode} ${(end - start) / 1000000}ms")
        } yield Patch.empty
    }

  def authMiddleware: HttpMiddleware[Any, Throwable] = Middleware.bearerAuthZIO { token =>
    ZIO.fromTry(jwtDecode(token)) *> ZIO.succeed(true)
  }

  private val authedApp = Routes.authed @@ authMiddleware
  private val pubApp    = Routes.public
  private val app       = (pubApp ++ authedApp) @@ middlewares

  private val env = Database.live

  override def run: URIO[Any, ExitCode] =
    Server.start(InetAddress.getByName("localhost"), 9000, app).provideLayer(env).exitCode
}
