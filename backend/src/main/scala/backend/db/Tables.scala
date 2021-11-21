package backend.db

final case class Account(id: Option[Int], name: String)
final case class Token(id: Option[Int], value: String, accountId: Int)

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure
  * to initialize this late.)
  */
object Tables {
  val profile = slick.jdbc.PostgresProfile
  import profile.api._
  import slick.model.ForeignKeyAction

  class Accounts(tag: Tag) extends Table[Account](tag, "accounts") {
    def id   = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")

    def * = (id.?, name) <> (Account.tupled, Account.unapply)
  }

  val accounts = TableQuery[Accounts]

  class Tokens(tag: Tag) extends Table[Token](tag, "tokens") {
    def id        = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def value     = column[String]("value")
    def accountId = column[Int]("accountId")
    def account   = foreignKey("ACC_FK", accountId, accounts)(_.id, ForeignKeyAction.NoAction)

    def * = (id.?, value, accountId) <> (Token.tupled, Token.unapply)
  }

  val tokens = TableQuery[Tokens]
}
