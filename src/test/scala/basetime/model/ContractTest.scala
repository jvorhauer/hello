package basetime.model

import java.time.ZonedDateTime
import java.util.UUID

import basetime.Repository
import basetime.model.transfer.Worker
import org.scalatest.{ BeforeAndAfter, Matchers, WordSpecLike }


class ContractTest extends WordSpecLike with Matchers with BeforeAndAfter {

  before {
    Repository.graph.V.drop().iterate()
  }

  "Contract" should {
    "Save a new ContractTransfer" in {
      val start = ZonedDateTime.now().plusDays(1)
      val end   = start.plusDays(3)

      val cid = UUID.randomUUID()
      Consumer.save(Consumer(cid, "testcons"))

      val pid = UUID.randomUUID()
      Producer.save(Producer(pid, "testprod"))

      Worker.save(Worker("contractor@basetime.nl", "contractor", pid))

      val contractId = UUID.randomUUID()
      Contract.save(transfer.ContractTransfer(contractId, start, end, cid, "contractor@basetime.nl"))

      Contract.find(contractId) shouldBe defined
    }
  }
}
