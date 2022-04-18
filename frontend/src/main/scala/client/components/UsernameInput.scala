package client.components

import com.raquo.laminar.api.L._

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
