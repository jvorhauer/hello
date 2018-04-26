package basetime.actors

import akka.actor.{ Actor, ActorLogging, Props }
import basetime.model.{ Command, Consumer }
import io.circe.generic.auto._
import io.circe.parser.decode


final class ConsumerActor extends Actor with ActorLogging {

  type Handler = String => Consumer

  override def receive = {
    case c: Command if c.topic == "consumer" => c match {
      case Command("consumer", "create", data) => sender ! upsert(data)
      case Command("consumer", "update", data) => sender ! upsert(data)
      case Command("consumer", "delete", data) => sender ! delete(data)
      case other => log.error(s"receive: method in $other not handled")
    }
    case other => log.error(s"receive: not a consumer command: $other")
  }

  def handle(data: String, f: Consumer => Consumer): Option[Consumer] = {
    decode[Consumer](data) match {
      case Left(e) =>
        log.error(s"$data is not a Consumer: $e")
        None
      case Right(c) => Some(f(c))
    }
  }

  def upsert(data: String): Option[Consumer] = handle(data, Consumer.save)
  def delete(data: String): Option[Consumer] = handle(data, Consumer.delete)

  def upsert(command: Command): Option[Consumer] = Consumer.process(command) match {
    case Left(e) =>
      log.error(s"upsert: $command: $e")
      None
    case Right(c) => Some(c)
  }
}


object ConsumerActor {
  val props = Props[ConsumerActor]
}
