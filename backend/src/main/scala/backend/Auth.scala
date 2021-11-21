package backend

import backend.Config.AppConfig
import backend.resources.DatabaseProvider.DatabaseProvider
import backend.resources.Token
import db._
import zhttp.http.{ Http, HttpApp, HttpError, Request }
import zio.ZIO
import zio.Has

object Auth {
  private val fail = Http.succeed(HttpError.BadRequest("No auth token provided").toResponse)

  def apply[R <: Has[AppConfig] with DatabaseProvider](
      app: Option[Account] => HttpApp[R, Throwable],
  ): HttpApp[R, Throwable] =
    Http.flatten {
      Http.fromEffectFunction[Request] { request =>
        val token = request.getHeader("token").map(_.value.toString)
        token match {
          case None        => ZIO.succeed(fail)
          case Some(token) => Token.getAccount(token).map(res => app(res))
        }
      }
    }

  def <<<[R <: Has[AppConfig] with DatabaseProvider](
      acc: Option[Account] => HttpApp[R, Throwable],
  ): HttpApp[R, Throwable] =
    apply(acc)
}
