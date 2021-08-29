package backend.couchdb

import zhttp.http.{ Header, HttpData, Method, Request, Response, URL }
import zhttp.service.{ Client => zClient }
import zio.ZIO

object Client {
  val url = "http://couchdb:5984/test/"
  val headers = List(
    Header.host("localhost:5984"),
    Header.basicHttpAuthorization("admin", "password"),
  )

  def getById(id: String) =
    for {
      url <- ZIO.fromEither(URL.fromString(s"$url/$id"))
      req  = Request(Method.GET -> url, headers)
      cl  <- zClient.make
      res <- cl.request(req)
    } yield res.content match {
      case HttpData.CompleteData(data) => Response.jsonString(data.map(_.toChar).mkString)
      case HttpData.StreamData(_)      => Response.text("<Chunked>")
      case HttpData.Empty              => Response.text("")
    }

}
