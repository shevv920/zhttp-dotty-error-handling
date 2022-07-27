package backend.routes

import backend.{ AppConfig, Security }
import backend.repositories.AccountRepo
import common.Protocol.{ Password, RequestError, RequestParseError, SigninRequest, SignupRequest }
import zhttp.http.*
import zio.ZIO
import zio.json.*

object Accounts {
  import backend.repositories.Resource.*

  val public: Http[AccountRepo with AppConfig, Throwable, Request, Response] =
    Http.collectZIO[Request] {
      case req @ Method.POST -> ~~ / "signin" =>
        for
          body: String   <- req.bodyAsString
          signinRequest  <- ZIO.fromEither(body.fromJson[SigninRequest]).mapError(e => RequestParseError(e))
          passwordHashed <- Security.hashPassword(signinRequest.password)
          accOpt         <- AccountRepo.getByUsernameAndPassword(signinRequest.username, passwordHashed)
        yield Response.json(accOpt.fold(Left("Not found"))(acc => Right(acc)).toJson)
      case req @ Method.POST -> ~~ / "signup" =>
        for
          body <- req.bodyAsString
          signupRequest: SignupRequest <-
            ZIO.fromEither(body.fromJson[SignupRequest]).mapError(e => RequestParseError(e))
          accOpt         <- AccountRepo.usernameExists(signupRequest.username)
          _              <- ZIO.when(accOpt)(ZIO.fail(RequestError("Username not available")))
          passwordHashed <- Security.hashPassword(signupRequest.password)
          _              <- AccountRepo.insertSignup(signupRequest.copy(password = Password(passwordHashed)))
          encoded        <- Security.jwtEncode(signupRequest.username)
        yield Response.json(encoded)
    }
}
