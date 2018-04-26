package basetime.model

import basetime.Repository
import gremlin.scala.{ label, _ }


@label("person")
final case class Person(
  @id id : Option[Int],
  email  : String,
  name   : String,
) extends Party


object Person {
  private val label = "person"
  private val IdKey = Key[String]("email")

  def list               : List[Person]   = Repository.all(label).map(_.toCC[Person])
  def find(email: String): Option[Vertex] = locate(email).headOption

  private def locate(email: String) = Repository.graph.V.hasLabel(label).has(IdKey, P.is(email))
}