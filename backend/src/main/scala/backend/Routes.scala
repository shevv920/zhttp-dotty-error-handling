package backend

import backend.resources.{ Accounts, Fruits }
import zhttp.http._

object Routes {
  private val health: Http[Any, Nothing, Request, Response] = Http.collect { case Method.GET -> !! / "health" =>
    Response.text("ok")
  }

  val public = Http.collectHttp[Request] {
    case _ -> !! / "health" =>
      health
    case _ -> "accounts" /: path =>
      Accounts.public.setPath(path)
  }

  val priv = Http.collectHttp[Request] {
    case _ -> "fruits" /: path =>
      Fruits.priv.setPath(path)
    case _ -> "accounts" /: path =>
      Accounts.priv.setPath(path)
  }
}
