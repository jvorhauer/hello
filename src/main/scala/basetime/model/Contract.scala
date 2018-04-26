package basetime.model

import java.time.ZonedDateTime
import java.util.UUID

import gremlin.scala.label
import basetime.Repository
import basetime.model.transfer.ContractTransfer
import gremlin.scala._
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.{ Error, ParsingFailure }
import io.circe.java8.time._


@label("contract")
final case class Contract(
  @id id : Option[Int],
  uuid   : UUID,
  start  : ZonedDateTime,
  end    : ZonedDateTime,
)

object Contract {

  private val label              = "contract"
  private val IdKey              = Key[UUID]("uuid")

  implicit val graph: ScalaGraph = Repository.graph


  def save(c: Contract): Contract = {
    find(c.uuid).map(
      _.updateAs[Contract](_.copy(start = c.start, end = c.end))
    ).getOrElse(
      graph.addVertex(c)
    ).toCC[Contract]
  }

  def save(c: ContractTransfer): Option[Contract] = {
    val prereq = for {
      consumer <- Consumer.find(c.consumer)
      worker <- Person.find(c.worker)
    } yield (consumer, worker)

    prereq.map(pr => {
      Contract.find(c.uuid).map(
        _.updateAs[Contract](_.copy(start = c.start, end = c.end))
      ).getOrElse({
        val (consumerV, workerV) = pr
        val contractV = graph.addVertex(Contract(None, c.uuid, c.start, c.end))
        consumerV --- "PAYS" --> contractV
        contractV --- "WITH" --> consumerV
        workerV --- "OBLIGED" --> contractV
        contractV --- "CONTRACTED" --> workerV
        contractV
      }).toCC[Contract]
    })
  }

  def process(command: Command): Either[Error, Option[Contract]] = {
    command match {
      case Command("contract", "create", data) => decode[ContractTransfer](data).map(save)
      case Command("contract", "update", data) => decode[ContractTransfer](data).map(save)
      case other => Left(ParsingFailure(s"NotApplicable $other", new RuntimeException(s"NotApplicable $other")))
    }
  }

  def find(uuid: UUID): Option[Vertex] = locate(uuid).headOption


  private def locate(uuid: UUID) = graph.V.hasLabel(label).has(IdKey, P.is(uuid))
}