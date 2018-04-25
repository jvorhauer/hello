package basetime.actors

import akka.actor.{ ActorLogging, Props }
import akka.persistence.{ PersistentActor, RecoveryCompleted }
import basetime.model.{ Command, Producer }
import io.circe.generic.auto._
import io.circe.parser.decode


final class ProducerActor extends PersistentActor with ActorLogging {

  override def persistenceId = "producer"

  override def receiveRecover = {
    case Command("producer", "create", data) => upsert(data)
    case Command("producer", "update", data) => upsert(data)
    case Command("producer", "delete", data) => delete(data)
    case RecoveryCompleted                   => log.info("receiveRecover: recovery completed")
    case other => log.error(s"receiveRecover: persisted $other not handled")
  }

  override def receiveCommand = {
    case c: Command if c.topic == "producer" => persist(c) {
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

  def delete(data: String): Option[Producer] = {
    def rem(p: Producer): Producer = {
      Producer.delete(p.id)
      p
    }
    handle(data, rem)
  }
}

object ProducerActor {
  val props = Props[ProducerActor]
}
