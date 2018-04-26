package basetime.model

import java.time.ZonedDateTime

import basetime.Repository
import basetime.model.transfer.Worker
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
  private val Since = Key[ZonedDateTime]("since")
  private val Role  = Key[String]("role")

  implicit val graph: ScalaGraph = Repository.graph


  def list               : List[Person]   = Repository.all(label).map(_.toCC[Person])
  def find(email: String): Option[Vertex] = locate(email).headOption

  def save(w: Worker): Option[Person] = {
    println(s"Worker.save: w = $w, producer = ${Producer.find(w.producer).map(_.toCC[Producer])}")

    Producer.find(w.producer).map(producer => {
      Person.find(w.email).map(
        _.updateAs[Person](_.copy(name = w.name))
      ).getOrElse({
        val now = ZonedDateTime.now()
        val saved = graph.addVertex(Person(None, w.email, w.name))
        producer --- ("EMPLOYER_OF", Role -> "EMPLOYER", Since -> now) --> saved
        saved --- ("EMPLOYED_BY", Role -> "WORKER", Since -> now) --> producer
        saved
      }).toCC[Person]
    })
  }

  def getProducer(p: Person): Option[Producer] = {
    find(p.email).flatMap(
      _.out("EMPLOYED_BY").headOption().map(_.toCC[Producer])
    )
  }


  private def locate(email: String) = Repository.graph.V.hasLabel(label).has(IdKey, P.is(email))
}