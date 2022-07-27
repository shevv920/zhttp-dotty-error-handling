package client.components

import com.raquo.laminar.api.L._

import scala.language.postfixOps
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom.HTMLElement

class UsernameInput extends CustomComponent {
  val inputId               = "username-input"
  val inputVar: Var[String] = Var("")
  val inputElem: ReactiveHtmlElement[org.scalajs.dom.html.Input] = input(
    name   := "username",
    idAttr := inputId,
    typ    := "text",
    controlled(
      value <-- inputVar,
      onInput.mapToValue --> inputVar,
    ),
  )

  val labelElem: ReactiveHtmlElement[org.scalajs.dom.html.Label] = label(
    forId := inputId,
    "Username",
  )
  import client.styles.given
  override val elem = div(
    UsernameInput.Styles.wrapper,
    labelElem,
    inputElem,
  )
}

object UsernameInput {
  import scalacss.DevDefaults.*
  object Styles extends StyleSheet.Inline {
    import dsl.*

    val wrapper = style(
      display.flex,
      flexDirection.column,
    )
  }
}
