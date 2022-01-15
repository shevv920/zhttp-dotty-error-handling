package client

import com.raquo.laminar.api.L._
import org.scalajs.dom

object Main extends App {
    val containerNode = dom.document.querySelector("#root")
    render(containerNode, Router.container)
}
