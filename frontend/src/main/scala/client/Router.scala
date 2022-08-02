package client

import com.raquo.domtypes.generic.codecs.{ Codec, StringAsIsCodec }
import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import com.raquo.waypoint.*
import org.scalajs.dom.html.Element
import org.scalajs.dom
import zio.json.*

object Router {
  import Pages._

  val loginRoute: Route[Signin.type, Unit]  = Route.static(Signin, root / "signin")
  val homeRoute: Route[Home.type, Unit]     = Route.static(Home, root / endOfSegments)
  val signupRoute: Route[Signup.type, Unit] = Route.static(Signup, root / "signup")

  implicit val codec: JsonCodec[Page] = DeriveJsonCodec.gen[Page]

  val router = new Router[Page](
    routes = List(loginRoute, homeRoute, signupRoute),
    getPageTitle = _.title,
    serializePage = page => page.toJson,
    deserializePage = pageStr => pageStr.fromJson[Page].getOrElse(NotFound),
  )(
    $popStateEvent = windowEvents.onPopState,
    owner = unsafeWindowOwner,
  )

  inline def navigateTo(page: Page): Binder[HtmlElement] =
    Binder { el =>
      val isLinkElement = el.ref.isInstanceOf[dom.html.Anchor]

      if (isLinkElement) {
        el.amend(href(router.absoluteUrlForPage(page)))
      }
      // If element is a link and user is holding a modifier while clicking:
      //  - Do nothing, browser will open the URL in new tab / window / etc. depending on the modifier key
      // Otherwise:
      //  - Perform regular pushState transition
      (onClick
        .filter(ev => !(isLinkElement && (ev.ctrlKey || ev.metaKey || ev.shiftKey || ev.altKey)))
        .preventDefault
        --> (_ => router.pushState(page))).bind(el)
    }

  import styles.given
  val container: ReactiveHtmlElement[Element] =
    div(
      styles.Common.container,
      nav(
        styles.Common.navigate,
        a(navigateTo(Home), "Home"),
        a(navigateTo(Signin), "Signin"),
        a(navigateTo(Signup), "Signup"),
      ),
      child <-- router.$currentPage.map(renderPage),
    )

}
