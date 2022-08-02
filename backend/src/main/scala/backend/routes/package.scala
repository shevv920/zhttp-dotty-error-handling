package backend.routes

import backend.repositories.Resource
import backend.repositories.Resource.Page
import common.Protocol.QueryParamParseError
import zio.{ Task, ZIO }

extension (qs: Map[String, List[String]]) {
  def string(name: String, default: String): Task[String] = ZIO.attempt(qs.get(name).fold(default)(_.head))

  def long(name: String, default: Long): Task[Long] = ZIO.attempt(qs.get(name).fold(default)(_.head.toInt))

  def bool(name: String, default: Boolean): ZIO[Any, QueryParamParseError, Boolean] =
    ZIO.attempt(qs.get(name).fold(default)(_.head.toBoolean)).mapError { case _: IllegalArgumentException =>
      QueryParamParseError(s"$name has to be boolean")
    }

  def intList(name: String, default: List[Int]): Task[List[Int]] =
    ZIO.attempt(qs.get(name).fold(default)(_.map(_.toInt)))

  def int(name: String, default: Int): ZIO[Any, QueryParamParseError, Int] =
    ZIO
      .attempt(qs.get(name).fold(default)(_.head.toInt))
      .mapError { case _: NumberFormatException =>
        QueryParamParseError(s"$name has to be int")
      }
}
