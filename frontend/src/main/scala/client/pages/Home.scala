package client.pages

import com.raquo.laminar.api.L.*

object Home {
  import client.styles.given

  val value = div(
    h2("Home", Styles.title)
  )
  import scalacss.DevDefaults.*

  object Styles extends StyleSheet.Inline {
    import dsl.*
    import client.styles.Colors

    val title = style(
      color(Colors.primary),
      backgroundColor(Colors.bgPrimary),
      textAlign.center,
    )
  }
}
