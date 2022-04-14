package client

import com.raquo.laminar.api.L
import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import com.raquo.waypoint._
import org.scalajs.dom.html.Element
import upickle.default._

object Router {
  import Pages._

  implicit val UserPageRW: ReadWriter[Login] = macroRW
  implicit val rw: ReadWriter[Page]          = macroRW

  val loginRoute: Route[Login, FragmentPatternArgs[Unit, Unit, String]] =
    Route.withFragment(
      encode = page => FragmentPatternArgs(path = (), query = (), fragment = page.token),
      decode = args => Login(args.fragment),
      pattern = (root / "login" / endOfSegments) withFragment fragment[String],
    )

  val homeRoute: Route[Home.type, Unit] = Route.static(Home, root / endOfSegments)

  val signupRoute = Route.static(Signup, root / "signup")

  val router = new Router[Page](
    routes = List(loginRoute, homeRoute, signupRoute),
    getPageTitle = _.title,
    serializePage = page => write(page)(rw),
    deserializePage = pageStr => read(pageStr)(rw),
  )(
    $popStateEvent = L.windowEvents.onPopState,
    owner = L.unsafeWindowOwner,
  )

  val container: ReactiveHtmlElement[Element] =
    div(
      h1("The client"),
      child <-- router.$currentPage.map(renderPage),
    )

}
