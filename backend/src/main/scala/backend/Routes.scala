package backend

import backend.resources.Account
import backend.resources.DatabaseProvider.DatabaseProvider
import common.Common.commonValue
import db.Tables.AccountsRow
import zhttp.http.HttpError.BadRequest
import zhttp.http._
import zio.ZIO

object Routes {
  val public = HttpApp.collect {
    case Method.GET -> Root / "health" =>
      Response.text("ok")
    case Method.GET -> Root =>
      Response.text(commonValue.toString)
    case Method.GET -> Root / "r" => Response.temporaryRedirect("http://localhost:9001")
  }

  val publicM = HttpApp
    .collectM { case req @ Method.POST -> Root / "login" =>
      for {
        token <- Account.login(req)
      } yield Response.text(token)
    }
    .catchAll { error: Throwable =>
      HttpApp.error(BadRequest(error.getMessage))
    }

  def authed(accountsRow: Option[AccountsRow]): RHttpApp[DatabaseProvider] = HttpApp.collect {
    case _ if accountsRow.isEmpty  => HttpError.Unauthorized("Unauthorized").toResponse
    case Method.GET -> Root / "me" => Response.text(s"Hello ${accountsRow.get.name}")
  }
}
