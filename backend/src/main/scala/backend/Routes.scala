package backend

import backend.resources.Account
import common.Common.commonValue
import zhttp.http.{ Http, Method, Request, Response }
import zio.ZIO
import zio.console.putStrLn
import zhttp.http._

object Routes {

  def authenticate[R, E](fail: HttpApp[R, E], success: HttpApp[R, E]): HttpApp[R, E] =
    Http.flatten {
      HttpApp.fromFunction {
        _.getBasicAuthorizationCredentials
          .flatMap(_ => Some(false))
          .fold[HttpApp[R, E]](fail)(_ => success)
      }
    }

  val success: HttpApp[Any, Nothing] = HttpApp.collectM {
    case Method.GET -> Root / "health" =>
      ZIO.succeed(Response.text("ok"))
    case Method.GET -> Root =>
      ZIO.succeed(Response.text(commonValue.toString))
  }

  val fail = HttpApp.forbidden("Forbidden!")
  // Composing all the HttpApps together
  //  val app: UHttpApp = authenticate(fail, success)
  val app = success
}
