package basetime

import gremlin.scala.{ ScalaGraph, Vertex }
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import gremlin.scala._
import org.apache.tinkerpop.gremlin.structure.io.IoCore

object Repository {
  val graph: ScalaGraph = TinkerGraph.open().asScala

  def list(label: String): List[Vertex] = graph.V.hasLabel(label).toList()
  def dump(): Unit = graph.graph.io(IoCore.graphml()).writeGraph("target/main-graph.xml")
}
