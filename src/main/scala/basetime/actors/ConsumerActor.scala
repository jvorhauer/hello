package basetime.actors

import akka.actor.{ ActorLogging, Props }
import akka.persistence.{ PersistentActor, RecoveryCompleted }
import basetime.model.{ Command, Consumer }
import io.circe.generic.auto._
import io.circe.parser.decode


final class ConsumerActor extends PersistentActor with ActorLogging {

  type Handler = String => Consumer

  override val persistenceId = "consumer"

  override def receiveRecover = {
    case Command("consumer", "create", data) => upsert(data)
    case Command("consumer", "update", data) => upsert(data)
    case Command("consumer", "delete", data) => delete(data)
    case RecoveryCompleted                   => log.info("receiveRecover: recovery completed")
    case other                               => log.info(s"receiveRecover: $other still not handled")
  }

  override def receiveCommand = {
    case c: Command if c.topic == "consumer" => persist(c) {
      case Command("consumer", "create", data) => sender ! upsert(data)
      case Command("consumer", "update", data) => sender ! upsert(data)
      case Command("consumer", "delete", data) => sender ! delete(data)
      case other                               => log.error(s"receiveCommand: $other not handled")
    }
    case other => log.error(s"receiveCommand: $other")
  }

  def upsert(data: String): Option[Consumer] = handle(data, Consumer.save)

  def delete(data: String): Option[Consumer] = {
    def rem(c: Consumer): Consumer = {
      Consumer.delete(c.id)
      c
    }
    handle(data, rem)
  }

  def handle(data: String, f: Consumer => Consumer): Option[Consumer] = {
    decode[Consumer](data) match {
      case Left(e) =>
        log.error(s"$data is not a Consumer: $e")
        None
      case Right(c) => Some(f(c))
    }
  }
}


object ConsumerActor {
  val props = Props[ConsumerActor]
}
