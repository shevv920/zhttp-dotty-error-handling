package client.components

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom.HTMLElement
import scala.language.postfixOps

class PasswordInput(val inputId: String = "password-input") extends CustomComponent:
  val inputVar: Var[String] = Var("")

  val inputElem = input(
    name   := "password",
    idAttr := inputId,
    typ    := "password",
    controlled(
      value <-- inputVar,
      onInput.mapToValue --> inputVar,
    ),
  )

  val labelElem = label(
    forId := inputId,
    "Password",
  )

  import client.styles.given
  override val elem = div(
    PasswordInput.Styles.wrapper,
    labelElem,
    inputElem,
  )

object PasswordInput:
  import scalacss.DevDefaults.*
  object Styles extends StyleSheet.Inline {
    import dsl.*

    val wrapper = style(
      display.flex,
      flexDirection.column,
    )
  }
