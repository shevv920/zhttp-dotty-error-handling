package backend.resources

import backend.resources.DatabaseProvider.DatabaseProvider
import db.Tables
import interop.SlickToZio
import zhttp.http.Request
import zio.ZIO
import zio.json._

object Account {
  import db.Tables._
  import db.Tables.AccountsRow
  import db.Tables.TokensRow
  import slick.jdbc.PostgresProfile.api._
  implicit val encoder = DeriveJsonEncoder.gen[AccountsRow]

  // TODO map request to data type LoginRequest with validations
  final case class LoginRequest(name: String, email: String)
  object LoginRequest {
    implicit val decoder = DeriveJsonDecoder.gen[LoginRequest]
  }
  def login(req: Request) = {
    val body = req.getBodyAsString.map(_.fromJson[LoginRequest])

    body match {
      case None            => ZIO.fail(new Throwable("Error parsing request data: no body"))
      case Some(Left(err)) => ZIO.fail(new Throwable(s"Error parsing request data $err"))
      case Some(Right(creds)) =>
        val query =
          Accounts.filter(acc => acc.name === creds.name && acc.email === creds.email).result.head
        def queryInsert(tokensRow: TokensRow) = (Tokens returning Tokens.map(_.value)) += tokensRow

        for {
          found      <- SlickToZio(query)
          tokenValue <- SlickToZio(queryInsert(TokensRow(s"${found.name}-token", found.id)))
        } yield tokenValue
    }

  }
}
