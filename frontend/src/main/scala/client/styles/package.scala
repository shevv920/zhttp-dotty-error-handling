package client.styles

import scalacss.DevDefaults._

object Colors {
  import scalacss.internal.Dsl.c

  val primary     = c"#f66600"
  val bgPrimary   = c"#1f1f1f"
  val bgSecondary = c"#f2f2f2"
  val fgPrimary   = c"#ffffff"
}

object Default extends StyleSheet.Standalone {
  import dsl._

  inline def borderPrimary = border(1.px, solid, Colors.primary)

  "*" - (
    margin(0.px),
    padding(0.px),
    outline(0.px),
  )

  "a" - (
    color(Colors.primary)
  )

  "button" - (
    color(Colors.fgPrimary),
    cursor.pointer,
    backgroundColor(Colors.primary),
  )

  "body" - (
    backgroundColor(Colors.bgPrimary)
  )

  "button, input" - (
    padding(2.px, 4.px),
    borderPrimary
  )

  "input" - (
    backgroundColor(Colors.bgSecondary)
  )
}

object Common extends StyleSheet.Inline {
  import dsl._

  val container = style(
    padding(4.px)
  )

  val navigate = style(
    display.flex,
    gap(4.px),
  )
}

import com.raquo.laminar.api.L.*
given Conversion[StyleA, Setter[HtmlElement]] = cls := _.htmlClass
