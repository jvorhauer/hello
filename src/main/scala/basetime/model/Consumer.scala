package basetime.model

import java.util.UUID

import basetime.Main
import gremlin.scala._


@label("consumer")
final case class Consumer(
  id   : UUID,
  name : String
)

object Consumer {

  private val IdKey = Key[UUID]("id")

  def list : List[Consumer] = Main.graph.V.hasLabel[Consumer].toCC[Consumer].toList()

  def findAsV(id: UUID) = Main.graph.V.hasLabel[Consumer].has(IdKey, P.is(id)).headOption
  def find(id: UUID): Option[Consumer] = findAsV(id).map(v => v.toCC[Consumer])
  def vAsCC(v: Vertex): Consumer = v.toCC[Consumer]

  def delete(id: UUID) = Main.graph.V.hasLabel[Consumer].has(IdKey, P.is(id)).drop
}
