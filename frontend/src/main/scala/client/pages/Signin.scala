package client.pages

import client.components.{ *, given }
import com.raquo.laminar.nodes.ReactiveHtmlElement
import common.Protocol.{ Password, SigninRequest }
import io.laminext.fetch.{ Fetch, FetchResponse }
import io.laminext.syntax.core.*
import org.scalajs.dom.html.Element
import com.raquo.laminar.api.L.*
import zio.json.*

object Signin {

  import scalacss.DevDefaults.*
  object Styles extends StyleSheet.Inline {
    import dsl.*
    
    val wrapper = style(
      display.flex,
      flexDirection.column,
      alignItems.center,
      color(client.styles.Colors.fgPrimary),
      height(100.%%),
      backgroundColor(client.styles.Colors.bgPrimary),
    )

    val form = style(
      display.flex,
      flexDirection.column,
      gap(4.px),
      width(320.px),
    )
  }

  import client.styles.given

  val value: ReactiveHtmlElement[Element] = {
    val (responsesStream, responseReceived) = EventStream.withCallback[FetchResponse[String]]

    val usernameInput = UsernameInput()
    val passwordInput = PasswordInput()

    val disabledSignal = usernameInput.inputVar.signal
      .combineWithFn(passwordInput.inputVar.signal)((a1, b1) => a1.isEmpty || b1.isEmpty)

    div(
      Styles.wrapper,
      h2("Sign in"),
      form(
        Styles.form,
        thisEvents(onSubmit.preventDefault).flatMap { _ =>
          Fetch
            .post(
              "http://localhost:9000/accounts/signin",
              body = SigninRequest(
                usernameInput.inputVar.now(),
                Password(passwordInput.inputVar.now()),
              ).toJson,
            )
            .text
        } --> responseReceived,
        usernameInput,
        passwordInput,
        Button("Submit", "submit").disableBy(disabledSignal),
      ),
    )
  }
}
