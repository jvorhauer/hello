package basetime.model.transfer

import java.time.ZonedDateTime
import java.util.UUID

import basetime.Repository
import basetime.model.{ Command, Person, Producer }
import gremlin.scala._
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.{ Error, ParsingFailure }


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

  def process(command: Command): Either[Error, Option[Person]] = {
    command match {
      case Command("worker", "create", data) => decode[Worker](data).map(save)
      case Command("worker", "udpate", data) => decode[Worker](data).map(save)
      case other => Left(ParsingFailure(s"NotApplicable $other", new RuntimeException(s"NotApplicable $other")))
    }
  }
}
