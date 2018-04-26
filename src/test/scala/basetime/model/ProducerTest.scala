package basetime.model

import java.util.UUID

import basetime.Repository
import gremlin.scala._
import org.scalatest.{ BeforeAndAfter, Matchers, WordSpecLike }


class ProducerTest extends WordSpecLike with Matchers with BeforeAndAfter {

  before {
    Repository.graph.V.drop().iterate()
  }

  "A Producer" should {
    "Save a new Producer" in {
      val id = UUID.randomUUID()
      val p = Producer(id, "test")
      val s = Producer.save(p)
      s.id should be (id)
      s.name should be ("test")
    }

    "Process a new Producer Command" in {
      val id = UUID.randomUUID()
      val command = Command("producer", "create", s"""{"id":"$id","name":"test"}""")
      Producer.process(command)

      val f = Producer.find(id)
      f shouldBe defined
      Producer.list should have size 1
      val p = f.get.toCC[Producer]
      p.id should be (id)
      p.name should be ("test")
    }

    "Find an existing Producer" in {
      val id = UUID.randomUUID()
      Producer.save(Producer(id, "found"))

      val f = Producer.find(id)
      f shouldBe defined
      val fp = f.get.toCC[Producer]
      fp.id should be (id)
      fp.name should be ("found")

      Producer.list should have size 1
    }

    "Not find a non-existing Producer" in {
      val id = UUID.randomUUID()

      val nf = Producer.find(id)
      nf shouldBe None
    }

    "Update a Producer" in {
      val id = UUID.randomUUID()
      Producer.save(Producer(id, "old"))
      Producer.list should have size 1
      val f = Producer.find(id)
      f shouldBe defined
      val fp = f.get.toCC[Producer]
      fp.id should be (id)
      fp.name should be ("old")

      Producer.save(Producer(id, "new"))
      Producer.list should have size 1
      val f2 = Producer.find(id)
      f2 shouldBe defined
      val fp2 = f2.get.toCC[Producer]
      fp2.id should be (id)
      fp2.name should be ("new")
    }

    "Delete a Producer" in {
      val id = UUID.randomUUID()
      val p = Producer.save(Producer(id, "found"))
      Producer.list should have size 1

      Producer.delete(p)
      Producer.list should have size 0
    }
  }
}
