package client

import client.components.*
import com.raquo.laminar.CollectionCommand
import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import common.Protocol.{ Password, SigninRequest, SignupRequest }
import io.laminext.fetch.{ Fetch, FetchResponse }
import io.laminext.syntax.core.*
import org.scalajs.dom.html.Element
import zio.json.*

import scala.util.{ Failure, Success }

object Pages {
  sealed trait Page extends Product with Serializable {
    val title: String = s"[Hello there]: ${this.productPrefix}"
  }
  case object Home     extends Page
  case object Signin   extends Page
  case object Signup   extends Page
  case object NotFound extends Page

  def renderPage(page: Page): ReactiveHtmlElement[Element] =
    page match {
      case Home     => pages.Home.value
      case Signup   => pages.Signup.value
      case Signin   => pages.Signin.value
      case NotFound => div("page not found")
    }
}
