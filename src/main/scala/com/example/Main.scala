package com.example

import java.io.IOException
import zio.{App, ExitCode, Schedule, URIO, ZEnv, ZIO}
import zio.console.{Console, getStrLn, putStrLn}
import zio.random._
import zio.clock
import zio.clock.Clock
import zio.duration._
import zhttp.http._
import zhttp.service.Server

case class Email(value: String) extends AnyVal
case class User(name: String, email: Email)
case class MyError(message: String) extends Throwable {
  override def toString = message
}

object Main extends App:

  def randomResult[E <: Throwable, T](error: E, result: T) =
    for
      random <- nextBoolean
      _ <- clock.sleep(100.milliseconds)
      res <- ZIO.effect[T] { if (random) result else throw error }
    yield res

  def createUserWithCompany(user: User) =
    for
      user <- randomResult(MyError("Failed to create user"), user)
      _    <- randomResult(MyError("Failed to add user to comapny"), "Added to company")
    yield user

  def sendHelloEmail(user: User) =
    for
      res <- randomResult(MyError("Failed to send email"), "Sent email.")
    yield res

  def createExternalServiceUser(user: User) =
    for
      res <- randomResult(MyError("Failed to add to external"), "Added to external.")
    yield res

  def createUserCleanUp[T](cause: zio.Cause[T]) =
    putStrLn("Create user cleanup.").orDie

  def sendEmailFallback(user: User) =
    putStrLn(s"${user.email} Added to emails to be sent").orDie

  def externalFallback(user: User) =
    putStrLn(s"${user.name} added to be created on external service")

  val app = Http.collectM[Request] {
    case Method.GET -> Root / "health" => ZIO.succeed(Response.text("ok"))
    case Method.GET -> Root / "user" =>
      (for
        parsedUser  <- ZIO.succeed(User("John", Email("some@email.com")))
        user        <- createUserWithCompany(parsedUser)
                        .retryN(5)
                        .onError(createUserCleanUp)
        sendFiber   <- sendHelloEmail(user)
                        .retryN(1)
                        .catchAll(_ => sendEmailFallback(user))
                        .fork
        createExt   <- createExternalServiceUser(user)
                        .retryN(1)
                        .catchAll(_ => externalFallback(user))
                        .fork
        _           <- sendFiber.join
        _           <- createExt.join
        _           <- putStrLn(s"${user.toString} created.")
      yield Response.text(user.toString))
        .uninterruptible
        .catchAllCause(c => ZIO.succeed(Response.text(c.untraced.toString)))
  }

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    Server.start(8090, app).exitCode
