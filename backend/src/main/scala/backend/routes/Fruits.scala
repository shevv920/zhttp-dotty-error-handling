package backend.routes

import backend.repositories.FruitRepo
import common.Protocol.{ CreateFruitRequest, QueryParamParseError, RequestParseError }
import zhttp.http.{ uuid as uuidPath, * }
import zio.ZIO
import zio.json.*

object Fruits {
  import backend.repositories.Resource.*

  val closed: Http[FruitRepo, Throwable, Request, Response] = Http.collectZIO {
    case req @ Method.GET -> _ =>
      for
        limit <- req.url.queryParams.int("limit", 10)
        page  <- req.url.queryParams.int("page", 1)
        l     <- Limit.make(limit).toZIO.mapError(RequestParseError.apply)
        p     <- Page.make(page).toZIO.mapError(RequestParseError.apply)
        res   <- FruitRepo.get(p, l)
      yield Response.json(res.toJson)
    case Method.GET -> _ / uuidPath(id) =>
      for res <- FruitRepo.getById(id)
      yield Response.json(res.toJson)
    case req @ Method.POST -> _ =>
      for
        body <- req.bodyAsString
        fruit <-
          ZIO.fromEither(body.fromJson[CreateFruitRequest]).tapError(s => ZIO.log(s)).mapError(RequestParseError.apply)
        res <- FruitRepo.insert(fruit)
      yield Response.json(res.toJson)
  }
}
