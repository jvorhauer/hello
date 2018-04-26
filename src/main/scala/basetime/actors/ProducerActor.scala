package basetime.actors

import akka.actor.{ Actor, ActorLogging, Props }
import basetime.model.{ Command, Producer }
import io.circe.generic.auto._
import io.circe.parser.decode


final class ProducerActor extends Actor with ActorLogging {

  override def receive = {
    case c: Command if c.topic == "producer" => c match {
      case Command("producer", "create", data) => sender ! upsert(data)
      case Command("producer", "update", data) => sender ! upsert(data)
      case Command("producer", "delete", data) => sender ! delete(data)
      case other => log.error(s"receiveCommand: persisted $other not handled")
    }
    case other => log.error(s"receiveCommand: $other")
  }


  def handle(data: String, f: Producer => Producer): Option[Producer] = {
    log.info(s"producer handle: $data")
    decode[Producer](data) match {
      case Left(e) =>
        log.error(s"$data is not a Producer: $e")
        None
      case Right(p) => Some(f(p))
    }
  }

  def upsert(data: String): Option[Producer] = handle(data, Producer.save)
  def delete(data: String): Option[Producer] = handle(data, Producer.delete)
}


object ProducerActor {
  val props = Props[ProducerActor]
}
