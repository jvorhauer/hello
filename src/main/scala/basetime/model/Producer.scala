package basetime.model

import java.util.UUID

import basetime.Repository
import gremlin.scala._


@label("producer")
final case class Producer(
  id: UUID,
  name: String
) extends Party


object Producer {

  val label = "producer"
  private val IdKey = Key[UUID]("id")

  def save(p: Producer): Producer = {
     find(p.id).map(
       vertex => vertex.updateAs[Producer](producer => producer.copy(name = p.name))
     ).getOrElse(
       Repository.graph.addVertex(p)
     ).toCC[Producer]
  }
  def list            : List[Producer] = all.map(_.toCC[Producer])
  def find(id: UUID)  : Option[Vertex] = locate(id).headOption
  def delete(id: UUID): Unit           = locate(id).drop.iterate()

  private def locate(id: UUID) = Repository.graph.V.hasLabel(label).has(IdKey, P.is(id))
  private def all = Repository.all(label)
}

