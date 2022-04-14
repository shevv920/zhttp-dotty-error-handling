package client

import com.raquo.laminar.CollectionCommand
import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import common.Protocol.{ AccountPublic, SignupRequest }
import io.laminext.fetch.{ Fetch, FetchResponse, ToRequestBody }
import org.scalajs.dom.html.Element
import io.laminext.syntax.core._
import zio.json._

import scala.util.{ Failure, Success }

object Pages {
  sealed trait Page extends Product with Serializable {
    val title: String = this.productPrefix
  }
  final case object Home                extends Page
  final case class Login(token: String) extends Page
  final case object Signup              extends Page

  val homePage: ReactiveHtmlElement[Element] =
    div(
      h2("Home")
    )

  def loginPage(token: String): ReactiveHtmlElement[Element] =
    div(
      h2("Login"),
      div(token),
    )

  def signupPage = {
    val usernameVar = Var("")
    val passwordVar = Var("")

    val (responsesStream, responseReceived) = EventStream.withCallback[FetchResponse[String]]
    val usernameInput = input(
      name        := "username",
      idAttr      := "username-input",
      typ         := "text",
      placeholder := "Jane",
      controlled(
        value <-- usernameVar,
        onInput.mapToValue --> usernameVar,
      ),
    )
    val passwordInput = input(
      name   := "password",
      idAttr := "password-input",
      typ    := "password",
      controlled(
        value <-- passwordVar,
        onInput.mapToValue --> passwordVar,
      ),
    )

    val formElement = form(
      thisEvents(onSubmit.preventDefault)
        .flatMap { ev =>
          Fetch
            .post(
              "http://localhost:9000/accounts/signup",
              body = SignupRequest(
                usernameVar.now(),
                passwordVar.now(),
              ).toJson,
            )
            .text
        } --> responseReceived,
      method := "post",
      label(
        target := "username-input",
        "Username",
      ),
      usernameInput,
      label(
        target := "password-input",
        "Password",
      ),
      passwordInput,
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
          children.command <-- responsesStream.recoverToTry.map {
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
        ),
      ),
    )
  }

  def renderPage(page: Page): ReactiveHtmlElement[Element] = page match {
    case Home         => homePage
    case Signup       => signupPage
    case Login(token) => loginPage(token)
  }

}
