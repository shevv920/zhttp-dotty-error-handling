package backend

import common.Common.commonValue
import zhttp.http._
import zio.ZIO

object Routes {
  val public: Http[Any, Nothing, Any, UResponse] = Http.collect {
    case Method.GET -> !! / "health" =>
      Response.text("ok")
    case Method.GET -> !! =>
      Response.text(commonValue.toString)
  }

  val publicM: Http[Any, Throwable, Any, UResponse] = Http.collectM {
    case Method.GET -> !! / "M" => ZIO.effect(Response.text("M"))
  }

  val authed: Http[Any, Throwable, Any, UResponse] = Http.collectM {
    case Method.GET -> !! / "a" => ZIO.effect(Response.text("a"))
  }
}
