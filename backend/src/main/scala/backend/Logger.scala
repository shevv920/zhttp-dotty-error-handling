package backend

import zhttp.http.{ Http, HttpApp, Request, Response }
import zio.ZIO
import zio.logging._

object Log {

  val live =
    Logging.console(
      logLevel = LogLevel.Info,
      format = LogFormat.ColoredLogFormat(),
    ) >>> Logging.withRootLoggerName("BACKEND")
}

object Logger {
  def apply[R <: Logging, E](httpApp: HttpApp[R, E]): HttpApp[R, E] = Http.flatten {
    Http.fromEffectFunction[Request] { request =>
      for {
        _ <- log.info(s"${request.method} <-- ${request.url.path.toString}")
      } yield httpApp.mapM { response =>
        for {
          _ <- response match {
                 case response: Response.HttpResponse[R, E] =>
                   log.info(
                     s"${request.method} --> ${request.url.path.toString} ${response.status.toJHttpStatus.code()}",
                   )
                 case _ => ZIO.unit
               }
        } yield response
      }
    }
  }

  def <<<[R <: Logging, E](httpApp: HttpApp[R, E]): HttpApp[R, E] = apply(httpApp)
}
