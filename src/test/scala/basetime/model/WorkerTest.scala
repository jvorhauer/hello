package basetime.model

import java.util.UUID

import basetime.Repository
import basetime.model.transfer.Worker
import gremlin.scala._
import org.scalatest.{ BeforeAndAfter, Matchers, WordSpecLike }


class WorkerTest extends WordSpecLike with Matchers with BeforeAndAfter {

  val EmailKey = Key[String]("email")

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

    "Process a new Worker Command" in {
      val id = UUID.randomUUID()
      Producer.save(Producer(id, "test"))

      Worker.process(Command("worker", "create", s"""{"email":"proc@basetime.nl","name":"proc","producer":"$id"}"""))
      val wv = Person.find("proc@basetime.nl")
      wv shouldBe defined
      Person.list should have size 1
      val p = wv.get.toCC[Person]
      p.email should be ("proc@basetime.nl")
      p.name should be ("proc")
      wv.get.outE("EMPLOYED_BY").value(Worker.Role).head should be ("WORKER")

      val ovp = Producer.find(id)
      ovp shouldBe defined
      val vp = ovp.get
      vp.outE("EMPLOYER_OF").value(Worker.Role).head should be ("EMPLOYER")
      vp.out("EMPLOYER_OF").value(EmailKey).head should be ("proc@basetime.nl")

      val person = vp.out("EMPLOYER_OF").head().toCC[Person]
      println(s"person: $person")
    }
  }
}
