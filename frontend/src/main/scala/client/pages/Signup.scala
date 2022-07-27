package client.pages

import client.components.{ *, given }
import com.raquo.laminar.CollectionCommand
import com.raquo.laminar.nodes.ReactiveHtmlElement
import common.Protocol.{ Password, SignupRequest }
import io.laminext.fetch.{ Fetch, FetchResponse }
import io.laminext.syntax.core.*
import org.scalajs.dom.html.Element
import com.raquo.laminar.api.L.*
import zio.json.*

import scala.util.{ Failure, Success }

object Signup {
  val value: ReactiveHtmlElement[org.scalajs.dom.html.Div] = {
    val (responsesStream, responseReceived) = EventStream.withCallback[FetchResponse[String]]

    val usernameInput = UsernameInput()
    val passwordInput = PasswordInput()

    val responseStream = responsesStream.recoverToTry.map {
      case Success(response) =>
        CollectionCommand.Append(
          div(
            div(
              code("Status: "),
              code(s"${response.status} ${response.statusText}"),
            ),
            div(
              code(response.data)
            ),
          )
        )
      case Failure(exception) =>
        CollectionCommand.Append(
          div(
            div(
              code("Error: "),
              code(exception.getMessage),
            )
          )
        )
    }

    val formElement =
      form(
        thisEvents(onSubmit.preventDefault)
          .flatMap { _ =>
            Fetch
              .post(
                "http://localhost:9000/accounts/signup",
                body = SignupRequest(
                  usernameInput.inputVar.now(),
                  Password(passwordInput.inputVar.now()),
                ).toJson,
              )
              .text
          } --> responseReceived,
        usernameInput,
        passwordInput,
        Button("Submit", "submit"),
      )

    div(
      h2("Signup"),
      formElement,
      div(
        div(
          code("received:")
        ),
        div(
          children.command <-- responseStream
        ),
      ),
    )
  }
}
