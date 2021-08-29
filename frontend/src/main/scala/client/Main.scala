package client

import com.raquo.laminar.api.L._
import io.laminext.fetch.{ Fetch, FetchResponse }
import io.laminext.syntax.core._
import org.scalajs.dom

object Main {
  def main(args: Array[String]): Unit = {

    val (responsesStream, responseReceived) = EventStream.withCallback[FetchResponse[String]]
    val resContainer = pre(
      child <-- responsesStream.map(_.data),
    )
    val s = Fetch.get("http://localhost:9000/").text
    val rootElement =
      div(
        div(s"below button should appear text: ${common.Common.commonValue.toString}"),
        button(
          "toggle",
          thisEvents(onClick).flatMap(_ => s) --> responseReceived,
        ),
        resContainer,
      )

    val containerNode = dom.document.querySelector("#root")
    render(containerNode, rootElement)
    ()
  }
}
