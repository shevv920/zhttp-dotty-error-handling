package backend.resources

import slick.basic.BasicBackend
import slick.jdbc.PostgresProfile
import zio.{ Has, Task, ULayer, ZIO, ZLayer }

object DatabaseProvider {
  type DatabaseProvider = Has[DatabaseProvider.Service]

  trait Service {
    def db: Task[BasicBackend#DatabaseDef]
  }

  def live: ULayer[DatabaseProvider] = ZLayer.succeed(new Service {
    override def db: Task[BasicBackend#DatabaseDef] = ZIO.effect(
      PostgresProfile.api.Database.forURL(
        "jdbc:postgresql://postgres/postgres?user=postgres&password=dbpassword",
      ),
    )
  })
}
