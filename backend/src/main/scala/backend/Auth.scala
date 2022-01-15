package backend


import backend.resources.TokenRepo
import zhttp.http.Middleware.ifThenElseM
import zhttp.http.{Headers, Http, Method, Middleware, Status}
import zio.{Has, ZIO}

object Auth {

  def auth[R, E](verify: Headers => ZIO[R, E, Boolean], responseHeaders: Headers = Headers.empty): Middleware[R, E] =
    ifThenElseM((_, _, h) => verify(h))(
      Middleware.identity,
      Middleware.fromApp(Http.status(Status.FORBIDDEN).addHeaders(responseHeaders)),
    )

  val tokenAuth: Middleware[Has[TokenRepo], Throwable] = auth(headers => {
    headers.getBearerToken match {
      case Some(token) =>
        for {
          acc <- TokenRepo.getAccountByTokenValue(token)
        } yield acc.nonEmpty
      case None => ZIO.succeed(false)
    }
  })
}
