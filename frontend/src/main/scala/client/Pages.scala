package client

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom.html.Element

object Pages {
  sealed trait Page extends Product with Serializable {
    val title: String = this.productPrefix
  }
  final case object Home                extends Page
  final case class Login(token: String) extends Page

  val homePage: ReactiveHtmlElement[Element] =
    div(
      h2("Home"),
    )

  def loginPage(token: String): ReactiveHtmlElement[Element] =
    div(
      h2("Login"),
      div(token),
    )

  def renderPage(page: Page): ReactiveHtmlElement[Element] = page match {
    case Home         => homePage
    case Login(token) => loginPage(token)
  }

}
