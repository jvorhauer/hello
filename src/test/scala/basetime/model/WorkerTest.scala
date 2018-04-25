package basetime.model

import java.util.UUID

import basetime.Repository
import basetime.model.transfer.Worker
import gremlin.scala._
import org.scalatest.{ BeforeAndAfter, Matchers, WordSpecLike }


class WorkerTest extends WordSpecLike with Matchers with BeforeAndAfter {

  before {
    Repository.graph.V.drop().iterate()
  }

  "Worker" should {
    "Save a new Worker" in {
      val id = UUID.randomUUID()
      Producer.save(Producer(id, "test"))

      Worker.save(Worker("test@basetime.nl", "test", id))
      val wv = Person.find("test@basetime.nl")
      wv shouldBe defined
      val p = wv.get.toCC[Person]
      p.email should be ("test@basetime.nl")
      p.name should be ("test")
      wv.get.outE("EMPLOYED_BY").value(Worker.Role).head should be ("WORKER")
      val when = wv.get.outE("EMPLOYED_BY").value(Worker.Since).head
      println(s"when? $when")
    }
  }
}
