package basetime.model.transfer

import java.time.ZonedDateTime
import java.util.UUID

import basetime.Repository
import basetime.model.{ Person, Producer }
import gremlin.scala._


/*
 * Worker is a DTO for a Person, but with the link to a Producer;
 * the producerId will be used to look up the Producer and, if found,
 * to link the Person to said Producer
 * The Person creeated as a consequence of the Command containing this Worker
 * will have an Edge to the Producer and a Role of "worker".
 */
case class Worker(
  email   : String,
  name    : String,
  producer: UUID
)


object Worker {

  val Since = Key[ZonedDateTime]("since")
  val Role  = Key[String]("role")

  implicit val graph: ScalaGraph = Repository.graph

  def save(w: Worker): Option[Person] = {
    Producer.find(w.producer).map(producer => {
      val now = ZonedDateTime.now()
      val saved = graph.addVertex(Person(w.email, w.name))
      producer --- ("EMPLOYER_OF", Role -> "EMPLOYER", Since -> now) --> saved
      saved --- ("EMPLOYED_BY", Role -> "WORKER", Since -> now) --> producer
      saved.toCC[Person]
    })
  }
}
