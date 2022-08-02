package client.components

import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom.html

extension (x: ReactiveHtmlElement[html.Button]) {
  def disableBy(signal: Signal[Boolean]) = x.amend(disabled <-- signal)
}

trait CustomComponent:
  def elem: Modifier[ReactiveHtmlElement[org.scalajs.dom.HTMLElement]]

given Conversion[CustomComponent, Modifier[ReactiveHtmlElement[org.scalajs.dom.HTMLElement]]] = _.elem
