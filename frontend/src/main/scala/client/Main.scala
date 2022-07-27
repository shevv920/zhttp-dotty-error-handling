package client

import client.pages.Home
import com.raquo.laminar.api.L.*
import org.scalajs.dom

object Main extends App {
  val CssSettings = scalacss.devOrProdDefaults
  import CssSettings._
  val rootNode: dom.Element = dom.document.querySelector("#root")
  val headNode: dom.Element = dom.document.querySelector("head")

  render(
    headNode,
    styleTag(
      styles.Default.render[String],
      styles.Common.render[String],
      components.UsernameInput.Styles.render[String],
      components.PasswordInput.Styles.render[String],
      pages.Home.Styles.render[String],
      pages.Signin.Styles.render[String],
    ),
  )
  render(rootNode, Router.container)
}
