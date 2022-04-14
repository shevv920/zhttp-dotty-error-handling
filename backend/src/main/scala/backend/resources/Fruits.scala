package backend.resources

import common.Protocol.RequestParseError
import kuzminki.api._
import kuzminki.sorting.Sorting
import zhttp.http.{ uuid => uuidPath, _ }
import zio.ZIO
import zio.json._

import java.util.UUID

final case class Fruit(id: UUID, name: String)
object Fruit {
  implicit val jsonCodec = zio.json.DeriveJsonCodec.gen[Fruit]
}

final case class InsertFruit(name: String)
object InsertFruit {
  implicit val jsonDecoder = zio.json.DeriveJsonDecoder.gen[InsertFruit]
}

class Fruits extends Model("fruits") {
  val id   = column[UUID]("id")
  val name = column[String]("name")

  val item   = read[Fruit](id, name)
  val insert = write[InsertFruit](name)

  def all = (id, name)
}

object Fruits extends Resource[Fruits] {
  import backend.Implicits._
  import backend.resources.Resource._

  val defaultOrder: Fruits => Seq[Sorting] = _.name.desc

  val items: Fruits = Model.get[Fruits]

  private def get(limit: => Limit = Limit(10), page: => Page = Page(1)) = {
    sql
      .select(items)
      .colsType(_.item)
      .all
      .orderBy(defaultOrder)
      .offset((page - 1) * limit)
      .limit(limit)
      .run
  }

  val closed: Http[Kuzminki, Throwable, Request, Response] = Http.collectZIO {
    case req @ Method.GET -> !! =>
      for {
        limit <- req.qs.int("limit", defaultLimit)
        page  <- req.qs.int("page", defaultPage)
        l     <- Limit.make(limit).mapError(QueryParamParseError(_)).toZIO
        p     <- Page.make(page).mapError(QueryParamParseError(_)).toZIO
        res   <- get(l, p)
      } yield Response.json(res.toJson)

    case req @ Method.POST -> !! =>
      import zio.json._
      val job = for {
        body  <- req.bodyAsString
        fruit <- ZIO.fromEither(body.fromJson[InsertFruit]).tapError(s => ZIO.log(s)).mapError(RequestParseError.apply)
        res   <- sql.insert(items).colsType(_.insert).returningType(_.item).runHead(fruit)
      } yield res

      job.map(j => Response.json(j.toJson))
    case req @ Method.PUT -> !! / uuidPath(id) =>
      import zio.json._
      val job = for {
        body  <- req.bodyAsString
        fruit <- ZIO.fromEither(body.fromJson[Fruit]).mapError(RequestParseError.apply)
        res   <- sql.update(items).set(_.name ==> fruit.name).where(_.id === id).returningType(_.item).runHeadOpt
      } yield res

      job.map(j => Response.text(j.toJson))
    case Method.GET -> !! / uuidPath(id) =>
      sql
        .select(items)
        .colsType(_.item)
        .where(fr => fr.id === id)
        .runHeadOpt
        .map(fruit => Response.json(fruit.toJson))
  }
}
