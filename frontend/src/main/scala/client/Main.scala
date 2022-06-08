package client

import client.components.UsernameInput
import com.raquo.laminar.api.L._
import org.scalajs.dom

object Main extends App {
  val CssSettings = scalacss.devOrProdDefaults
  import CssSettings._
  val rootNode = dom.document.querySelector("#root")

  val headNode = dom.document.querySelector("head")
  render(
    headNode,
    styleTag(
      UsernameInput.render[String]
    ),
  )
  render(rootNode, Router.container)
}
