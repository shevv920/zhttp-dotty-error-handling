package backend.resources

import db.Tables.AccountsRow
import interop.SlickToZio
import zio.ZIO

object Token {
  import db.Tables._
  import slick.jdbc.PostgresProfile.api._

  def getAccount(token: String) = {
    val query =
      for {
        _   <- Tokens.filter(_.value === token)
        acc <- Accounts
      } yield acc
    SlickToZio(query.result.headOption)
  }
}
