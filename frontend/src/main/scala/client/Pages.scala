package client

import com.raquo.laminar.CollectionCommand
import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import common.Protocol.{ SigninRequest, SignupRequest }
import io.laminext.fetch.{ Fetch, FetchResponse }
import io.laminext.syntax.core._
import org.scalajs.dom.html.Element
import zio.json._

import scala.util.{ Failure, Success }

object Pages {
  sealed trait Page extends Product with Serializable {
    val title: String = this.productPrefix
  }
  final case object Home   extends Page
  final case object Signin extends Page
  final case object Signup extends Page

  val homePage: ReactiveHtmlElement[Element] =
    div(
      h2("Home")
    )

  def signinPage: ReactiveHtmlElement[Element] = {
    val (responsesStream, responseReceived) = EventStream.withCallback[FetchResponse[String]]

    val usernameInput = components.usernameInput()
    val passwordInput = components.passwordInput()

    val disabledSignal = usernameInput.inputVar.signal
      .combineWithFn(passwordInput.inputVar.signal)((a1, b1) => a1.isEmpty || b1.isEmpty)

    div(
      h2("Sign in"),
      form(
        thisEvents(onSubmit.preventDefault).flatMap { _ =>
          Fetch
            .post(
              "http://localhost:9000/accounts/signin",
              body = SigninRequest(
                usernameInput.inputVar.now(),
                passwordInput.inputVar.now(),
              ).toJson,
            )
            .text
        } --> responseReceived,
        usernameInput.elem,
        passwordInput.elem,
        button(
          typ := "submit",
          "Submit",
          disabled <-- disabledSignal,
        ),
      ),
    )
  }

  def signupPage = {
    val (responsesStream, responseReceived) = EventStream.withCallback[FetchResponse[String]]

    val usernameInput = components.usernameInput()
    val passwordInput = components.passwordInput()

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
                  passwordInput.inputVar.now(),
                ).toJson,
              )
              .text
          } --> responseReceived,
        usernameInput.elem,
        passwordInput.elem,
        button(typ := "submit", "Submit"),
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

  def renderPage(page: Page): ReactiveHtmlElement[Element] = page match {
    case Home   => homePage
    case Signup => signupPage
    case Signin => signinPage
  }

}
