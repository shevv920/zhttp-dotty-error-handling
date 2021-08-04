package couchdb

import zio.*
import sttp.client3.*
import sttp.client3.httpclient.zio.HttpClientZioBackend

object SimpleClient {
  val basicUrl = "http://couchdb:5984/"
  val sessionUrl = s"${basicUrl}_session/"
  val body = """{ "name": "admin", "password": "password" }"""

  val request = basicRequest
    .contentType("application/json")
    .post(uri"$sessionUrl")
    .body(body)
    .response(asStringAlways)

  val program: ZIO[zio.ZEnv, Throwable, String] =
    for
      backend <- HttpClientZioBackend()
      res     <- request.send(backend).either.absolve
      cookies = res.unsafeCookies
      res2    <- basicRequest.cookies(cookies).get(uri"${basicUrl}_all_dbs").response(asStringAlways).send(backend).either.absolve
    yield  res2.body

}
