package backend.resources

import backend.Config.AppConfig
import interop.SlickToZio._
import zhttp.http.Request
import zio.ZIO
import zio.json._
import zio.config._

object Account {
  import backend.db.Tables._
  import backend.db._
  import backend.db.Tables.profile.api._

  implicit val encoder = DeriveJsonEncoder.gen[Account]

  // TODO map request to data type LoginRequest with validations
  final case class LoginRequest(name: String, email: String)
  object LoginRequest {
    implicit val decoder = DeriveJsonDecoder.gen[LoginRequest]
  }

  val createIfNotExists = accounts.schema.createIfNotExists.toZIO
  def login(req: Request) = {
    val body = req.getBodyAsString.map(_.fromJson[LoginRequest])

    body match {
      case None            => ZIO.fail(new Throwable("Error parsing request data: no body"))
      case Some(Left(err)) => ZIO.fail(new Throwable(s"Error parsing request data $err"))
      case Some(Right(creds)) =>
        val query =
          accounts.filter(acc => acc.name === creds.name).result.head
        def queryInsert(tokensRow: Token) = (tokens returning tokens.map(_.value)) += tokensRow

        for {
          config     <- getConfig[AppConfig]
          _          <- ZIO.when(config.env == "development")(createIfNotExists)
          found      <- query.toZIO
          tokenValue <- queryInsert(Token(None, s"${found.name}-token", found.id.get)).toZIO
        } yield tokenValue
    }
  }

}
