package backend

import common.Common.commonValue

import java.io.IOException
import zio.{ App, ExitCode, Schedule, URIO, ZEnv, ZIO }
import zio.console.{ getStrLn, putStrLn, Console }
import zio.random._
import zio.clock
import zio.clock.Clock
import zio.duration._
import zhttp.http._
import zhttp.service.Server
import couchdb.SimpleClient

object Main extends App:

  val app = Http.collectM[Request] {
    case Method.GET -> Root / "health" =>
      putStrLn("health check ok") *> ZIO.succeed(Response.text("ok"))
    case Method.GET -> Root =>
      ZIO.succeed(Response.text(commonValue.toString))
    case Method.GET -> Root / "testdb" =>
      for
        resp <- SimpleClient.program
      yield Response.text(resp.toString)
  }

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    Server.start(9000, CORS(app)).exitCode
