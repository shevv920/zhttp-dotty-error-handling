package couchdb

import zio.*
import sttp.client3.*
import sttp.client3.httpclient.zio.HttpClientZioBackend

object SimpleClient {
  val basicUrl = "http://couchdb:5984/"
  val sessionUrl = s"${basicUrl}_session/"
  val body = """{ "username": "admin", "password": "password" } """

  val request = basicRequest.get(uri"$basicUrl").response(asStringAlways)

  val program: ZIO[zio.ZEnv, Throwable, String] =
    for
      backend <- HttpClientZioBackend()
      res     <- request.send(backend).either.absolve
    yield  res.body

}
