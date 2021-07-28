package com.example

import java.io.IOException
import zio.{ App, ExitCode, Schedule, URIO, ZEnv, ZIO }
import zio.console.{ getStrLn, putStrLn, Console }
import zio.random._
import zio.clock
import zio.clock.Clock
import zio.duration._
import zhttp.http._
import zhttp.service.Server


object Main extends App:

  val app = Http.collectM[Request] {
    case Method.GET -> Root / "health" => putStrLn("health check ok") *> ZIO.succeed(Response.text("ok"))
    case Method.GET -> Root => ZIO.succeed(Response.text("root"))
  }

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    Server.start(9000, app).exitCode
