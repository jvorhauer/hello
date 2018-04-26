package basetime.actors

import akka.actor.{ Actor, ActorLogging, Props }
import basetime.model.transfer.Worker
import basetime.model.{ Command, Person }


final class WorkerActor extends Actor with ActorLogging {

  override def receive = {
    case c: Command if c.topic == "worker" => c match {
      case Command("worker", "create", _) => sender ! upsert(c)
      case Command("worker", "update", _) => sender ! upsert(c)
      case other => log.error(s"receive: NotApplicable $other")
    }
  }

  def upsert(command: Command): Option[Person] = Worker.process(command) match {
    case Left(e) =>
      log.error(s"upsert: $command: $e")
      None
    case Right(p) => p
  }
}

object WorkerActor {
  val props = Props[WorkerActor]
}
