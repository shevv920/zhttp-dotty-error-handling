package backend

import zhttp.http.Request
import zio.ZIO

object Implicits {
  case class QueryParamParseError(message: String) extends Throwable
  object QueryParamParseError {
    implicit val jsonEncoder = zio.json.DeriveJsonEncoder.gen[QueryParamParseError]
  }

  class QueryParams(val src: Map[String, List[String]]) {
    def string(name: String, default: String) = ZIO.attempt(src.get(name).fold(default)(_.head))
    def int(name: String, default: Int) =
      ZIO
        .attempt(src.get(name).fold(default)(_.head.toInt))
        .mapError { case _: NumberFormatException =>
          QueryParamParseError(s"$name has to be int")
        }
    def long(name: String, default: Long) = ZIO.attempt(src.get(name).fold(default)(_.head.toInt))

    def bool(name: String, default: Boolean) =
      ZIO.attempt(src.get(name).fold(default)(_.head.toBoolean)).mapError { case _: IllegalArgumentException =>
        QueryParamParseError(s"$name has to be boolean")
      }
    def intList(name: String, default: List[Int]) = ZIO.attempt(src.get(name).fold(default)(_.map(_.toInt)))

  }

  implicit class RichRequest(val request: Request) {
    def qs = new QueryParams(request.url.queryParams)
  }
}
