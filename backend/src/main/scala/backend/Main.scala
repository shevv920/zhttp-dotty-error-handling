package backend

import backend.resources.Accounts.jwtDecode
import zhttp.http.Middleware.interceptZIOPatch
import zhttp.http._
import zhttp.http.middleware.HttpMiddleware
import zhttp.service._
import zio.{ Clock, ExitCode, URIO, ZIO, ZIOAppDefault }

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
    for {
      username <- ZIO.fromTry(jwtDecode(token))
      _        <- ZIO.log(token)
      _        <- ZIO.log(username.toString)
      //      acc      <- sql.select(items).colsType(_.itemPublic).where(acc => acc.username === username.content).runHeadOpt
    } yield true
  }

  private val privApp = Routes.priv @@ authMiddleware
  private val pubApp  = Routes.public
  private val app     = (pubApp ++ privApp) @@ middlewares

  private val env = Database.layer

  override def run: URIO[Any, ExitCode] =
    Server.start(9000, app).provideLayer(env).exitCode
}
