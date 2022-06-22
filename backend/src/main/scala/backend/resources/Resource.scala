package backend.resources

import kuzminki.api.Model
import kuzminki.sorting.Sorting
import zio.prelude.Assertion.greaterThanOrEqualTo
import zio.prelude.Subtype

trait Resource[T <: Model] {
  import Resource._

  type Limit = Limit.Type
  type Page  = Page.Type

  val defaultOrder: T => Seq[Sorting]

  lazy val defaultLimit: Limit = Limit(10)
  lazy val defaultPage: Page   = Page(1)

  val items: T

}

object Resource {
  type Limit = Limit.type
  type Page  = Page.type

  object Limit extends Subtype[Int] {
    override def assertion = assert {
      greaterThanOrEqualTo(0)
    }
  }

  object Page extends Subtype[Int] {
    override def assertion = assert {
      greaterThanOrEqualTo(1)
    }
  }
}
