package client.components

import com.raquo.laminar.api.L._

import scala.language.postfixOps

class UsernameInput {
  val inputId  = "username-input"
  val inputVar = Var("")
  val inputElem = input(
    name   := "username",
    idAttr := inputId,
    typ    := "text",
    controlled(
      value <-- inputVar,
      onInput.mapToValue --> inputVar,
    ),
    className := UsernameInput.valid.htmlClass,
  )
  val labelElem = label(
    forId := inputId,
    "Username",
  )
  val elem = Seq(
    labelElem,
    inputElem,
  )
}

import scalacss.DevDefaults._
object UsernameInput extends StyleSheet.Inline {
  import dsl._

  val common = mixin(
    borderRadius(0 px),
    border(1 px, solid, black),
    outline.none,
  )

  val invalid = style(
    common,
    border(1 px, solid, red),
  )

  val valid = style(
    common
  )
}
