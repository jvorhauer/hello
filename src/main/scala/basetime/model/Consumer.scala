package basetime.model

import java.util.UUID

import basetime.Repository
import gremlin.scala._


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
  def list             : List[Consumer]   = Repository.all(label).map(_.toCC[Consumer])
  def find(id: UUID)   : Option[Vertex]   = locate(id).headOption
  def delete(id: UUID) : Unit             = locate(id).drop.iterate()

  private def locate(id: UUID) = Repository.graph.V.hasLabel(label).has(IdKey, P.is(id))
}
