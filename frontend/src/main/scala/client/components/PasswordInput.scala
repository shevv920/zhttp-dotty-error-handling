package client.components

import com.raquo.laminar.api.L._

class PasswordInput {
  val inputId  = "password-input"
  val inputVar = Var("")
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
  val elem = Seq(
    labelElem,
    inputElem,
  )
}
