package client.components

import com.raquo.laminar.CollectionCommand
import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom.HTMLElement

import scala.language.postfixOps

// TODO ???
class PasswordInput(val inputId: String = "password-input") extends CustomComponent:
  val inputVar: Var[String]     = Var("")
  val labelTextVar: Var[String] = Var("Password")

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
    child.text <-- labelTextVar,
  )

  def withId(id: String) = {
    inputElem.amend(idAttr := id)
    labelElem.amend(forId  := id)
    this
  }

  def withLabelText(text: String) = {
    labelTextVar.set(text)
    this
  }

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
