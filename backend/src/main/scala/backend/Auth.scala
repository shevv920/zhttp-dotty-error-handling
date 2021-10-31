package backend

import backend.resources.DatabaseProvider.DatabaseProvider
import backend.resources.Token
import zhttp.http.{ Http, HttpApp, HttpError, Request, UResponse }
import zio.ZIO

object Auth {
  private val fail = Http.succeed(HttpError.BadRequest("No auth token provided").toResponse)

  def apply[R <: DatabaseProvider](
      app: Option[AccountsRow] => HttpApp[R, Throwable],
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

  def <<<[R <: DatabaseProvider](
      app: Option[AccountsRow] => HttpApp[R, Throwable],
  ): HttpApp[R, Throwable] =
    apply(app)
}
