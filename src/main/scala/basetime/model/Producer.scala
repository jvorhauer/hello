package basetime.model

import java.util.UUID

import basetime.Main
import gremlin.scala._


@label("producer")
final case class Producer(
  id: UUID,
  name: String
) extends Party


object Producer {

  val label = "producer"
  private val IdKey = Key[UUID]("id")

  def list() : List[Producer] = Main.graph.V.hasLabel[Producer].toCC[Producer].toList()

  def findAsV(id: UUID): Option[Vertex] = Main.graph.V.hasLabel[Producer].has(IdKey, P.is(id)).headOption
  def find(id: UUID): Option[Producer] = findAsV(id).map(v => v.toCC[Producer])
  def vAsCC(v: Vertex): Producer = v.toCC[Producer]

  def delete(id: UUID): Unit = Main.graph.V.hasLabel[Producer].has(IdKey, P.is(id)).drop
}

