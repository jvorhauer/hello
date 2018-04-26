package basetime.model

import java.util.UUID

import basetime.Repository
import gremlin.scala._
import io.circe.{ Error, ParsingFailure }
import io.circe.generic.auto._
import io.circe.parser.decode


@label("consumer")
final case class Consumer(
  id   : UUID,
  name : String
)


object Consumer {

  private val label = "consumer"
  private val IdKey = Key[UUID]("id")

  def save(c: Consumer): Consumer = {
    find(c.id).map(_.updateAs[Consumer](_.copy(name = c.name))).getOrElse(Repository.graph.addVertex(c)).toCC[Consumer]
  }
  def list                : List[Consumer] = Repository.all(label).map(_.toCC[Consumer])
  def find(id: UUID)      : Option[Vertex] = locate(id).headOption
  def delete(c: Consumer) : Consumer       = {
    locate(c.id).drop.iterate()
    c
  }

  def process(command: Command): Either[Error, Consumer] = {
    command match {
      case Command("consumer", "create", data) => decode[Consumer](data).map(save)
      case Command("consumer", "update", data) => decode[Consumer](data).map(save)
      case Command("consumer", "delete", data) => decode[Consumer](data).map(delete)
      case other => Left(ParsingFailure(s"NotApplicable $other", new RuntimeException(s"NotApplicable $other")))
    }
  }

  private def locate(id: UUID) = Repository.graph.V.hasLabel(label).has(IdKey, P.is(id))
}
