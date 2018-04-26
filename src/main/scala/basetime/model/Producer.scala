package basetime.model

import java.util.UUID

import basetime.Repository
import gremlin.scala._
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.{ Error, ParsingFailure }


@label("producer")
final case class Producer(
  id  : UUID,
  name: String
) extends Party


object Producer {

  val label = "producer"
  val IdKey = Key[UUID]("id")

  def save(p: Producer): Producer = {
     find(p.id).map(
       _.updateAs[Producer](_.copy(name = p.name))
     ).getOrElse(
       Repository.graph.addVertex(p)
     ).toCC[Producer]
  }
  def list               : List[Producer] = all.map(_.toCC[Producer])
  def find(id: UUID)     : Option[Vertex] = locate(id).headOption
  def delete(p: Producer): Producer       = {
    locate(p.id).drop.iterate()
    p
  }

  def process(c: Command): Either[Error, Producer] = {
    c match {
      case Command("producer", "delete", data) => decode[Producer](data).map(delete)
      case Command("producer", "create", data) => decode[Producer](data).map(save)
      case Command("producer", "update", data) => decode[Producer](data).map(save)
      case other => Left(ParsingFailure(s"process: $other: $c", new RuntimeException(s"process: $other: $c")))
    }
  }

  private def locate(id: UUID) = Repository.graph.V.hasLabel(label).has(IdKey, P.is(id))
  private def all = Repository.list(label)
}

